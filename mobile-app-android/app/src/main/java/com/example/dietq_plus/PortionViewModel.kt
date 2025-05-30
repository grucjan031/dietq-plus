package com.example.dietq_plus

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

data class PortionableDish(
    val originalDish: Dish,
    val currentPortions: Float = 1.0f
) {
    val adjustedIngredients: List<AdjustedIngredient>
        get() = originalDish.skladniki.map { ingredient ->
            AdjustedIngredient(
                nazwa = ingredient.nazwa,
                originalAmount = ingredient.ilosc,
                adjustedAmount = ingredient.ilosc * currentPortions,
                jednostka = ingredient.jednostka,
                wartosci_na_100g = ingredient.wartosci_na_100g
            )
        }

    val adjustedNutritionalValues: NutritionalValues
        get() = with(originalDish.wartosci_odzywcze) {
            NutritionalValues(
                kcal = kcal * currentPortions,
                bialko = bialko * currentPortions,
                weglowodany = weglowodany * currentPortions,
                tluszcze = tluszcze * currentPortions
            )
        }
}

data class AdjustedIngredient(
    val nazwa: String,
    val originalAmount: Int,
    val adjustedAmount: Float,
    val jednostka: String,
    val wartosci_na_100g: NutritionalValues
)

class PortionViewModel : ViewModel() {
    private val _portionableDish = MutableStateFlow<PortionableDish?>(null)
    val portionableDish: StateFlow<PortionableDish?> = _portionableDish

    private val _isInShoppingList = MutableStateFlow(false)
    val isInShoppingList: StateFlow<Boolean> = _isInShoppingList

    fun setDish(dish: Dish) {
        _portionableDish.value = PortionableDish(dish)
        // Sprawdź czy danie jest już w liście zakupów
        _isInShoppingList.value = ShoppingListRepository.isInShoppingList(dish.id)
    }

    fun updatePortions(portions: Float) {
        _portionableDish.value?.let { currentDish ->
            _portionableDish.value = currentDish.copy(currentPortions = portions)
        }
    }

    fun addToShoppingList() {
        _portionableDish.value?.let { portionableDish ->
            ShoppingListRepository.addDishToShoppingList(portionableDish)
            _isInShoppingList.value = true
        }
    }

    fun removeFromShoppingList() {
        _portionableDish.value?.let { portionableDish ->
            ShoppingListRepository.removeDishFromShoppingList(portionableDish.originalDish.id)
            _isInShoppingList.value = false
        }
    }
}