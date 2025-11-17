package com.example.moneytrack

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request SMS permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_SMS),
            101
        )

        val db = AppDatabase.getDatabase(applicationContext)
        val transactionDao = db.transactionDao()
        val viewModelFactory = TransactionViewModelFactory(transactionDao)
        val viewModel: TransactionViewModel by viewModels { viewModelFactory }

        val isAuthenticated = mutableStateOf(false)
        val showBiometricPrompt = mutableStateOf(false)
        val lastTenTransactions = mutableStateOf<List<TransactionEntity>>(emptyList())

        setContent {
            MaterialTheme {
                when {
                    isAuthenticated.value -> {
                        // ✅ Fetch last 10 transactions when authenticated
                        LaunchedEffect(Unit) {
                            lifecycleScope.launch {
                                lastTenTransactions.value = transactionDao.getLastTenTransactions()
                            }
                        }
                        TransactionListScreen(viewModel, lastTenTransactions.value)
                    }
                    showBiometricPrompt.value -> {
                        BiometricLogin(
                            onSuccess = {
                                fetchEiSmsAndStore(transactionDao)
                                isAuthenticated.value = true
                            },
                            onError = {
                                Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
                                showBiometricPrompt.value = false
                            }
                        )
                    }
                    else -> WelcomeScreen(
                        onLoginClick = { showBiometricPrompt.value = true },
                        onRegisterClick = {
                            Toast.makeText(applicationContext, "Registration coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ✅ Fetch EI SMS and store last 5 in DB
    private fun fetchEiSmsAndStore(transactionDao: com.example.moneytrack.data.TransactionDao) {
        val cursor = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            "address='EI SMS'",
            null,
            "date DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")
            var count = 0
            while (it.moveToNext() && count < 5) {
                val body = it.getString(bodyIndex)
                val date = it.getString(dateIndex)

                val amount = extractAmount(body)
                val cardEnding = extractCardEnding(body)

                val transaction = TransactionEntity(
                    bank = "EI Bank",
                    amount = amount,
                    expenseType = "Debit",
                    shop = "Unknown",
                    date = date,
                    cardEnding = cardEnding,
                    remainingLimit = "N/A"
                )

                lifecycleScope.launch {
                    transactionDao.insert(transaction)
                }
                count++
            }
        }
    }

    private fun extractAmount(body: String): Double {
        val regex = Regex("""AED\s?(\d+(\.\d{1,2})?)""")
        return regex.find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    private fun extractCardEnding(body: String): String {
        val regex = Regex("""Card\s?ending\s?(\d{4})""")
        return regex.find(body)?.groupValues?.get(1) ?: "XXXX"
    }
}

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to MoneyTrack", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Register")
        }
    }
}

@Composable
fun BiometricLogin(onSuccess: () -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)

    LaunchedEffect(Unit) {
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS) {

            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(context as FragmentActivity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError("Error: $errString")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Authentication failed")
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Use your fingerprint to access MoneyTrack")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            onError("Biometric authentication not available")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionListScreen(viewModel: TransactionViewModel, lastTen: List<TransactionEntity>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("💰 MoneyTrack") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Last 10 Transactions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
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