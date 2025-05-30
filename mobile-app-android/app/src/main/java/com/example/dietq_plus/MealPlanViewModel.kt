package com.example.dietq_plus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.dietq_plus.PortionableDish
import com.example.dietq_plus.ShoppingListRepository
import com.example.dietq_plus.EnhancedUserSettingsRepository
import com.example.dietq_plus.EnhancedUserSettings

data class MonthlyNutritionStats(
    val averageCalories: Double,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFat: Double,
    val daysWithMeals: Int,
    val totalDays: Int
)

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EnhancedUserSettingsRepository.getInstance(application)
    private val _monthPlans = MutableStateFlow<Map<LocalDate, DayPlan>>(emptyMap())
    val monthPlans: StateFlow<Map<LocalDate, DayPlan>> = _monthPlans

    private val _currentDateCalories = MutableStateFlow(0)
    val currentDateCalories: StateFlow<Int> = _currentDateCalories

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    // StateFlow dla dziennych wartości odżywczych
    private val _currentDateNutrition = MutableStateFlow(NutritionalValues(0.0, 0.0, 0.0, 0.0))
    val currentDateNutrition: StateFlow<NutritionalValues> = _currentDateNutrition

    // Pobieranie początkowych ustawień
    private val initialSettings = repository.getUserSettings()

    private val _targetCalories = MutableStateFlow(initialSettings.targetCalories)
    val targetCalories: StateFlow<Int> = _targetCalories

    private val _availableMealTypes = MutableStateFlow<List<MealType>>(getMealTypesForCount(initialSettings.numberOfMeals))
    val availableMealTypes: StateFlow<List<MealType>> = _availableMealTypes

    // Przechowywanie poprzedniej liczby posiłków do porównania
    private var previousMealCount: Int = initialSettings.numberOfMeals

    init {
        initializeMonth(YearMonth.now())
        updateCurrentDateCalories() // Oblicz kalorie dla początkowo wybranej daty

        viewModelScope.launch {
            repository.userSettingsFlow
                .collect { newSettings ->
                    val newMealCount = newSettings.numberOfMeals
                    _targetCalories.value = newSettings.targetCalories
                    _availableMealTypes.value = getMealTypesForCount(newMealCount)

                    if (newMealCount < previousMealCount) {
                        handleMealCountReduction(previousMealCount, newMealCount)
                    }
                    previousMealCount = newMealCount
                }
        }
    }

    // Pomocnicza funkcja do generowania listy typów posiłków na podstawie liczby
    private fun getMealTypesForCount(count: Int): List<MealType> {
        return when (count) {
            3 -> listOf(MealType.SNIADANIE, MealType.OBIAD, MealType.KOLACJA)
            4 -> listOf(MealType.SNIADANIE, MealType.DRUGIE_SNIADANIE, MealType.OBIAD, MealType.KOLACJA)
            else -> MealType.values().toList() // Dla 5 lub innej/domyślnej wartości
        }
    }

    private fun handleMealCountReduction(oldMealCount: Int, newMealCount: Int) {
        val currentPlans = _monthPlans.value
        val updatedPlans = mutableMapOf<LocalDate, DayPlan>()

        currentPlans.forEach { (date, dayPlan) ->
            // Tworzymy głęboką kopię mapy posiłków i list dań, aby zapewnić niemutowalność
            val newMealsData = dayPlan.meals.mapValues { entry ->
                entry.value.copy(dishes = entry.value.dishes.toMutableList())
            }.toMutableMap()

            // Logika przenoszenia dań i czyszczenia starych posiłków
            // Z 5 posiłków na 4 lub 3 (PODWIECZOREK staje się nieaktywny)
            if (oldMealCount == 5 && newMealCount <= 4) {
                val podwieczorekMeal = newMealsData[MealType.PODWIECZOREK]
                if (podwieczorekMeal != null && podwieczorekMeal.dishes.isNotEmpty()) {
                    val kolacjaMeal = newMealsData[MealType.KOLACJA] ?: Meal(MealType.KOLACJA)
                    val updatedKolacjaDishes = kolacjaMeal.dishes.toMutableList()
                    updatedKolacjaDishes.addAll(podwieczorekMeal.dishes)
                    newMealsData[MealType.KOLACJA] = kolacjaMeal.copy(dishes = updatedKolacjaDishes)
                    newMealsData[MealType.PODWIECZOREK] = podwieczorekMeal.copy(dishes = mutableListOf())
                }
            }

            // Z 5 lub 4 posiłków na 3 (DRUGIE_SNIADANIE staje się nieaktywne)
            if ((oldMealCount == 5 || oldMealCount == 4) && newMealCount == 3) {
                val drugieSniadanieMeal = newMealsData[MealType.DRUGIE_SNIADANIE]
                if (drugieSniadanieMeal != null && drugieSniadanieMeal.dishes.isNotEmpty()) {
                    val obiadMeal = newMealsData[MealType.OBIAD] ?: Meal(MealType.OBIAD)
                    val updatedObiadDishes = obiadMeal.dishes.toMutableList()
                    updatedObiadDishes.addAll(drugieSniadanieMeal.dishes)
                    newMealsData[MealType.OBIAD] = obiadMeal.copy(dishes = updatedObiadDishes)
                    newMealsData[MealType.DRUGIE_SNIADANIE] = drugieSniadanieMeal.copy(dishes = mutableListOf())
                }
            }

            updatedPlans[date] = dayPlan.copy(meals = newMealsData)
        }
        _monthPlans.value = updatedPlans
        updateCurrentDateCalories() // Przelicz kalorie po modyfikacjach
    }

    /**
     * Oblicza wartości odżywcze dla konkretnego dnia
     */
    fun calculateDailyNutrition(date: LocalDate): NutritionalValues {
        val dayPlan = _monthPlans.value[date] ?: return NutritionalValues(0.0, 0.0, 0.0, 0.0)
        val activeMealTypes = getMealTypesForCount(previousMealCount)

        return dayPlan.meals.filterKeys { it in activeMealTypes }
            .values.fold(NutritionalValues(0.0, 0.0, 0.0, 0.0)) { acc, meal ->
                meal.dishes.fold(acc) { mealAcc, dish ->
                    NutritionalValues(
                        kcal = mealAcc.kcal + dish.wartosci_odzywcze.kcal,
                        bialko = mealAcc.bialko + dish.wartosci_odzywcze.bialko,
                        weglowodany = mealAcc.weglowodany + dish.wartosci_odzywcze.weglowodany,
                        tluszcze = mealAcc.tluszcze + dish.wartosci_odzywcze.tluszcze
                    )
                }
            }
    }

    /**
     * Oblicza wartości odżywcze dla konkretnego posiłku
     */
    fun calculateMealNutrition(meal: Meal): NutritionalValues {
        return meal.dishes.fold(NutritionalValues(0.0, 0.0, 0.0, 0.0)) { acc, dish ->
            NutritionalValues(
                kcal = acc.kcal + dish.wartosci_odzywcze.kcal,
                bialko = acc.bialko + dish.wartosci_odzywcze.bialko,
                weglowodany = acc.weglowodany + dish.wartosci_odzywcze.weglowodany,
                tluszcze = acc.tluszcze + dish.wartosci_odzywcze.tluszcze
            )
        }
    }

    /**
     * Aktualizuje wartości odżywcze dla aktualnie wybranej daty
     */
    private fun updateCurrentDateNutrition() {
        _currentDateNutrition.value = calculateDailyNutrition(_selectedDate.value)
    }

    /**
     * Pobiera procentowy rozkład makroskładników dla danej daty
     */
    fun getMacroPercentages(date: LocalDate): Triple<Float, Float, Float> {
        val nutrition = calculateDailyNutrition(date)
        val totalCalories = nutrition.kcal

        if (totalCalories <= 0) {
            return Triple(0f, 0f, 0f)
        }

        val proteinPercentage = ((nutrition.bialko * 4) / totalCalories * 100).toFloat()
        val carbsPercentage = ((nutrition.weglowodany * 4) / totalCalories * 100).toFloat()
        val fatPercentage = ((nutrition.tluszcze * 9) / totalCalories * 100).toFloat()

        return Triple(proteinPercentage, carbsPercentage, fatPercentage)
    }

    /**
     * Pobiera szczegółowe statystyki dla miesiąca
     */
    fun getMonthlyNutritionStats(yearMonth: YearMonth): MonthlyNutritionStats {
        val monthPlans = _monthPlans.value
        val activeMealTypes = getMealTypesForCount(previousMealCount)

        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var daysWithMeals = 0

        for (dayOfMonth in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(dayOfMonth)
            val dayPlan = monthPlans[date]

            if (dayPlan != null) {
                val hasMeals = dayPlan.meals.filterKeys { it in activeMealTypes }
                    .values.any { it.dishes.isNotEmpty() }

                if (hasMeals) {
                    daysWithMeals++
                    val dailyNutrition = calculateDailyNutrition(date)
                    totalCalories += dailyNutrition.kcal
                    totalProtein += dailyNutrition.bialko
                    totalCarbs += dailyNutrition.weglowodany
                    totalFat += dailyNutrition.tluszcze
                }
            }
        }

        return MonthlyNutritionStats(
            averageCalories = if (daysWithMeals > 0) totalCalories / daysWithMeals else 0.0,
            averageProtein = if (daysWithMeals > 0) totalProtein / daysWithMeals else 0.0,
            averageCarbs = if (daysWithMeals > 0) totalCarbs / daysWithMeals else 0.0,
            averageFat = if (daysWithMeals > 0) totalFat / daysWithMeals else 0.0,
            daysWithMeals = daysWithMeals,
            totalDays = yearMonth.lengthOfMonth()
        )
    }

    fun addDishToMeal(date: LocalDate, mealType: MealType, dish: Dish) {
        val currentPlans = _monthPlans.value.toMutableMap()
        val dayPlan = currentPlans[date] ?: DayPlan(date)

        val meal = dayPlan.meals[mealType]?.copy(
            dishes = (dayPlan.meals[mealType]?.dishes?.toMutableList() ?: mutableListOf()).apply { add(dish) }
        ) ?: Meal(mealType, mutableListOf(dish))

        val updatedMeals = dayPlan.meals.toMutableMap().apply { this[mealType] = meal }
        currentPlans[date] = dayPlan.copy(meals = updatedMeals)

        _monthPlans.value = currentPlans
        if (date == _selectedDate.value) {
            updateCurrentDateCalories()
        }
    }

    fun removeDishFromMeal(date: LocalDate, mealType: MealType, dishIndex: Int) {
        val currentPlans = _monthPlans.value.toMutableMap()
        val dayPlan = currentPlans[date] ?: return
        val meal = dayPlan.meals[mealType] ?: return

        if (dishIndex >= 0 && dishIndex < meal.dishes.size) {
            val updatedDishes = meal.dishes.toMutableList().apply { removeAt(dishIndex) }
            val updatedMeal = meal.copy(dishes = updatedDishes)
            val updatedMeals = dayPlan.meals.toMutableMap().apply { this[mealType] = updatedMeal }
            currentPlans[date] = dayPlan.copy(meals = updatedMeals)
            _monthPlans.value = currentPlans
        }
        if (date == _selectedDate.value) {
            updateCurrentDateCalories()
        }
    }

    fun calculateDailyCalories(date: LocalDate): Int {
        val dayPlan = _monthPlans.value[date] ?: return 0
        // Sumuj kalorie tylko z aktywnych typów posiłków
        val activeMealTypes = getMealTypesForCount(previousMealCount)
        return dayPlan.meals.filterKeys { it in activeMealTypes }
            .values.sumOf { meal ->
                meal.dishes.sumOf { dish ->
                    dish.wartosci_odzywcze.kcal.toInt()
                }
            }
    }

    /**
     * Zaktualizowana metoda updateCurrentDateCalories - teraz również aktualizuje wartości odżywcze
     */
    private fun updateCurrentDateCalories() {
        _currentDateCalories.value = calculateDailyCalories(_selectedDate.value)
        updateCurrentDateNutrition() // Dodana linia
    }

    fun initializeMonth(yearMonth: YearMonth) {
        val newPlans = _monthPlans.value.toMutableMap() // Zachowaj istniejące plany
        val now = LocalDate.now()

        for (dayOfMonth in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(dayOfMonth)
            if (!newPlans.containsKey(date)) { // Dodaj DayPlan tylko jeśli jeszcze nie istnieje
                newPlans[date] = DayPlan(date)
            }
        }
        _monthPlans.value = newPlans
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateCurrentDateCalories() // To już wywołuje updateCurrentDateNutrition()
    }

    fun assignDishToMeal(date: LocalDate, mealType: MealType, dish: Dish) {
        val currentPlans = _monthPlans.value.toMutableMap()
        val dayPlan = currentPlans[date] ?: DayPlan(date)

        // Tworzymy nową mapę meals z zaktualizowanym posiłkiem
        val updatedDishes = dayPlan.meals[mealType]?.dishes?.toMutableList() ?: mutableListOf()
        updatedDishes.add(dish)
        val updatedMeals = dayPlan.meals.toMutableMap()
        updatedMeals[mealType] = Meal(mealType, updatedDishes)
        currentPlans[date] = DayPlan(date, updatedMeals)

        _monthPlans.value = currentPlans
        if (date == _selectedDate.value) {
            updateCurrentDateCalories() // To już wywołuje updateCurrentDateNutrition()
        }
    }
    // DODAJ TE METODY DO MealPlanViewModel.kt (na końcu klasy, przed companion object):

    /**
     * Dodaje danie do planu posiłków i automatycznie do listy zakupów
     */
    fun assignDishToMealAndShoppingList(date: LocalDate, mealType: MealType, dish: Dish) {
        // Dodaj do planu posiłków
        assignDishToMeal(date, mealType, dish)

        // Automatycznie dodaj do listy zakupów z domyślną porcją (1.0)
        val portionableDish = PortionableDish(dish, 1.0f)
        ShoppingListRepository.addDishToShoppingList(portionableDish)
    }

    /**
     * Usuwa danie z posiłku i opcjonalnie z listy zakupów
     */
    fun removeDishFromMealAndShoppingList(date: LocalDate, mealType: MealType, dishIndex: Int, removeFromShoppingList: Boolean = true) {
        val dayPlan = _monthPlans.value[date] ?: return
        val meal = dayPlan.meals[mealType] ?: return

        if (dishIndex >= 0 && dishIndex < meal.dishes.size) {
            val dishToRemove = meal.dishes[dishIndex]

            // Usuń z planu posiłków
            removeDishFromMeal(date, mealType, dishIndex)

            // Sprawdź czy to danie występuje jeszcze w innych posiłkach w tym miesiącu
            if (removeFromShoppingList && !isDishUsedElsewhere(dishToRemove.id, date, mealType, dishIndex)) {
                ShoppingListRepository.removeDishFromShoppingList(dishToRemove.id)
            }
        }
    }

    /**
     * Sprawdza czy danie jest używane w innych posiłkach
     */
    private fun isDishUsedElsewhere(dishId: Int, excludeDate: LocalDate? = null, excludeMealType: MealType? = null, excludeIndex: Int? = null): Boolean {
        val monthPlans = _monthPlans.value

        monthPlans.forEach { (date, dayPlan) ->
            dayPlan.meals.forEach { (mealType, meal) ->
                meal.dishes.forEachIndexed { index, dish ->
                    // Pomiń sprawdzane danie
                    if (date == excludeDate && mealType == excludeMealType && index == excludeIndex) {
                        return@forEachIndexed
                    }

                    if (dish.id == dishId) {
                        return true
                    }
                }
            }
        }

        return false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MealPlanViewModel(this.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY) as Application)
            }
        }
    }
}