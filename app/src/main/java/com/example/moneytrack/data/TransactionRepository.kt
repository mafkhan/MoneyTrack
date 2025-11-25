package com.example.moneytrack.data

import kotlinx.coroutines.flow.Flow
class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val lastTenTransactions = transactionDao.getLastTenTransactions()
  //  suspend fun getAllTransactions(): List<TransactionEntity> {
   //     return transactionDao.getAllTransactions()
  //  }

    suspend fun insert(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }
}
