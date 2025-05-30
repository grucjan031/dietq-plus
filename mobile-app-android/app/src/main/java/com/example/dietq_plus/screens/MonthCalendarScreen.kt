package com.example.dietq_plus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dietq_plus.MealPlanViewModel
import com.example.dietq_plus.components.CalendarDayItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthCalendarScreen(
    viewModel: MealPlanViewModel = viewModel(),
    onDayClick: (LocalDate) -> Unit
) {
    val monthPlans by viewModel.monthPlans.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Stan dla aktualnie wyświetlanego miesiąca
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    // Własne skróty nazw dni tygodnia
    val shortDayNames = remember {
        mapOf(
            DayOfWeek.MONDAY to "pon.",
            DayOfWeek.TUESDAY to "wt.",
            DayOfWeek.WEDNESDAY to "śr.",
            DayOfWeek.THURSDAY to "czw.",
            DayOfWeek.FRIDAY to "pt.",
            DayOfWeek.SATURDAY to "sob.",
            DayOfWeek.SUNDAY to "nied."
        )
    }

    // Formatowanie nazwy miesiąca
    val monthFormatter = remember { DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl")) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 16.dp, end = 16.dp, bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kalendarz",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Nawigacja między miesiącami
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentYearMonth = currentYearMonth.minusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Poprzedni miesiąc"
                )
            }

            Text(
                text = currentYearMonth.format(monthFormatter).capitalize(),
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                currentYearMonth = currentYearMonth.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Następny miesiąc"
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(7) { dayOfWeek ->
                val day = DayOfWeek.of(dayOfWeek + 1)
                Text(
                    text = shortDayNames[day] ?: "",
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }

            val firstDayOfMonth = currentYearMonth.atDay(1).dayOfWeek.value
            items(firstDayOfMonth - 1) {
                Box(modifier = Modifier.padding(8.dp))
            }

            items(currentYearMonth.lengthOfMonth()) { dayOfMonth ->
                val date = currentYearMonth.atDay(dayOfMonth + 1)
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

// Funkcja pomocnicza do zmiany pierwszej litery na wielką
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}