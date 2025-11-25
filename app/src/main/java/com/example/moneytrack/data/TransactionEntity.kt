package com.example.moneytrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val smsId: Long,  // NEW: unique SMS identifier
    val cardEnding: String,
    val shop: String,
    val amount: Double,
    val date: String,
    val remainingLimit: String,
    val bank: String,
    val expenseType: String?
)