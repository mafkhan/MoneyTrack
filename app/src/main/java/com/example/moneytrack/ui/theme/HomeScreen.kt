package com.example.moneytrack.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.moneytrack.TransactionViewModel

@Composable
fun HomeScreen(viewModel: TransactionViewModel) {

    val lastTen by viewModel.lastTenTransactions.collectAsState(initial = emptyList())

    LazyColumn {
        items(lastTen) { txn ->
            Text("${txn.shop} - ${txn.amount}")
        }
    }
}
