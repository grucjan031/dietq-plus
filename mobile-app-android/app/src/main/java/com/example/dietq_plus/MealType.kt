package com.example.dietq_plus

import java.time.LocalDate

enum class MealType {
    SNIADANIE,
    DRUGIE_SNIADANIE,
    OBIAD,
    PODWIECZOREK,
    KOLACJA
}

data class Meal(
    val type: MealType,
    val dishes: MutableList<Dish> = mutableListOf()
)

data class DayPlan(
    val date: LocalDate,
    val meals: MutableMap<MealType, Meal> = mutableMapOf(
        MealType.SNIADANIE to Meal(MealType.SNIADANIE),
        MealType.DRUGIE_SNIADANIE to Meal(MealType.DRUGIE_SNIADANIE),
        MealType.OBIAD to Meal(MealType.OBIAD),
        MealType.PODWIECZOREK to Meal(MealType.PODWIECZOREK),
        MealType.KOLACJA to Meal(MealType.KOLACJA)
    )
)