package com.example.dietq_plus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dietq_plus.Dish
import com.example.dietq_plus.DishImage
import com.example.dietq_plus.CustomDishViewModel
import com.example.dietq_plus.components.DetailedNutritionalInfo
import com.example.dietq_plus.components.PortionSelector
import com.example.dietq_plus.components.CustomIngredientsCard

@Composable
fun EnhancedDishDetailScreen(
    dish: Dish,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    customDishViewModel: CustomDishViewModel = viewModel()
) {
    var showAdvancedControls by remember { mutableStateOf(false) }
    val customizableDish by customDishViewModel.customizableDish.collectAsState()
    val isInShoppingList by customDishViewModel.isInShoppingList.collectAsState()

    LaunchedEffect(dish) {
        customDishViewModel.setDish(dish)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text(dish.nazwa)
                        if (showAdvancedControls && customizableDish?.hasCustomizations == true) {
                            Text(
                                text = "Dostosowane",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    // Przycisk do włączania/wyłączania zaawansowanych kontroli
                    IconButton(
                        onClick = { showAdvancedControls = !showAdvancedControls }
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Zaawansowane opcje",
                            tint = if (showAdvancedControls) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Przycisk resetowania (tylko gdy są dostosowania)
                    customizableDish?.let { current ->
                        if (current.hasCustomizations && showAdvancedControls) {
                            IconButton(
                                onClick = {
                                    customDishViewModel.resetAllCustomizations()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Resetuj wszystkie dostosowania",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Przycisk listy zakupów
                    IconButton(
                        onClick = {
                            if (isInShoppingList) {
                                customDishViewModel.removeFromShoppingList()
                            } else {
                                customDishViewModel.addToShoppingList()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = if (isInShoppingList) "Usuń z listy zakupów" else "Dodaj do listy zakupów",
                            tint = if (isInShoppingList) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        customizableDish?.let { current ->
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
                    // Opis dania
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

                // Kontrola porcji (pokazywana warunkowo)
                if (showAdvancedControls) {
                    item {
                        PortionSelector(
                            currentPortions = current.portions,
                            onPortionChange = { portions ->
                                customDishViewModel.updatePortions(portions)
                            }
                        )
                    }
                }

                item {
                    // Wartości odżywcze - dynamicznie przeliczane lub podstawowe
                    DetailedNutritionalInfo(
                        nutritionalValues = if (showAdvancedControls) {
                            current.totalNutritionalValues
                        } else {
                            dish.wartosci_odzywcze
                        }
                    )
                }

                item {
                    // Składniki - edytowalne lub podstawowe
                    if (showAdvancedControls) {
                        CustomIngredientsCard(
                            customIngredients = current.customIngredients,
                            portions = current.portions,
                            onIngredientAmountChange = { index, amount ->
                                customDishViewModel.updateIngredientAmount(index, amount)
                            },
                            onResetIngredient = { index ->
                                customDishViewModel.resetIngredient(index)
                            }
                        )
                    } else {
                        // Podstawowa lista składników z wskazówką
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Składniki",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Text(
                                            text = "Kliknij ${Icons.Default.Tune.name} aby edytować",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

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
                }

                item {
                    // Sposób przygotowania
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

                // Informacja o dostosowaniach na dole
                if (showAdvancedControls && current.hasCustomizations) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ℹ️ Dostosowania aktywne",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "To danie zostało dostosowane. Wartości odżywcze i składniki zostały przeliczone zgodnie z Twoimi zmianami.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                if (current.portions != 1.0f) {
                                    Text(
                                        text = "• Porcje: ${current.portions}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                val customizedCount = current.customIngredients.count { it.isCustomized }
                                if (customizedCount > 0) {
                                    Text(
                                        text = "• Dostosowane składniki: $customizedCount",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}