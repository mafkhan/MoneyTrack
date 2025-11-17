package com.example.moneytrack.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneytrack.R
import com.example.moneytrack.data.AppDatabase
import com.example.moneytrack.data.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val pdus = bundle.get("pdus") as Array<*>?
            if (pdus != null) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        for (pdu in pdus) {
                            val format = intent.getStringExtra("format")
                            val message = SmsMessage.createFromPdu(pdu as ByteArray, format)
                            val sender = message.displayOriginatingAddress ?: ""
                            val msgBody = message.displayMessageBody ?: ""

                            Log.d("SMSReceiver", "SMS received from $sender: $msgBody")

                            if (sender.contains("EI SMS", ignoreCase = true) &&
                                msgBody.contains("Credit Card Purchase", ignoreCase = true)) {
                                processBankMessage(context, msgBody)
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
    private fun processBankMessage(context: Context, message: String) {
        try {
            val cardEnding = Regex("Card Ending: (\\d+)").find(message)?.groupValues?.get(1)
            val shop = Regex("At: (.*)").find(message)?.groupValues?.get(1)?.trim()
            val amount = Regex("Amount: AED ([\\d.]+)").find(message)?.groupValues?.get(1)?.toDoubleOrNull()
            val date = Regex("Date: (\\d{2}/\\d{2}/\\d{4})").find(message)?.groupValues?.get(1)
            val remainingLimit = Regex("Available Limit: AED ([\\d,.]+)").find(message)?.groupValues?.get(1)?.replace(",", "")

            if (cardEnding != null && amount != null && shop != null && date != null && remainingLimit != null) {
                val db = AppDatabase.getDatabase(context)

                // Use goAsync() to prevent premature receiver termination
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val existingType = db.transactionDao().getExpenseTypeForShop(shop)

                        val transaction = TransactionEntity(
                            cardEnding = cardEnding,
                            shop = shop,
                            amount = amount,
                            date = date,
                            remainingLimit = remainingLimit,
                            bank = "EI",
                            expenseType = existingType ?: "Uncategorized"
                        )

                        db.transactionDao().insert(transaction)
                        Log.d("SMSReceiver", "Transaction saved: $transaction")

                        if (existingType == null) {
                            sendNotification(context, shop)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                Log.e("SMSReceiver", "Failed to parse SMS: Missing required fields")
            }
        } catch (e: Exception) {
            Log.e("SMSReceiver", "Error parsing SMS: ${e.message}")
        }
    }

    private fun sendNotification(context: Context, shop: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "moneytrack_channel",
                "Money Track Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "moneytrack_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use proper notification icon
            .setContentTitle("Set Expense Type")
            .setContentText("New expense at $shop. Please set an expense type.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context).notify(shop.hashCode(), builder.build())
    }
}