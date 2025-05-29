package com.example.dietq_plus

import android.content.Context
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class UserSettingsRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    private val _userSettingsFlow = MutableStateFlow(getUserSettings())
    val userSettingsFlow: StateFlow<UserSettings> = _userSettingsFlow

    fun getUserSettings(): UserSettings {
        val numberOfMeals = sharedPreferences.getInt("number_of_meals", 5)
        val targetCalories = sharedPreferences.getInt("target_calories", 2000)
        return UserSettings(numberOfMeals, targetCalories)
    }

    fun saveUserSettings(userSettings: UserSettings) {
        sharedPreferences.edit()
            .putInt("number_of_meals", userSettings.numberOfMeals)
            .putInt("target_calories", userSettings.targetCalories)
            .apply()
        _userSettingsFlow.value = userSettings

    }
    companion object {
        @Volatile
        private var INSTANCE: UserSettingsRepository? = null

        fun getInstance(context: Context): UserSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserSettingsRepository.getInstance(application)
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = repository.userSettingsFlow

    private val _editingSettings = MutableStateFlow(repository.getUserSettings())
    val editingSettings: StateFlow<UserSettings> = _editingSettings

    init {
        _userSettings.value = repository.getUserSettings()
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
    fun updateNumberOfMeals(numberOfMeals: Int) {
        _editingSettings.value = _editingSettings.value.copy(numberOfMeals = numberOfMeals)
    }

    fun updateTargetCalories(targetCalories: Int) {
        _editingSettings.value = _editingSettings.value.copy(targetCalories = targetCalories)
    }

    fun saveSettings() {
        repository.saveUserSettings(_editingSettings.value)
    }
}