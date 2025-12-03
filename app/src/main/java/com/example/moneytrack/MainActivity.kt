package com.example.moneytrack

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
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import java.text.DecimalFormat

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission updated
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_SMS),
            101
        )

        val db = AppDatabase.getDatabase(applicationContext)
        val transactionDao = db.transactionDao()
        val viewModelFactory = TransactionViewModelFactory(transactionDao)
        val viewModel: TransactionViewModel by viewModels { viewModelFactory }

        //this is to backfill -->>    // viewModel.bulkUpdateExpenseTypes()

        setContent {
            MaterialTheme {
                val lastTen by viewModel.lastTenTransactions.collectAsState(initial = emptyList())
                val currentMonthTotal by viewModel.currentMonthTotal.collectAsState(initial = 0.0)
                val currentDayTotal by viewModel.currentDayTotal.collectAsState(initial = 0.0)


                TransactionListScreen(
                    viewModel = viewModel,
                    //lastTen = allTransactions.takeLast(10),
                    lastTen = lastTen,
                    currentMonthTotal = currentMonthTotal,
                    currentDayTotal = currentDayTotal


                )

            }
        }



    }

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


    // Fetch only the latest EI SMS
    fun fetchLatestEiSms(): Pair<Long, String>? {
        val cursor = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            "address = 'EI SMS'",
            null,
            "date DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val smsId = it.getLong(it.getColumnIndexOrThrow("_id"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                return Pair(smsId, body)
            }
        }

        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(viewModel: TransactionViewModel, lastTen: List<TransactionEntity>, currentMonthTotal: Double,
    currentDayTotal: Double
) {
    val context = LocalContext.current
    var latestSms by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💰 MoneyTrack") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // ⭐ Current Month Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📅 Current Month Total Expense", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        //text = "AED ${"%.2f".format(currentMonthTotal)}",
                        text = "AED ${DecimalFormat("#,###.00").format(currentMonthTotal)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Day total

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📅 Expense Today", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        //text = "AED ${"%.2f".format(currentDayTotal)}",
                        text = "AED ${DecimalFormat("#,###.00").format(currentDayTotal)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            // type based expense expCat


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

                        // Refresh monthly total AFTER adding transactions
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
//@Composable
//fun HomeScreen(viewModel: TransactionViewModel) {
//
//    val transactions by viewModel.currentMonthTransactions.collectAsState()
//
//    LazyColumn {
//        items(transactions) { txn ->
//            Text("${txn.date} - ${txn.shop} - AED ${txn.amount}")
//        }
//    }
//    val totals by viewModel.currentMonthTotals.collectAsState(initial = emptyList())
//
//    Column {
//
//        // Header Row
//        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//            Text(
//                text = "Expense Type",
//                modifier = Modifier.weight(1f),
//                style = MaterialTheme.typography.titleMedium
//            )
//            Text(
//                text = "Total (AED)",
//                modifier = Modifier.weight(1f),
//                style = MaterialTheme.typography.titleMedium
//            )
//        }
//
//        Divider()
//
//        // Table Data Rows
//        LazyColumn {
//            items(totals) { item ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp, horizontal = 8.dp)
//                ) {
//                    Text(
//                        text = item.expenseType,
//                        modifier = Modifier.weight(1f),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = "%.2f".format(item.totalAmount),
//                        modifier = Modifier.weight(1f),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//                Divider()
//            }
//        }
//    }




