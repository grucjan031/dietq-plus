package com.example.dietq_plus.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.dietq_plus.ui.theme.DietqplusTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.navArgument
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dietq_plus.DailyNutritionSummary
import com.example.dietq_plus.DayPlan
import com.example.dietq_plus.Dish
import com.example.dietq_plus.DishViewModel
import com.example.dietq_plus.Meal
import com.example.dietq_plus.MealPlanViewModel
import com.example.dietq_plus.MealType
import com.example.dietq_plus.components.MealCard

@Composable
fun DayDetailScreen(
    date: LocalDate,
    viewModel: MealPlanViewModel,
    dishViewModel: DishViewModel,
    onBackClick: () -> Unit,
    onAddDishClick: (MealType) -> Unit,
    onDishClick: ((Dish) -> Unit)? = null
) {
    val monthPlans by viewModel.monthPlans.collectAsState()
    val dayPlan = monthPlans[date] ?: DayPlan(date)
    val availableMealTypes by viewModel.availableMealTypes.collectAsState()
    val targetCalories by viewModel.targetCalories.collectAsState()

    val dailyNutrition = viewModel.calculateDailyNutrition(date)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 16.dp, end = 16.dp, bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (date != LocalDate.now()) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                }
            }

            Text(
                text = "Plan na ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                // Dodanie paddingu na dole dla dolnego paska nawigacji
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DailyNutritionSummary(
                    dailyNutrition = dailyNutrition,
                    targetCalories = targetCalories
                )
            }

            availableMealTypes.forEach { mealType ->
                item {
                    MealCard(
                        mealType = mealType,
                        meal = dayPlan.meals[mealType] ?: Meal(mealType),
                        onAddDishClick = { onAddDishClick(mealType) },
                        onRemoveDishClick = { dishIndex ->
                            viewModel.removeDishFromMealAndShoppingList(date, mealType, dishIndex, true)
                        },
                        onDishClick = onDishClick
                    )
                }
            }
        }
    }
}