package com.example.moneytrack

// Android / Compose
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.moneytrack.data.ExpenseTypeTotal
import com.example.moneytrack.ui.MonthSlider
// App data
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity
// Compose helpers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import java.text.DecimalFormat
// Pager (experimental)
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
// Time
import java.time.YearMonth

@OptIn(ExperimentalFoundationApi::class)

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_SMS),
            101
        )

        // DB + ViewModel setup
        val db = AppDatabase.getDatabase(applicationContext)
        val transactionDao = db.transactionDao()
        val viewModelFactory = TransactionViewModelFactory(transactionDao)
        val viewModel: TransactionViewModel by viewModels { viewModelFactory }

        // Set UI
        setContent {
            MaterialTheme {
                val lastTen by viewModel.lastTenTransactions.collectAsState(initial = emptyList())
                val currentMonthTotal by viewModel.currentMonthTotal.collectAsState(initial = 0.0)
                val currentDayTotal by viewModel.currentDayTotal.collectAsState(initial = 0.0)

                Column(modifier = Modifier.fillMaxSize()) {

                    // EXISTING MAIN UI
                    TransactionListScreen(
                        viewModel = viewModel,
                        lastTen = lastTen,
                        currentMonthTotal = currentMonthTotal,
                        currentDayTotal = currentDayTotal
                    )
                }
            }
        }
    }

    // --- Helper methods (these must be inside the Activity) ---

    fun fetchAllEiSms(): List<Pair<Long, String>> {
        val messages = mutableListOf<Pair<Long, String>>()

        val cursor = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("_id", "body", "date"),
            "address = 'EI SMS'",
            null,
            "date DESC"
        )

        cursor?.use { c ->
            val idIndex = c.getColumnIndexOrThrow("_id")
            val bodyIndex = c.getColumnIndexOrThrow("body")
            while (c.moveToNext()) {
                val smsId = c.getLong(idIndex)
                val body = c.getString(bodyIndex)
                messages.add(Pair(smsId, body))
            }
        }

        return messages
    }




}

// --- Composables: OUTSIDE the Activity class ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel,
    lastTen: List<TransactionEntity>,
    currentMonthTotal: Double,
    currentDayTotal: Double
) {
    val context = LocalContext.current
    var latestSms by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💰 MoneyTrack") })

            MonthSlider(viewModel)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        )

        {
            // ⭐ Current Month Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📅 Current Month Total Expense", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AED ${DecimalFormat("#,###.00").format(currentMonthTotal)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day total
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📅 Expense Today", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AED ${DecimalFormat("#,###.00").format(currentDayTotal)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GET All BUTTON
            Button(
                onClick = {
                    val smsList = (context as MainActivity).fetchAllEiSms()

                    if (smsList.isEmpty()) {
                        latestSms = "No EI SMS messages found."
                        return@Button
                    }

                    (context as MainActivity).lifecycleScope.launch {
                        var successCount = 0

                        smsList.forEach { (smsId, body) ->
                            val parsed = SmsUtils.processBankMessage(context, body)
                            if (parsed != null) {
                                viewModel.addTransaction(parsed)
                                successCount++
                            }
                        }

                        // Refresh monthly & daily totals AFTER adding transactions
                        viewModel.loadCurrentMonthTotal()
                        viewModel.loadCurrentDayTotal()

                        latestSms = "✔ Processed $successCount EI messages."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Latest")
            }

            if (latestSms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Latest EI SMS:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(latestSms, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(lastTen) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
fun MonthSummaryCard(yearMonth: YearMonth, totals: List<ExpenseTypeTotal>) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${yearMonth.month} ${yearMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (totals.isEmpty()) {
                Text("No data")
            } else {
                totals.forEach { item ->
                    Text("${item.expenseType}: ${item.totalAmount}")
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bank: ${transaction.bank}")
            Text("Amount: ${transaction.amount}")
            Text("Type: ${transaction.expenseType}")
            Text("Shop: ${transaction.shop}")
            Text("Date: ${transaction.date}")
            Text("CardEnding: ${transaction.cardEnding}")
        }
    }
}
