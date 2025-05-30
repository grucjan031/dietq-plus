package com.example.dietq_plus.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dietq_plus.AdjustedIngredient
import kotlin.math.roundToInt

@Composable
fun PortionSelector(
    currentPortions: Float,
    onPortionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var portionText by remember(currentPortions) { mutableStateOf(currentPortions.toString()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Liczba porcji",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kontrolki do zmiany porcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przycisk minus
                IconButton(
                    onClick = {
                        val newPortions = (currentPortions - 0.5f).coerceAtLeast(0.5f)
                        onPortionChange(newPortions)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Zmniejsz porcje",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Pole tekstowe do wprowadzania wartości
                OutlinedTextField(
                    value = portionText,
                    onValueChange = { newValue ->
                        portionText = newValue
                        newValue.toFloatOrNull()?.let { portions ->
                            val clampedPortions = portions.coerceIn(0.5f, 10f)
                            onPortionChange(clampedPortions)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    suffix = {
                        Text(
                            "porcji",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )

                // Przycisk plus
                IconButton(
                    onClick = {
                        val newPortions = (currentPortions + 0.5f).coerceAtMost(10f)
                        onPortionChange(newPortions)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zwiększ porcje",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Suwak do precyzyjnej regulacji
            Slider(
                value = currentPortions,
                onValueChange = onPortionChange,
                valueRange = 0.5f..10f,
                steps = 19, // 0.5, 1.0, 1.5, ..., 10.0
                modifier = Modifier.fillMaxWidth()
            )

            // Etykiety suwaka
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "10.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AdjustedIngredientsCard(
    adjustedIngredients: List<AdjustedIngredient>,
    portions: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Składniki",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "${portions} porcji",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            adjustedIngredients.forEach { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    showOriginal = portions != 1.0f
                )
                if (ingredient != adjustedIngredients.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun IngredientRow(
    ingredient: AdjustedIngredient,
    showOriginal: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ingredient.nazwa,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatAmount(ingredient.adjustedAmount)} ${ingredient.jednostka}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (showOriginal && ingredient.adjustedAmount != ingredient.originalAmount.toFloat()) {
                    Text(
                        text = "oryg. ${ingredient.originalAmount} ${ingredient.jednostka}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Float): String {
    return if (amount == amount.roundToInt().toFloat()) {
        amount.roundToInt().toString()
    } else {
        String.format("%.1f", amount)
    }
}