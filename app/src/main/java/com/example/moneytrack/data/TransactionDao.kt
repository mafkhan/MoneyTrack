package com.example.moneytrack.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>


    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 10")
    fun getLastTenTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT expenseType FROM transactions WHERE shop = :shop LIMIT 1")
    suspend fun getExpenseTypeForShop(shop: String): String?

    @Query("SELECT SUM(amount) FROM transactions WHERE substr(date, 7, 4) || '-' || substr(date, 4, 2) = strftime('%Y-%m', 'now')")
    suspend fun getCurrentMonthTotal(): Double?

    @Query("""
    SELECT COUNT(*) FROM transactions
    WHERE amount = :amount
    AND shop = :shop
    AND date = :date
""")

    suspend fun countDuplicate(amount: Double, shop: String, date: String): Int
    @Query("SELECT COUNT(*) FROM transactions WHERE smsId = :smsId")
    suspend fun existsBySmsId(smsId: Long): Int

    @Query("SELECT * FROM transactions WHERE date = :date AND amount = :amount AND cardEnding = :cardEnding LIMIT 1")
    suspend fun findDuplicate(date: String, amount: Double, cardEnding: String): TransactionEntity?



}
