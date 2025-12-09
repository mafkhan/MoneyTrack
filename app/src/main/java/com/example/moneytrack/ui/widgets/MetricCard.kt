package com.example.moneytrack.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class MetricItem(
    val title: String,
    val amount: Double,
    val change: Double? = null
)

@Composable
fun MetricCard(
    item: MetricItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AED %.2f".format(item.amount),
                style = MaterialTheme.typography.headlineSmall
            )

            item.change?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Change: $it%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
