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
    //val userSettingsRepository = UserSettingsRepository(LocalContext.current)
    //val userSettings by userSettingsRepository.userSettingsFlow.collectAsState()

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
                    onBackClick = { },  // Nie jest potrzebne na Home
                    onAddDishClick = { mealType ->
                        navController.navigate("selectDish/${LocalDate.now()}/${mealType.name}")
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

            // Istniejące ekrany
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
                    onBackClick = { navController.popBackStack() }
                )
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
    val editingSettings by viewModel.editingSettings.collectAsState() // Lokalny stan edycji
    val context = LocalContext.current

    // Stan dla wartości w TextField, aby umożliwić wpisywanie tekstu
    var targetCaloriesInput by remember { mutableStateOf(editingSettings.targetCalories.toString()) }

    // Synchronizacja targetCaloriesInput z editingSettings.targetCalories, gdy zmienia się z innego źródła (np. suwak)
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

        // Liczba posiłków
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
                    modifier = Modifier.weight(1f), // Aby przyciski miały równą szerokość
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (editingSettings.numberOfMeals == i) {
                            MaterialTheme.colorScheme.primaryContainer // Kolor podświetlenia
                        } else {
                            Color.Transparent // Domyślny kolor tła
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
                            MaterialTheme.colorScheme.primary // Grubsza lub inna ramka dla zaznaczonego
                        } else {
                            MaterialTheme.colorScheme.outline // Domyślna ramka
                        }
                    )
                ) {
                    Text("$i")
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Docelowa liczba kalorii
        Text(
            text = "Docelowa liczba kalorii: ${editingSettings.targetCalories} kcal",
            style = MaterialTheme.typography.titleMedium
        )
// Pole tekstowe do wprowadzania kalorii
        OutlinedTextField(
            value = targetCaloriesInput,
            onValueChange = { newValue ->
                targetCaloriesInput = newValue
                // Próbujemy zaktualizować ViewModel tylko jeśli wartość jest poprawną liczbą
                newValue.toIntOrNull()?.let {
                    // Ograniczamy wartość do zakresu suwaka
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
                // Aktualizuj również targetCaloriesInput, aby pole tekstowe odzwierciedlało zmianę suwaka
                targetCaloriesInput = it.toInt().toString()
            },
            valueRange = 1000f..6500f,
            steps = (6500-1000)/25 -1, // Poprawiona liczba kroków
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.nazwa,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(text = dish.opis, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = "Kalorie: ${dish.wartosci_odzywcze.kcal} kcal")
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
            // Dni tygodnia
            items(7) { dayOfWeek ->
                val day = DayOfWeek.of(dayOfWeek + 1)
                Text(
                    text = day.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pl")),
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }

            // Puste miejsca przed pierwszym dniem miesiąca
            val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
            items(firstDayOfMonth - 1) {
                Box(modifier = Modifier.padding(8.dp))
            }

            // Dni miesiąca
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
fun DayDetailScreen(
    date: LocalDate,
    viewModel: MealPlanViewModel,
    dishViewModel: DishViewModel,
    onBackClick: () -> Unit,
    onAddDishClick: (MealType) -> Unit
) {
    val monthPlans by viewModel.monthPlans.collectAsState()
    val dayPlan = monthPlans[date] ?: DayPlan(date)
    val availableMealTypes by viewModel.availableMealTypes.collectAsState()

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
                .padding(16.dp)
        ) {
            availableMealTypes.forEach { mealType ->
                item {
                    MealCard(
                        mealType = mealType,
                        meal = dayPlan.meals[mealType] ?: Meal(mealType),
                        onAddDishClick = { onAddDishClick(mealType) },
                        onRemoveDishClick = { dishIndex ->
                            viewModel.removeDishFromMeal(date, mealType, dishIndex)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
    onRemoveDishClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (mealType) {
                    MealType.SNIADANIE -> "Śniadanie"
                    MealType.DRUGIE_SNIADANIE -> "Drugie śniadanie"
                    MealType.OBIAD -> "Obiad"
                    MealType.PODWIECZOREK -> "Podwieczorek"
                    MealType.KOLACJA -> "Kolacja"
                },
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (meal.dishes.isNotEmpty()) {
                Column {
                    meal.dishes.forEachIndexed { index, dish ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dish.nazwa,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "Kalorie: ${dish.wartosci_odzywcze.kcal} kcal")
                            }

                            IconButton(onClick = { onRemoveDishClick(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń")
                            }
                        }
                        if (index < meal.dishes.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
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
    onBackClick: () -> Unit
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
                        DishSelectItem(dish = dish, onClick = { onDishSelected(dish) })
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.nazwa,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Kalorie: ${dish.wartosci_odzywcze.kcal} kcal")
            }
            Icon(Icons.Default.Add, contentDescription = "Wybierz")
        }
    }
}