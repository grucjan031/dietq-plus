package com.example.dietq_plus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import com.example.dietq_plus.screens.EnhancedDishDetailScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.dietq_plus.ui.theme.DietqplusTheme
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.RestaurantMenu
import com.example.dietq_plus.CustomIngredient
import com.example.dietq_plus.CustomizableDish
import com.example.dietq_plus.EnhancedSettingsViewModel
import com.example.dietq_plus.screens.EnhancedSettingsScreen
import com.example.dietq_plus.screens.ShoppingListScreen
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
import com.example.dietq_plus.PortionableDish
import com.example.dietq_plus.ShoppingListRepository

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
import com.example.dietq_plus.components.DailyMacroItem
import com.example.dietq_plus.components.MacronutrientsRow
import com.example.dietq_plus.components.calculateMacroPercentage
import com.example.dietq_plus.screens.DayDetailScreen
import com.example.dietq_plus.screens.MonthCalendarScreen
import com.example.dietq_plus.screens.SelectDishScreen
import com.example.dietq_plus.screens.SettingsScreen


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DietApp() {
    val dishViewModel: DishViewModel = viewModel()
    val mealPlanViewModel: MealPlanViewModel = viewModel(factory = MealPlanViewModel.Factory)

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route ?: "home"

    Scaffold(
        bottomBar = {
            Column {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Jadłospis") },
                        label = { Text("Jadłospis") },
                        selected = currentRoute.startsWith("home"),
                        onClick = { navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }}
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Kalendarz") },
                        label = { Text("Kalendarz") },
                        selected = currentRoute.startsWith("monthCalendar"),
                        onClick = { navController.navigate("monthCalendar") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }}
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Zakupy") },
                        label = { Text("Zakupy") },
                        selected = currentRoute.startsWith("shoppingList"),
                        onClick = { navController.navigate("shoppingList") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }}
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Ustawienia") },
                        label = { Text("Ustawienia") },
                        selected = currentRoute.startsWith("settings"),
                        onClick = { navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }}
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
        ) {
            composable("home") {
                DayDetailScreen(
                    date = LocalDate.now(),
                    viewModel = mealPlanViewModel,
                    dishViewModel = dishViewModel,
                    onBackClick = { },
                    onAddDishClick = { mealType ->
                        navController.navigate("selectDish/${LocalDate.now()}/${mealType.name}")
                    },
                    onDishClick = { dish ->
                        navController.navigate("dishDetail/${dish.id}")
                    }
                )
            }

            composable(
                "dayDetail/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val date = LocalDate.parse(dateStr)

                DayDetailScreen(
                    date = date,
                    viewModel = mealPlanViewModel,
                    dishViewModel = dishViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddDishClick = { mealType ->
                        navController.navigate("selectDish/${date}/${mealType.name}")
                    },
                    onDishClick = { dish ->
                        navController.navigate("dishDetail/${dish.id}")
                    }
                )
            }

            composable("monthCalendar") {
                MonthCalendarScreen(
                    viewModel = mealPlanViewModel,
                    onDayClick = { date ->
                        mealPlanViewModel.selectDate(date)
                        navController.navigate("dayDetail/${date}")
                    }
                )
            }

            composable("settings") {
                val enhancedSettingsViewModel: EnhancedSettingsViewModel = viewModel(factory = EnhancedSettingsViewModel.Factory)
                EnhancedSettingsScreen(viewModel = enhancedSettingsViewModel)
            }
            composable("shoppingList") {
                ShoppingListScreen()
            }
            composable(
                "dishDetail/{dishId}",
                arguments = listOf(navArgument("dishId") { type = NavType.IntType })
            ) { backStackEntry ->
                val dishId = backStackEntry.arguments?.getInt("dishId") ?: 0
                val dishes by dishViewModel.dishes.collectAsState()
                val dish = dishes.find { it.id == dishId }

                if (dish != null) {
                    EnhancedDishDetailScreen(
                        dish = dish,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable(
                "selectDish/{date}/{mealType}",
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType },
                    navArgument("mealType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val date = LocalDate.parse(dateStr)
                val mealTypeStr = backStackEntry.arguments?.getString("mealType") ?: MealType.SNIADANIE.name
                val mealType = MealType.valueOf(mealTypeStr)

                SelectDishScreen(
                    viewModel = dishViewModel,
                    onDishSelected = { dish ->
                        // ZMIANA: użyj nowej metody która automatycznie dodaje do listy zakupów
                        mealPlanViewModel.assignDishToMealAndShoppingList(date, mealType, dish)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() },
                    onDishClick = { dish ->
                        navController.navigate("dishDetail/${dish.id}")
                    }
                )
            }
        }
    }
}





data class UserSettings(
    val numberOfMeals: Int = 5,
    val targetCalories: Int = 2000
)

// Komponent do wyświetlania zdjęcia dania z API
@Composable
fun DishImage(
    dishId: Int,
    hasImage: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val imageUrl = if (hasImage) {
        "http://192.168.0.126:5000/api/photos/dish_${dishId}.jpg"
    } else {
        "http://192.168.0.126:5000/api/photos/no-image.jpg"
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Zdjęcie dania",
        modifier = modifier,
        contentScale = contentScale
    )
}



@Composable
fun DailyNutritionSummary(
    dailyNutrition: NutritionalValues,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Podsumowanie dnia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kalorie",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${dailyNutrition.kcal.toInt()} / $targetCalories kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = (dailyNutrition.kcal.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Makroskładniki",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DailyMacroItem(
                    label = "Białko",
                    value = "${String.format("%.1f", dailyNutrition.bialko)}g",
                    percentage = calculateMacroPercentage(dailyNutrition.bialko * 4, dailyNutrition.kcal),
                    color = MaterialTheme.colorScheme.primary
                )
                DailyMacroItem(
                    label = "Węglowodany",
                    value = "${String.format("%.1f", dailyNutrition.weglowodany)}g",
                    percentage = calculateMacroPercentage(dailyNutrition.weglowodany * 4, dailyNutrition.kcal),
                    color = MaterialTheme.colorScheme.secondary
                )
                DailyMacroItem(
                    label = "Tłuszcze",
                    value = "${String.format("%.1f", dailyNutrition.tluszcze)}g",
                    percentage = calculateMacroPercentage(dailyNutrition.tluszcze * 9, dailyNutrition.kcal),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietqplusTheme {
                DietApp()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DietqplusTheme {
        Greeting("Android")
    }
}