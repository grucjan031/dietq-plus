package com.example.dietq_plus

import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    LOW("Niska (siedzący tryb życia)", 1.2),
    MODERATE("Średnia (umiarkowana aktywność)", 1.55),
    HIGH("Wysoka (dużo ruchu / sport codziennie)", 1.725)
}

enum class Gender(val displayName: String) {
    MALE("Mężczyzna"),
    FEMALE("Kobieta")
}

data class EnhancedUserSettings(
    val numberOfMeals: Int = 5,
    val targetCalories: Int = 2000,
    val weight: Float = 70f, // kg
    val height: Int = 170, // cm
    val age: Int = 30, // lat
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val useCalculatedCalories: Boolean = false
) {
    val bmi: Float
        get() = weight / (height / 100f).pow(2)

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "Niedowaga"
            bmi < 25f -> "Waga prawidłowa"
            bmi < 30f -> "Nadwaga"
            else -> "Otyłość"
        }

    val calculatedCalories: Int
        get() {
            // Wzór Harris-Benedict
            val bmr = when (gender) {
                Gender.MALE -> 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
                Gender.FEMALE -> 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
            }
            return (bmr * activityLevel.multiplier).toInt()
        }

    val effectiveTargetCalories: Int
        get() = if (useCalculatedCalories) calculatedCalories else targetCalories
}

class EnhancedUserSettingsRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("enhanced_user_settings", Context.MODE_PRIVATE)

    private val _userSettingsFlow = MutableStateFlow(getUserSettings())
    val userSettingsFlow: StateFlow<EnhancedUserSettings> = _userSettingsFlow

    fun getUserSettings(): EnhancedUserSettings {
        return EnhancedUserSettings(
            numberOfMeals = sharedPreferences.getInt("number_of_meals", 5),
            targetCalories = sharedPreferences.getInt("target_calories", 2000),
            weight = sharedPreferences.getFloat("weight", 70f),
            height = sharedPreferences.getInt("height", 170),
            age = sharedPreferences.getInt("age", 30),
            gender = Gender.valueOf(sharedPreferences.getString("gender", Gender.MALE.name) ?: Gender.MALE.name),
            activityLevel = ActivityLevel.valueOf(sharedPreferences.getString("activity_level", ActivityLevel.MODERATE.name) ?: ActivityLevel.MODERATE.name),
            useCalculatedCalories = sharedPreferences.getBoolean("use_calculated_calories", false)
        )
    }

    fun saveUserSettings(userSettings: EnhancedUserSettings) {
        sharedPreferences.edit()
            .putInt("number_of_meals", userSettings.numberOfMeals)
            .putInt("target_calories", userSettings.targetCalories)
            .putFloat("weight", userSettings.weight)
            .putInt("height", userSettings.height)
            .putInt("age", userSettings.age)
            .putString("gender", userSettings.gender.name)
            .putString("activity_level", userSettings.activityLevel.name)
            .putBoolean("use_calculated_calories", userSettings.useCalculatedCalories)
            .apply()
        _userSettingsFlow.value = userSettings
    }

    companion object {
        @Volatile
        private var INSTANCE: EnhancedUserSettingsRepository? = null

        fun getInstance(context: Context): EnhancedUserSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EnhancedUserSettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}