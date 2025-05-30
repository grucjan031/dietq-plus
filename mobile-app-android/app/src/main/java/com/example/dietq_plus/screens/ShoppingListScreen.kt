package com.example.dietq_plus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dietq_plus.ShoppingListRepository
import com.example.dietq_plus.components.ShoppingListItemCard
import com.example.dietq_plus.components.EmptyShoppingListMessage

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier
) {
    val shoppingListItems by ShoppingListRepository.shoppingList.collectAsState()
    val itemsList = remember(shoppingListItems) {
        shoppingListItems.values.sortedBy { it.nazwa }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Header z akcjami
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lista zakupów",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${itemsList.size} składników",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (itemsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val checkedCount = itemsList.count { it.isChecked }
                    val progressPercentage = if (itemsList.isNotEmpty()) {
                        checkedCount.toFloat() / itemsList.size.toFloat()
                    } else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Postęp: $checkedCount/${itemsList.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (checkedCount > 0) {
                            TextButton(
                                onClick = { ShoppingListRepository.clearCheckedItems() }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń zaznaczone",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usuń zaznaczone")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = progressPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (itemsList.isEmpty()) {
            EmptyShoppingListMessage()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = itemsList,
                    key = { "${it.nazwa}_${it.jednostka}" }
                ) { item ->
                    ShoppingListItemCard(
                        item = item,
                        onCheckedChange = {
                            ShoppingListRepository.toggleItemChecked("${item.nazwa}_${item.jednostka}")
                        }
                    )
                }
            }
        }
    }
}