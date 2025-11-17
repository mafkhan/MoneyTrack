package com.example.moneytrack.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.moneytrack.data.TransactionEntity

class TransactionAdapter :
    ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    class TransactionViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    class DiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val textView = TextView(parent.context).apply {
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        return TransactionViewHolder(textView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.textView.text = "💳 ${item.cardEnding} — ${item.shop}\n" +
                "💸 ${item.amount} | ${item.date}\n" +
                "🏷️ ${item.expenseType} | Remaining: ${item.remainingLimit}"
    }
}
