
package com.example.moneytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.TransactionDao
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.moneytrack.data.TransactionEntity

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    // Observing all transactions
    val allTransactions: LiveData<List<TransactionEntity>> =
        transactionDao.getAllTransactions().asLiveData()

    // Existing method
    fun insertTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
        }
    }

    // ✅ New method for FAB usage
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
