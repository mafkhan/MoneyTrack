package com.example.moneytrack.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrack.data.AppDatabase
import kotlinx.coroutines.launch
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.Toast

class TransactionListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a simple layout programmatically
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "💰 Money Track — Transactions"
            textSize = 22f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@TransactionListActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(recyclerView)
        }

        rootLayout.addView(linearLayout)
        setContentView(rootLayout)

        adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        loadTransactions()
    }


    private fun loadTransactions() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.transactionDao().getAllTransactions().collect { transactions ->
                if (transactions.isEmpty()) {
                    Toast.makeText(this@TransactionListActivity, "No transactions found", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(transactions)
            }
        }

    }
}
