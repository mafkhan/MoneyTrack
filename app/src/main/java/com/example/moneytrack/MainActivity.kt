package com.example.moneytrack

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.ui.dashboard.DashboardScreen
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_SMS),
            101
        )

        // ViewModel
        val db = AppDatabase.getDatabase(applicationContext)
        val transactionDao = db.transactionDao()
        val viewModelFactory = TransactionViewModelFactory(transactionDao)
        val viewModel: TransactionViewModel by viewModels { viewModelFactory }

        setContent {

            MaterialTheme {

                Surface(modifier = Modifier.fillMaxSize()) {

                    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState(initial = 0.0)
                    val currentDayTotal by viewModel.currentDayTotal.collectAsState(initial = 0.0)
                    val currentMonthAtmTotal by viewModel.currentMonthAtmTotal.collectAsState(initial = 0.0)
                    var latestSms by remember { mutableStateOf("") }

                    DashboardScreen(
                        todayExpense = currentDayTotal,
                        todayChange = 0.0,
                        monthExpense = currentMonthTotal,
                        monthCredit = 500.0,
                        monthTransfer = 300.0,
                        monthATM = currentMonthAtmTotal,
                        chartData = listOf(30f, 25f, 20f),
                        categories = listOf("Food" to 0.5f, "Bills" to 0.3f),
                        latestSms = latestSms,
                        onGetLatestClick = {
                            onGetLatestClicked(viewModel) { msg ->
                                latestSms = msg
                            }
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FUNCTION FIXED — MUST BE INSIDE MAINACTIVITY
    // -------------------------------------------------------------------------
    fun onGetLatestClicked(
        viewModel: TransactionViewModel,
        onResult: (String) -> Unit
    ) {

        val smsList = fetchAllEiSms()

        if (smsList.isEmpty()) {
            onResult("No EI SMS messages found.")
            return
        }

        lifecycleScope.launch {
            var count = 0


                smsList.forEach { (smsId, body) ->
                    val exists = viewModel.existsBySmsId(smsId)
                    if (exists) return@forEach
                    // <-- correctly receiving smsId from Pair
                    val parsed = SmsUtils.processBankMessage(
                        context = this@MainActivity,
                        message = body,
                        smsId = smsId               // <-- FIXED (this is the correct variable)
                    )
                if (parsed != null) {
                    viewModel.addTransaction(parsed)
                    count++
                }
            }

            viewModel.loadCurrentMonthTotal()
            viewModel.loadCurrentDayTotal()
            viewModel.loadCurrentMonthAtmTotal()

            onResult("✔ Processed $count EI messages.")
        }
    }

    // -------------------------------------------------------------------------
    // FIXED — THIS MUST BE INSIDE MAINACTIVITY, OUTSIDE setContent
    // -------------------------------------------------------------------------
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
                messages.add(smsId to body)
            }
        }

        return messages
    }
}
