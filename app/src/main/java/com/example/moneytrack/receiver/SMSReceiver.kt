package com.example.moneytrack.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.example.moneytrack.SmsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.Telephony

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val pdus = bundle["pdus"] as Array<*>?
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

                            // Only process EI SMS with credit card purchase
                            if (sender.contains("EI SMS", ignoreCase = true) &&
                                msgBody.contains("Credit Card Purchase", ignoreCase = true)) {

                                // ❗ Get smsId from message
                                val smsId: Long = message.indexOnIccOrDefault() // helper function below
                                SmsUtils.processBankMessage(context, msgBody, smsId)
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}

// Helper extension to safely get a unique ID from SmsMessage
fun SmsMessage.indexOnIccOrDefault(): Long {
    return try {
        this.timestampMillis // fallback to timestamp if _id unavailable
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
