package com.example.moneytrack.data

import kotlinx.coroutines.flow.Flow
class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val lastTenTransactions = transactionDao.getLastTenTransactions()
    val currentMonthTotals = transactionDao.getCurrentMonthTotals()

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }
}
