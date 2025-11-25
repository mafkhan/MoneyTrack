package com.example.moneytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.TransactionDao
import com.example.moneytrack.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _currentMonthTotal = MutableStateFlow(0.0)
    val currentMonthTotal: StateFlow<Double> = _currentMonthTotal.asStateFlow()

    private val _lastTenTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val lastTenTransactions: StateFlow<List<TransactionEntity>> = _lastTenTransactions.asStateFlow()

    init {
        loadAllTransactions()
        loadLastTenTransactions()
        loadCurrentMonthTotal()
    }

    private fun loadAllTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collectLatest {
                _allTransactions.value = it
            }
        }
    }

    private fun loadLastTenTransactions() {
        viewModelScope.launch {
            transactionDao.getLastTenTransactions().collectLatest {
                _lastTenTransactions.value = it
            }
        }
    }

    fun loadCurrentMonthTotal() {
        viewModelScope.launch {
            val total = transactionDao.getCurrentMonthTotal()
            _currentMonthTotal.value = total ?: 0.0
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
        }
    }



}


@Suppress("UNCHECKED_CAST")
class TransactionViewModelFactory(private val transactionDao: TransactionDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
