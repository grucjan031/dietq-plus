package com.example.dietq_plus.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dietq_plus.Dish
import com.example.dietq_plus.DishImage
import com.example.dietq_plus.Meal
import com.example.dietq_plus.MealType
import com.example.dietq_plus.NutritionalValues

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