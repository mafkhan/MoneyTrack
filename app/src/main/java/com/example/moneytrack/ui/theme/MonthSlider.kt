package com.example.moneytrack.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import com.example.moneytrack.TransactionViewModel
import com.example.moneytrack.data.ExpenseTypeTotal
import java.time.YearMonth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthSlider(viewModel: TransactionViewModel) {

    val currentYearMonth = YearMonth.now()
    val monthsToShow = 13

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { monthsToShow }
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) { page ->

        val selectedMonth = currentYearMonth.minusMonths(page.toLong())

        val totals by viewModel
            .getTotalsForMonth(selectedMonth.year, selectedMonth.monthValue)
            .collectAsState(initial = emptyList())

        MonthSummaryCard(selectedMonth, totals)
    }
}

@Composable
fun MonthSummaryCard(yearMonth: YearMonth, totals: List<ExpenseTypeTotal>) {

    Card(

        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${yearMonth.month} ${yearMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (totals.isEmpty()) {
                Text("No data")
            } else {
                totals.forEach { item ->
                    Text("Total: AED ${DecimalFormat("#,###.00").format(item.totalAmount)}")


                }
            }
        }
    }
}
