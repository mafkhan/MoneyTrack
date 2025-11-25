package com.example.moneytrack

import android.content.Context
import android.util.Log
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity


object SmsUtils {

    /**
     * Parse message and insert only if not duplicate (by smsId).
     * Returns the inserted TransactionEntity or null if parsing failed or duplicate.
     */
    suspend fun processBankMessage(context: Context, message: String): TransactionEntity? {
        return try {
            val cardEnding = Regex("""Card Ending:\s*(\d{4})""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.get(1)
            val shop = Regex("""At:\s*(.+)""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.get(1)?.trim()
            val amount = Regex("""Amount:\s*AED\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
            val date = Regex("""Date:\s*(\d{2}/\d{2}/\d{4})""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.get(1)
            val remainingLimit = Regex("""Available Limit:\s*AED\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.get(1)?.replace(",", "")

            if (cardEnding == null || shop == null || amount == null || date == null || remainingLimit == null) {
                Log.e("SmsUtils", "Failed to parse SMS: $message")
                return null
            }

            val db = AppDatabase.getDatabase(context)
            val dao = db.transactionDao()

            // Use combination of date + amount + cardEnding as unique key
            val existing = dao.findDuplicate(date = date, amount = amount, cardEnding = cardEnding)
            if (existing != null) {
                Log.d("SmsUtils", "Duplicate transaction skipped: $shop - $amount - $date")

                return null
            }

            val existingType = dao.getExpenseTypeForShop(shop)

            val transaction = TransactionEntity(
                smsId = System.currentTimeMillis(), // just a unique long, not for duplicate check
                cardEnding = cardEnding,
                shop = shop,
                amount = amount,
                date = date,
                remainingLimit = remainingLimit,
                bank = "EI",
                expenseType = existingType ?: "Uncategorized"
            )

            dao.insert(transaction)
            Log.d("SmsUtils", "Inserted transaction: $transaction")
            transaction
        } catch (e: Exception) {
            Log.e("SmsUtils", "Error parsing SMS: ${e.message}")
            null
        }
    }

}



