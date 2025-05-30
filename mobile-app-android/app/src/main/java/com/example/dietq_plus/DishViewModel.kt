package com.example.dietq_plus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DishViewModel : ViewModel() {

    private val _dishes = MutableStateFlow<List<Dish>>(emptyList())
    val dishes: StateFlow<List<Dish>> = _dishes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchDishes()
    }

    fun fetchDishes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _dishes.value = RetrofitInstance.apiService.getAllDishes()
            } catch (e: Exception) {
                _error.value = "Błąd pobierania danych: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}