package com.example.dietq_plus.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dietq_plus.NutritionalValues


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