package com.example.moneytrack

import android.content.Context
import android.util.Log
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity

object SmsUtils {

    private val TAG = "SmsUtils"

    /**
     * Processes a bank SMS by detecting type (ATM or normal purchase),
     * parsing the message, checking duplicates, and returning a TransactionEntity.
     *
     * NOTE: This does NOT insert into DB, caller inserts.
     */
    suspend fun processBankMessage(
        context: Context,
        message: String,
        smsId: Long
    ): TransactionEntity? {

        val db = AppDatabase.getDatabase(context)
        val dao = db.transactionDao()

        val smsHash = hashMessage(message)

        // Duplicate by smsId
        if (dao.existsBySmsId(smsId) > 0) {
            return null
        }

        // Duplicate by Hash
        if (dao.findByHash(smsHash) != null) {
            return null
        }

        parseAtmWithdrawal(message, smsId, smsHash)?.let { return it }
        parsePurchase(message, smsId, smsHash)?.let { return it }

        return null
    }



    // ======================================================================
    // PARSE ATM WITHDRAWAL
    // ======================================================================
    fun parseAtmWithdrawal(message: String, smsId: Long, smsHash: String): TransactionEntity? {
        if (!message.contains("ATM Withdrawal", ignoreCase = true)) return null

        val amount = Regex("""Amount:\s*AED\s*([0-9,.]+)""")
            .find(message)?.groupValues?.get(1)
            ?.replace(",", "")?.toDoubleOrNull() ?: return null

        val date = Regex("""Date:\s*([0-9]{2}/[0-9]{2}/[0-9]{4})""")
            .find(message)?.groupValues?.get(1) ?: return null

        val cardEnding = Regex("""Debit Card Ending:\s*([0-9]{4})""")
            .find(message)?.groupValues?.get(1) ?: ""

        return TransactionEntity(
            smsHash = smsHash,
            smsId = smsId,
            cardEnding = cardEnding,
            shop = "ATM Withdrawal",
            amount = amount,
            date = date,
            remainingLimit = "",
            bank = "EI",
            expenseType = "ATM Withdrawal"
        )
    }


    // ======================================================================
    // PARSE PURCHASE TRANSACTION (Normal EI Credit/Debit SMS)
    // ======================================================================
    fun parsePurchase(message: String, smsId: Long, smsHash: String): TransactionEntity? {
        if (!message.contains("Card Ending", ignoreCase = true)) return null

        val cardEnding = Regex("""Card Ending:\s*:?\s*(\d{4})""")
            .find(message)?.groupValues?.get(1) ?: return null

        val shop = Regex("""At:\s*(.+)""")
            .find(message)?.groupValues?.get(1)
            ?.substringBefore("\n")?.trim() ?: return null

        val amount = Regex("""Amount:\s*AED\s*([\d,]+(?:\.\d{1,2})?)""")
            .find(message)?.groupValues?.get(1)
            ?.replace(",", "")?.toDoubleOrNull() ?: return null

        val date = Regex("""Date:\s*(\d{2}/\d{2}/\d{4})""")
            .find(message)?.groupValues?.get(1) ?: return null

        val remainingLimit = Regex("""Available Limit:\s*AED\s*([\d,]+)""")
            .find(message)?.groupValues?.get(1)
            ?.replace(",", "") ?: ""

        return TransactionEntity(
            smsHash = smsHash,
            smsId = smsId,
            cardEnding = cardEnding,
            shop = shop,
            amount = amount,
            date = date,
            remainingLimit = remainingLimit,
            bank = "EI",
            expenseType = "Uncategorized"
        )
    }


    // ======================================================================
    // Utility
    // ======================================================================
    fun hashMessage(message: String): String {
        return message.trim().hashCode().toString()
    }
}
