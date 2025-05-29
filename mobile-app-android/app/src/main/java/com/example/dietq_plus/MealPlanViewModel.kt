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


class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserSettingsRepository.getInstance(application)
    private val _monthPlans = MutableStateFlow<Map<LocalDate, DayPlan>>(emptyMap())
    val monthPlans: StateFlow<Map<LocalDate, DayPlan>> = _monthPlans
    private val _currentDateCalories = MutableStateFlow(0)
    val currentDateCalories: StateFlow<Int> = _currentDateCalories

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

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
                        // updateCurrentDateCalories() jest wywoływane w handleMealCountReduction
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
                    val kolacjaMeal = newMealsData[MealType.KOLACJA] ?: Meal(MealType.KOLACJA) // Powinien istnieć
                    val updatedKolacjaDishes = kolacjaMeal.dishes.toMutableList() // Kopia listy dań kolacji
                    updatedKolacjaDishes.addAll(podwieczorekMeal.dishes) // Dodaj dania z podwieczorku
                    newMealsData[MealType.KOLACJA] = kolacjaMeal.copy(dishes = updatedKolacjaDishes)

                    // Wyczyść dania z podwieczorku (nie usuwaj klucza, tylko zawartość)
                    newMealsData[MealType.PODWIECZOREK] = podwieczorekMeal.copy(dishes = mutableListOf())
                }
            }

            // Z 5 lub 4 posiłków na 3 (DRUGIE_SNIADANIE staje się nieaktywne)
            if ((oldMealCount == 5 || oldMealCount == 4) && newMealCount == 3) {
                val drugieSniadanieMeal = newMealsData[MealType.DRUGIE_SNIADANIE]
                if (drugieSniadanieMeal != null && drugieSniadanieMeal.dishes.isNotEmpty()) {
                    val obiadMeal = newMealsData[MealType.OBIAD] ?: Meal(MealType.OBIAD) // Powinien istnieć
                    val updatedObiadDishes = obiadMeal.dishes.toMutableList() // Kopia listy dań obiadu
                    updatedObiadDishes.addAll(drugieSniadanieMeal.dishes) // Dodaj dania z drugiego śniadania
                    newMealsData[MealType.OBIAD] = obiadMeal.copy(dishes = updatedObiadDishes)

                    // Wyczyść dania z drugiego śniadania
                    newMealsData[MealType.DRUGIE_SNIADANIE] = drugieSniadanieMeal.copy(dishes = mutableListOf())
                }
            }

            updatedPlans[date] = dayPlan.copy(meals = newMealsData)
        }
        _monthPlans.value = updatedPlans
        updateCurrentDateCalories() // Przelicz kalorie po modyfikacjach
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
        if (date == _selectedDate.value) { // Zawsze aktualizuj kalorie, jeśli data jest wybrana
            updateCurrentDateCalories()
        }
    }

    fun calculateDailyCalories(date: LocalDate): Int {
        val dayPlan = _monthPlans.value[date] ?: return 0
        // Sumuj kalorie tylko z aktywnych typów posiłków
        val activeMealTypes = getMealTypesForCount(previousMealCount) // Użyj aktualnej liczby posiłków
        return dayPlan.meals.filterKeys { it in activeMealTypes }
            .values.sumOf { meal ->
                meal.dishes.sumOf { dish ->
                    dish.wartosci_odzywcze.kcal.toInt()
                }
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MealPlanViewModel(this.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY) as Application)
            }
        }
    }

    // Ta funkcja nie jest już potrzebna, jeśli _availableMealTypes jest aktualizowane w collect
    // Zamiast tego używamy getMealTypesForCount
    // fun updateAvailableMealTypes() {
    //     val settings = repository.getUserSettings()
    //     _availableMealTypes.value = getMealTypesForCount(settings.numberOfMeals)
    // }

    private fun updateCurrentDateCalories() {
        _currentDateCalories.value = calculateDailyCalories(_selectedDate.value)
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
        updateCurrentDateCalories()
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
            updateCurrentDateCalories()
        }
    }
}