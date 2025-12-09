package com.example.moneytrack.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    categories: List<Pair<String, Float>>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // -----------------------------
        // METRICS GRID
        // -----------------------------
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

        // -----------------------------
        // DONUT CHART
        // -----------------------------
        item {
            DonutChartCard(
                title = "Monthly Expense Breakdown",
                data = chartData
            )
        }

        // -----------------------------
        // CATEGORY BARS
        // -----------------------------
        item {
            CategoryBarsCard(
                title = "Spending Categories",
                categories = categories
            )
        }
    }
}
