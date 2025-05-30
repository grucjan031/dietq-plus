package com.example.dietq_plus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EnhancedSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EnhancedUserSettingsRepository.getInstance(application)

    val userSettings: StateFlow<EnhancedUserSettings> = repository.userSettingsFlow

    private val _editingSettings = MutableStateFlow(repository.getUserSettings())
    val editingSettings: StateFlow<EnhancedUserSettings> = _editingSettings

    init {
        _editingSettings.value = repository.getUserSettings()
    }

    fun updateNumberOfMeals(numberOfMeals: Int) {
        _editingSettings.value = _editingSettings.value.copy(numberOfMeals = numberOfMeals)
    }

    fun updateTargetCalories(targetCalories: Int) {
        _editingSettings.value = _editingSettings.value.copy(targetCalories = targetCalories)
    }

    fun updateWeight(weight: Float) {
        _editingSettings.value = _editingSettings.value.copy(weight = weight)
    }

    fun updateHeight(height: Int) {
        _editingSettings.value = _editingSettings.value.copy(height = height)
    }

    fun updateAge(age: Int) {
        _editingSettings.value = _editingSettings.value.copy(age = age)
    }

    fun updateGender(gender: Gender) {
        _editingSettings.value = _editingSettings.value.copy(gender = gender)
    }

    fun updateActivityLevel(activityLevel: ActivityLevel) {
        _editingSettings.value = _editingSettings.value.copy(activityLevel = activityLevel)
    }

    fun updateUseCalculatedCalories(useCalculated: Boolean) {
        _editingSettings.value = _editingSettings.value.copy(useCalculatedCalories = useCalculated)
    }

    fun saveSettings() {
        repository.saveUserSettings(_editingSettings.value)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EnhancedSettingsViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
}