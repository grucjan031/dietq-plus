package com.example.dietq_plus.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dietq_plus.SettingsViewModel

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
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 16.dp, end = 16.dp, bottom = 16.dp),
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