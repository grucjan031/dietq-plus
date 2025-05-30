package com.example.dietq_plus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ShoppingListItem(
    val nazwa: String,
    val totalAmount: Float,
    val jednostka: String,
    val isChecked: Boolean = false,
    val dishSources: List<DishSource> = emptyList()
)

data class DishSource(
    val dishId: Int,
    val dishName: String,
    val amount: Float,
    val portions: Float
)

object ShoppingListRepository {
    private val _shoppingList = MutableStateFlow<Map<String, ShoppingListItem>>(emptyMap())
    val shoppingList: StateFlow<Map<String, ShoppingListItem>> = _shoppingList

    private val _dishesInShoppingList = MutableStateFlow<Set<Int>>(emptySet())
    val dishesInShoppingList: StateFlow<Set<Int>> = _dishesInShoppingList

    fun addDishToShoppingList(portionableDish: PortionableDish) {
        val currentList = _shoppingList.value.toMutableMap()
        val currentDishes = _dishesInShoppingList.value.toMutableSet()

        portionableDish.adjustedIngredients.forEach { ingredient ->
            val key = "${ingredient.nazwa}_${ingredient.jednostka}"
            val existingItem = currentList[key]

            val dishSource = DishSource(
                dishId = portionableDish.originalDish.id,
                dishName = portionableDish.originalDish.nazwa,
                amount = ingredient.adjustedAmount,
                portions = portionableDish.currentPortions
            )

            if (existingItem != null) {
                // Sprawdź czy to danie już jest w źródłach tego składnika
                val existingSourceIndex = existingItem.dishSources.indexOfFirst {
                    it.dishId == portionableDish.originalDish.id
                }

                val updatedSources = if (existingSourceIndex >= 0) {
                    // Aktualizuj istniejące źródło
                    existingItem.dishSources.toMutableList().apply {
                        set(existingSourceIndex, dishSource)
                    }
                } else {
                    // Dodaj nowe źródło
                    existingItem.dishSources + dishSource
                }

                val newTotalAmount = updatedSources.sumOf { it.amount.toDouble() }.toFloat()

                currentList[key] = existingItem.copy(
                    totalAmount = newTotalAmount,
                    dishSources = updatedSources
                )
            } else {
                currentList[key] = ShoppingListItem(
                    nazwa = ingredient.nazwa,
                    totalAmount = ingredient.adjustedAmount,
                    jednostka = ingredient.jednostka,
                    dishSources = listOf(dishSource)
                )
            }
        }

        currentDishes.add(portionableDish.originalDish.id)
        _shoppingList.value = currentList
        _dishesInShoppingList.value = currentDishes
    }

    fun removeDishFromShoppingList(dishId: Int) {
        val currentList = _shoppingList.value.toMutableMap()
        val currentDishes = _dishesInShoppingList.value.toMutableSet()

        // Usuń danie ze wszystkich składników
        currentList.keys.toList().forEach { key ->
            val item = currentList[key]!!
            val updatedSources = item.dishSources.filter { it.dishId != dishId }

            if (updatedSources.isEmpty()) {
                currentList.remove(key)
            } else {
                val newTotalAmount = updatedSources.sumOf { it.amount.toDouble() }.toFloat()
                currentList[key] = item.copy(
                    totalAmount = newTotalAmount,
                    dishSources = updatedSources
                )
            }
        }

        currentDishes.remove(dishId)
        _shoppingList.value = currentList
        _dishesInShoppingList.value = currentDishes
    }

    fun toggleItemChecked(itemKey: String) {
        val currentList = _shoppingList.value.toMutableMap()
        val item = currentList[itemKey]
        if (item != null) {
            currentList[itemKey] = item.copy(isChecked = !item.isChecked)
            _shoppingList.value = currentList
        }
    }

    fun clearCheckedItems() {
        val currentList = _shoppingList.value.toMutableMap()
        val itemsToRemove = currentList.filter { it.value.isChecked }.keys
        itemsToRemove.forEach { currentList.remove(it) }
        _shoppingList.value = currentList
    }

    fun isInShoppingList(dishId: Int): Boolean {
        return _dishesInShoppingList.value.contains(dishId)
    }

    fun getShoppingListItems(): List<ShoppingListItem> {
        return _shoppingList.value.values.sortedBy { it.nazwa }
    }
}