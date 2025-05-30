package com.example.dietq_plus

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CustomIngredient(
    val originalIngredient: Ingredient,
    val customAmount: Float = originalIngredient.ilosc.toFloat(),
    val isCustomized: Boolean = false
) {
    val adjustedNutritionalValues: NutritionalValues
        get() {
            val ratio = customAmount / 100f // wartości na 100g
            return with(originalIngredient.wartosci_na_100g) {
                NutritionalValues(
                    kcal = kcal * ratio,
                    bialko = bialko * ratio,
                    weglowodany = weglowodany * ratio,
                    tluszcze = tluszcze * ratio
                )
            }
        }
}

data class CustomizableDish(
    val originalDish: Dish,
    val customIngredients: List<CustomIngredient> = originalDish.skladniki.map {
        CustomIngredient(it)
    },
    val portions: Float = 1.0f
) {
    val totalNutritionalValues: NutritionalValues
        get() {
            val ingredientsNutrition = customIngredients.fold(
                NutritionalValues(0.0, 0.0, 0.0, 0.0)
            ) { acc, customIngredient ->
                val adjusted = customIngredient.adjustedNutritionalValues
                NutritionalValues(
                    kcal = acc.kcal + adjusted.kcal,
                    bialko = acc.bialko + adjusted.bialko,
                    weglowodany = acc.weglowodany + adjusted.weglowodany,
                    tluszcze = acc.tluszcze + adjusted.tluszcze
                )
            }

            // Zastosuj mnożnik porcji
            return NutritionalValues(
                kcal = ingredientsNutrition.kcal * portions,
                bialko = ingredientsNutrition.bialko * portions,
                weglowodany = ingredientsNutrition.weglowodany * portions,
                tluszcze = ingredientsNutrition.tluszcze * portions
            )
        }

    val hasCustomizations: Boolean
        get() = customIngredients.any { it.isCustomized } || portions != 1.0f
}

class CustomDishViewModel : ViewModel() {
    private val _customizableDish = MutableStateFlow<CustomizableDish?>(null)
    val customizableDish: StateFlow<CustomizableDish?> = _customizableDish

    private val _isInShoppingList = MutableStateFlow(false)
    val isInShoppingList: StateFlow<Boolean> = _isInShoppingList

    fun setDish(dish: Dish) {
        _customizableDish.value = CustomizableDish(dish)
        _isInShoppingList.value = ShoppingListRepository.isInShoppingList(dish.id)
    }

    fun updateIngredientAmount(ingredientIndex: Int, newAmount: Float) {
        _customizableDish.value?.let { current ->
            val updatedIngredients = current.customIngredients.toMutableList()
            if (ingredientIndex < updatedIngredients.size) {
                val ingredient = updatedIngredients[ingredientIndex]
                val originalAmount = ingredient.originalIngredient.ilosc.toFloat()
                updatedIngredients[ingredientIndex] = ingredient.copy(
                    customAmount = newAmount,
                    isCustomized = newAmount != originalAmount
                )
                _customizableDish.value = current.copy(customIngredients = updatedIngredients)
            }
        }
    }

    fun updatePortions(newPortions: Float) {
        _customizableDish.value?.let { current ->
            _customizableDish.value = current.copy(portions = newPortions)
        }
    }

    fun resetIngredient(ingredientIndex: Int) {
        _customizableDish.value?.let { current ->
            val updatedIngredients = current.customIngredients.toMutableList()
            if (ingredientIndex < updatedIngredients.size) {
                val ingredient = updatedIngredients[ingredientIndex]
                updatedIngredients[ingredientIndex] = ingredient.copy(
                    customAmount = ingredient.originalIngredient.ilosc.toFloat(),
                    isCustomized = false
                )
                _customizableDish.value = current.copy(customIngredients = updatedIngredients)
            }
        }
    }

    fun resetAllCustomizations() {
        _customizableDish.value?.let { current ->
            val resetIngredients = current.originalDish.skladniki.map {
                CustomIngredient(it)
            }
            _customizableDish.value = current.copy(
                customIngredients = resetIngredients,
                portions = 1.0f
            )
        }
    }

    fun addToShoppingList() {
        _customizableDish.value?.let { customDish ->
            // Konwertuj na PortionableDish z dostosowanymi składnikami
            val adjustedDish = createAdjustedDish(customDish)
            val portionableDish = PortionableDish(adjustedDish, 1.0f)
            ShoppingListRepository.addDishToShoppingList(portionableDish)
            _isInShoppingList.value = true
        }
    }

    fun removeFromShoppingList() {
        _customizableDish.value?.let { customDish ->
            ShoppingListRepository.removeDishFromShoppingList(customDish.originalDish.id)
            _isInShoppingList.value = false
        }
    }

    private fun createAdjustedDish(customDish: CustomizableDish): Dish {
        val adjustedIngredients = customDish.customIngredients.map { customIngredient ->
            customIngredient.originalIngredient.copy(
                ilosc = (customIngredient.customAmount * customDish.portions).toInt()
            )
        }

        return customDish.originalDish.copy(
            skladniki = adjustedIngredients,
            wartosci_odzywcze = customDish.totalNutritionalValues
        )
    }
}