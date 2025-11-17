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


    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT 10")
    suspend fun getLastTenTransactions(): List<TransactionEntity>



    @Query("SELECT expenseType FROM transactions WHERE shop = :shop LIMIT 1")
    suspend fun getExpenseTypeForShop(shop: String): String?

}
