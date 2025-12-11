package com.example.moneytrack.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moneytrack.ui.widgets.*

@Composable
fun DashboardScreen(
    todayExpense: Double,
    todayChange: Double,
    monthExpense: Double,
    monthCredit: Double,
    monthTransfer: Double,
    monthATM: Double,
    chartData: List<Float>,
    categories: List<Pair<String, Float>>,
    latestSms: String,
    onGetLatestClick: () -> Unit      // <-- Only callback. No Activity calls.
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --------------------------------
        // GET LATEST SMS BUTTON
        // --------------------------------
        item {
            Button(
                onClick = { onGetLatestClick() },   // <-- FIXED
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Latest EI SMS")
            }
        }

        // --------------------------------
        // SHOW STATUS MESSAGE
        // --------------------------------
        item {
            if (latestSms.isNotEmpty()) {
                Text(text = latestSms, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // --------------------------------
        // METRICS
        // --------------------------------
        item {
            MetricsGrid(
                items = listOf(
                    MetricItem("Total Expenses Today", todayExpense, todayChange),
                    MetricItem("Total Expenses This Month", monthExpense),
                    MetricItem("Total Credit This Month", monthCredit),
                    MetricItem("Total Transfer This Month", monthTransfer),
                    MetricItem("Total ATM Withdrawal", monthATM)
                )
            )
        }

        // --------------------------------
        // DONUT CHART
        // --------------------------------
        item {
            DonutChartCard(
                title = "Monthly Expense Breakdown",
                data = chartData
            )
        }

        // --------------------------------
        // CATEGORY BARS
        // --------------------------------
        item {
            CategoryBarsCard(
                title = "Spending Categories",
                categories = categories
            )
        }
    }
}
