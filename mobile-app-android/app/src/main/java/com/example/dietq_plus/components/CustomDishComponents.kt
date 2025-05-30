package com.example.dietq_plus.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dietq_plus.CustomIngredient
import kotlin.math.roundToInt

@Composable
fun CustomIngredientsCard(
    customIngredients: List<CustomIngredient>,
    portions: Float,
    onIngredientAmountChange: (Int, Float) -> Unit,
    onResetIngredient: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Składniki (edytowalne)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (portions != 1.0f) {
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

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Kliknij Edytuj",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            customIngredients.forEachIndexed { index, customIngredient ->
                CustomIngredientRow(
                    customIngredient = customIngredient,
                    portions = portions,
                    onAmountChange = { newAmount ->
                        onIngredientAmountChange(index, newAmount)
                    },
                    onReset = {
                        onResetIngredient(index)
                    }
                )

                if (index < customIngredients.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CustomIngredientRow(
    customIngredient: CustomIngredient,
    portions: Float,
    onAmountChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val finalAmount = customIngredient.customAmount * portions

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (customIngredient.isCustomized) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (customIngredient.isCustomized) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customIngredient.originalIngredient.nazwa,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        if (customIngredient.isCustomized) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "ZMIENIONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${formatAmount(finalAmount)} ${customIngredient.originalIngredient.jednostka}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (customIngredient.isCustomized) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (portions != 1.0f || customIngredient.isCustomized) {
                        Text(
                            text = "oryg. ${customIngredient.originalIngredient.ilosc} ${customIngredient.originalIngredient.jednostka}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edytuj ilość",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edytuj", style = MaterialTheme.typography.bodySmall)
                    }

                    if (customIngredient.isCustomized) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onReset,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Przywróć oryginał",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Wartości odżywcze składnika
            val nutrition = customIngredient.adjustedNutritionalValues
            if (nutrition.kcal > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Wartości: ${(nutrition.kcal * portions).toInt()} kcal, " +
                                "B: ${String.format("%.1f", nutrition.bialko * portions)}g, " +
                                "W: ${String.format("%.1f", nutrition.weglowodany * portions)}g, " +
                                "T: ${String.format("%.1f", nutrition.tluszcze * portions)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        IngredientAmountDialog(
            ingredientName = customIngredient.originalIngredient.nazwa,
            currentAmount = customIngredient.customAmount,
            unit = customIngredient.originalIngredient.jednostka,
            originalAmount = customIngredient.originalIngredient.ilosc.toFloat(),
            onAmountConfirmed = { newAmount ->
                onAmountChange(newAmount)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun IngredientAmountDialog(
    ingredientName: String,
    currentAmount: Float,
    unit: String,
    originalAmount: Float,
    onAmountConfirmed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(currentAmount.toString()) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edytuj ilość składnika",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = ingredientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue
                        isError = newValue.toFloatOrNull() == null || newValue.toFloatOrNull()!! <= 0
                    },
                    label = { Text("Ilość ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Wprowadź poprawną liczbę większą od 0", color = MaterialTheme.colorScheme.error) }
                    } else {
                        { Text("Oryginalna ilość: $originalAmount $unit") }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Szybkie przyciski do często używanych wartości
                Text(
                    text = "Szybkie ustawienia:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickValues = listOf(
                        originalAmount * 0.5f to "50%",
                        originalAmount to "100%",
                        originalAmount * 1.5f to "150%",
                        originalAmount * 2f to "200%"
                    )

                    quickValues.forEach { (value, label) ->
                        FilterChip(
                            onClick = {
                                amountText = value.toString()
                                isError = false
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                            selected = amountText.toFloatOrNull() == value,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = {
                            amountText.toFloatOrNull()?.let { amount ->
                                if (amount > 0) {
                                    onAmountConfirmed(amount)
                                }
                            }
                        },
                        enabled = !isError && amountText.toFloatOrNull() != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zapisz")
                    }
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