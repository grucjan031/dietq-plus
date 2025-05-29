package com.example.dietq_plus

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


@Composable
fun DietApp() {
    val dishViewModel: DishViewModel = viewModel()
    val mealPlanViewModel: MealPlanViewModel = viewModel(factory = MealPlanViewModel.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route ?: "home"

    val currentDate = LocalDate.now()
    val currentCalories by mealPlanViewModel.currentDateCalories.collectAsState()
    val targetCalories by mealPlanViewModel.targetCalories.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                CaloriesProgressBar(
                    currentCalories = currentCalories,
                    targetCalories = targetCalories
                )
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
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
                val settingsViewModel: SettingsViewModel = viewModel()
                SettingsScreen(viewModel = settingsViewModel)
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
                        mealPlanViewModel.assignDishToMeal(date, mealType, dish)
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() },
                    onDishClick = { dish ->
                        navController.navigate("dishDetail/${dish.id}")
                    }
                )
            }

            composable(
                "dishDetail/{dishId}",
                arguments = listOf(navArgument("dishId") { type = NavType.IntType })
            ) { backStackEntry ->
                val dishId = backStackEntry.arguments?.getInt("dishId") ?: 0
                val dishes by dishViewModel.dishes.collectAsState()
                val dish = dishes.find { it.id == dishId }

                if (dish != null) {
                    DishDetailScreen(
                        dish = dish,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CaloriesProgressBar(
    currentCalories: Int,
    targetCalories: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Kalorie: $currentCalories / $targetCalories kcal")
            Text("${(currentCalories.toFloat() / targetCalories.toFloat() * 100).toInt()}%")
        }

        LinearProgressIndicator(
            progress = (currentCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val editingSettings by viewModel.editingSettings.collectAsState()
    val context = LocalContext.current

    var targetCaloriesInput by remember { mutableStateOf(editingSettings.targetCalories.toString()) }

    LaunchedEffect(editingSettings.targetCalories) {
        if (editingSettings.targetCalories.toString() != targetCaloriesInput) {
            targetCaloriesInput = editingSettings.targetCalories.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Liczba posiłków: ${userSettings.numberOfMeals}",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 3..5) {
                OutlinedButton(
                    onClick = { viewModel.updateNumberOfMeals(i) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (editingSettings.numberOfMeals == i) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (editingSettings.numberOfMeals == i) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (editingSettings.numberOfMeals == i) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Text("$i")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Docelowa liczba kalorii: ${editingSettings.targetCalories} kcal",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = targetCaloriesInput,
            onValueChange = { newValue ->
                targetCaloriesInput = newValue
                newValue.toIntOrNull()?.let {
                    val clampedValue = it.coerceIn(1000, 6500)
                    viewModel.updateTargetCalories(clampedValue)
                }
            },
            label = { Text("Wpisz kalorie") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Slider(
            value = editingSettings.targetCalories.toFloat(),
            onValueChange = {
                viewModel.updateTargetCalories(it.toInt())
                targetCaloriesInput = it.toInt().toString()
            },
            valueRange = 1000f..6500f,
            steps = (6500-1000)/25 -1,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1000 kcal")
            Text("6500 kcal")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveSettings() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Zapisz ustawienia")
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
        "http://192.168.55.107:5000/api/photos/dish_${dishId}.jpg"
    } else {
        "http://192.168.55.107:5000/api/photos/no-image.jpg"
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
fun MacronutrientsRow(
    nutritionalValues: NutritionalValues,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    Column(modifier = modifier) {
        if (showTitle) {
            Text(
                text = "Makroskładniki",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MacronutrientItem(
                label = "Białko",
                value = "${String.format("%.1f", nutritionalValues.bialko)}g",
                color = MaterialTheme.colorScheme.primary
            )
            MacronutrientItem(
                label = "Węglowodany",
                value = "${String.format("%.1f", nutritionalValues.weglowodany)}g",
                color = MaterialTheme.colorScheme.secondary
            )
            MacronutrientItem(
                label = "Tłuszcze",
                value = "${String.format("%.1f", nutritionalValues.tluszcze)}g",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun MacronutrientItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailedNutritionalInfo(
    nutritionalValues: NutritionalValues,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wartości odżywcze",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kalorie",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${nutritionalValues.kcal.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            NutritionalRow(
                label = "Białko",
                value = "${String.format("%.1f", nutritionalValues.bialko)}g",
                color = MaterialTheme.colorScheme.primary
            )
            NutritionalRow(
                label = "Węglowodany",
                value = "${String.format("%.1f", nutritionalValues.weglowodany)}g",
                color = MaterialTheme.colorScheme.secondary
            )
            NutritionalRow(
                label = "Tłuszcze",
                value = "${String.format("%.1f", nutritionalValues.tluszcze)}g",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun NutritionalRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun DishListItem(dish: Dish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Zdjęcie dania
            DishImage(
                dishId = dish.id,
                hasImage = dish.ma_zdjecie,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.nazwa,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dish.opis,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kalorie: ${dish.wartosci_odzywcze.kcal.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                MacronutrientsRow(
                    nutritionalValues = dish.wartosci_odzywcze,
                    showTitle = false
                )
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MonthCalendarScreen(
    viewModel: MealPlanViewModel = viewModel(),
    onDayClick: (LocalDate) -> Unit
) {
    val monthPlans by viewModel.monthPlans.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth = remember { YearMonth.now() }

    Column(modifier = Modifier.fillMaxSize()) {
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(title = { Text("Plan posiłków - ${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("pl"))}") })

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(7) { dayOfWeek ->
                val day = DayOfWeek.of(dayOfWeek + 1)
                Text(
                    text = day.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pl")),
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }

            val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
            items(firstDayOfMonth - 1) {
                Box(modifier = Modifier.padding(8.dp))
            }

            items(currentMonth.lengthOfMonth()) { dayOfMonth ->
                val date = currentMonth.atDay(dayOfMonth + 1)
                val isSelected = selectedDate == date
                val dayPlan = monthPlans[date]

                CalendarDayItem(
                    date = date,
                    isSelected = isSelected,
                    hasPlans = dayPlan?.meals?.any { it.value.dishes.isNotEmpty() } ?: false,
                    onClick = { onDayClick(date) }
                )
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    hasPlans: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (hasPlans) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
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

@Composable
fun DailyMacroItem(
    label: String,
    value: String,
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = (percentage / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .width(60.dp)
                .height(4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
        )
    }
}

fun calculateMacroPercentage(macroCalories: Double, totalCalories: Double): Float {
    return if (totalCalories > 0) {
        ((macroCalories / totalCalories) * 100).toFloat()
    } else {
        0f
    }
}

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

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Plan na ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                            viewModel.removeDishFromMeal(date, mealType, dishIndex)
                        },
                        onDishClick = onDishClick
                    )
                }
            }
        }
    }
}

@Composable
fun MealCard(
    mealType: MealType,
    meal: Meal,
    onAddDishClick: () -> Unit,
    onRemoveDishClick: (Int) -> Unit,
    onDishClick: ((Dish) -> Unit)? = null
) {
    val totalNutrition = meal.dishes.fold(
        NutritionalValues(0.0, 0.0, 0.0, 0.0)
    ) { acc, dish ->
        NutritionalValues(
            kcal = acc.kcal + dish.wartosci_odzywcze.kcal,
            bialko = acc.bialko + dish.wartosci_odzywcze.bialko,
            weglowodany = acc.weglowodany + dish.wartosci_odzywcze.weglowodany,
            tluszcze = acc.tluszcze + dish.wartosci_odzywcze.tluszcze
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (mealType) {
                        MealType.SNIADANIE -> "Śniadanie"
                        MealType.DRUGIE_SNIADANIE -> "Drugie śniadanie"
                        MealType.OBIAD -> "Obiad"
                        MealType.PODWIECZOREK -> "Podwieczorek"
                        MealType.KOLACJA -> "Kolacja"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (meal.dishes.isNotEmpty()) {
                    Text(
                        text = "${totalNutrition.kcal.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (meal.dishes.isNotEmpty()) {
                Column {
                    meal.dishes.forEachIndexed { index, dish ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (onDishClick != null) {
                                        Modifier.clickable { onDishClick(dish) }
                                    } else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Zdjęcie dania w posiłku
                                DishImage(
                                    dishId = dish.id,
                                    hasImage = dish.ma_zdjecie,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dish.nazwa,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Kalorie: ${dish.wartosci_odzywcze.kcal.toInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "B: ${String.format("%.1f", dish.wartosci_odzywcze.bialko)}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "W: ${String.format("%.1f", dish.wartosci_odzywcze.weglowodany)}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "T: ${String.format("%.1f", dish.wartosci_odzywcze.tluszcze)}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { onRemoveDishClick(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        if (index < meal.dishes.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (meal.dishes.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Podsumowanie posiłku",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                MacronutrientsRow(
                                    nutritionalValues = totalNutrition,
                                    showTitle = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onAddDishClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dodaj danie")
            }
        }
    }
}

@Composable
fun SelectDishScreen(
    viewModel: DishViewModel = viewModel(),
    onDishSelected: (Dish) -> Unit,
    onBackClick: () -> Unit,
    onDishClick: ((Dish) -> Unit)? = null
) {
    val dishes by viewModel.dishes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Wybierz danie") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchDishes() }) {
                        Text("Odśwież")
                    }
                }
            } else {
                LazyColumn {
                    items(dishes) { dish ->
                        EnhancedDishSelectItem(
                            dish = dish,
                            onSelectClick = { onDishSelected(dish) },
                            onDetailClick = if (onDishClick != null) {
                                { onDishClick(dish) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedDishSelectItem(
    dish: Dish,
    onSelectClick: () -> Unit,
    onDetailClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Zdjęcie dania
                DishImage(
                    dishId = dish.id,
                    hasImage = dish.ma_zdjecie,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dish.nazwa,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dish.opis,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${dish.wartosci_odzywcze.kcal.toInt()} kcal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MacronutrientsRow(
                nutritionalValues = dish.wartosci_odzywcze,
                showTitle = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Dodaj",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dodaj")
                }

                if (onDetailClick != null) {
                    OutlinedButton(
                        onClick = onDetailClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Szczegóły",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Szczegóły")
                    }
                }
            }
        }
    }
}

@Composable
fun DishSelectItem(dish: Dish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DishImage(
                dishId = dish.id,
                hasImage = dish.ma_zdjecie,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.nazwa,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Kalorie: ${dish.wartosci_odzywcze.kcal.toInt()} kcal")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "B: ${String.format("%.1f", dish.wartosci_odzywcze.bialko)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "W: ${String.format("%.1f", dish.wartosci_odzywcze.weglowodany)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "T: ${String.format("%.1f", dish.wartosci_odzywcze.tluszcze)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.Add, contentDescription = "Wybierz")
        }
    }
}

@Composable
fun DishDetailScreen(
    dish: Dish,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(dish.nazwa) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Zdjęcie dania
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    DishImage(
                        dishId = dish.id,
                        hasImage = dish.ma_zdjecie,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Opis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dish.opis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                DetailedNutritionalInfo(
                    nutritionalValues = dish.wartosci_odzywcze
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Składniki",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        dish.skladniki.forEach { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ingredient.nazwa,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${ingredient.ilosc} ${ingredient.jednostka}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sposób przygotowania",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dish.sposob_przygotowania,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        DietApp()
                    }
                }
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