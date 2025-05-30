package com.example.dietq_plus.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dietq_plus.ActivityLevel
import com.example.dietq_plus.EnhancedSettingsViewModel
import com.example.dietq_plus.Gender

@Composable
fun EnhancedSettingsScreen(
    viewModel: EnhancedSettingsViewModel = viewModel()
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val editingSettings by viewModel.editingSettings.collectAsState()
    val context = LocalContext.current

    var weightInput by remember { mutableStateOf(editingSettings.weight.toString()) }
    var heightInput by remember { mutableStateOf(editingSettings.height.toString()) }
    var ageInput by remember { mutableStateOf(editingSettings.age.toString()) }
    var targetCaloriesInput by remember { mutableStateOf(editingSettings.targetCalories.toString()) }

    LaunchedEffect(editingSettings) {
        weightInput = editingSettings.weight.toString()
        heightInput = editingSettings.height.toString()
        ageInput = editingSettings.age.toString()
        targetCaloriesInput = editingSettings.targetCalories.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ustawienia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sekcja podstawowych informacji
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dane osobowe",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Płeć
                    Text(
                        text = "Płeć",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Gender.values().forEach { gender ->
                            FilterChip(
                                onClick = { viewModel.updateGender(gender) },
                                label = { Text(gender.displayName) },
                                selected = editingSettings.gender == gender,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Wiek
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { newValue ->
                            ageInput = newValue
                            newValue.toIntOrNull()?.let {
                                val clampedValue = it.coerceIn(16, 100)
                                viewModel.updateAge(clampedValue)
                            }
                        },
                        label = { Text("Wiek (lat)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Wzrost
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { newValue ->
                            heightInput = newValue
                            newValue.toIntOrNull()?.let {
                                val clampedValue = it.coerceIn(120, 250)
                                viewModel.updateHeight(clampedValue)
                            }
                        },
                        label = { Text("Wzrost (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Waga
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { newValue ->
                            weightInput = newValue
                            newValue.toFloatOrNull()?.let {
                                val clampedValue = it.coerceIn(30f, 300f)
                                viewModel.updateWeight(clampedValue)
                            }
                        },
                        label = { Text("Waga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // BMI Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BMI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = String.format("%.1f", editingSettings.bmi),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = editingSettings.bmiCategory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Poziom aktywności
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Poziom aktywności fizycznej",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ActivityLevel.values().forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = editingSettings.activityLevel == level,
                                    onClick = { viewModel.updateActivityLevel(level) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = editingSettings.activityLevel == level,
                                onClick = { viewModel.updateActivityLevel(level) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = level.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Kalorie
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dzienne zapotrzebowanie kaloryczne",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Przełącznik między obliczonymi a ręcznymi kaloriami
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Automatyczne obliczanie")
                        Switch(
                            checked = editingSettings.useCalculatedCalories,
                            onCheckedChange = { viewModel.updateUseCalculatedCalories(it) }
                        )
                    }

                    if (editingSettings.useCalculatedCalories) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Obliczone zapotrzebowanie:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${editingSettings.calculatedCalories} kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wzór Harris-Benedict",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = targetCaloriesInput,
                            onValueChange = { newValue ->
                                targetCaloriesInput = newValue
                                newValue.toIntOrNull()?.let {
                                    val clampedValue = it.coerceIn(1000, 6500)
                                    viewModel.updateTargetCalories(clampedValue)
                                }
                            },
                            label = { Text("Docelowe kalorie") },
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
                            Text("1000 kcal", style = MaterialTheme.typography.bodySmall)
                            Text("6500 kcal", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Liczba posiłków
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Liczba posiłków dziennie: ${editingSettings.numberOfMeals}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                }
            }
        }

        // Przycisk zapisania
        item {
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Zapisz ustawienia")
            }
        }
    }
}