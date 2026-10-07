package com.example.foodordering.order

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.Order
import com.example.foodordering.utils.SessionManager

class OrdersActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView

    private val orders = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.isAdmin()) {
            finish()
            return
        }

        rvOrders = findViewById(R.id.rvOrders)
        tvEmpty = findViewById(R.id.tvEmptyOrders)

        adapter = OrderAdapter(orders)
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter

        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        orders.clear()
        val userId = sessionManager.getUserId()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id, user_id, total, status, created_at, order_code
            FROM ${DatabaseHelper.TABLE_ORDERS}
            WHERE user_id = ?
            ORDER BY created_at DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val order = Order(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    orderCode = cursor.getString(cursor.getColumnIndexOrThrow("order_code"))
                )
                orders.add(order)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        adapter.notifyDataSetChanged()

        if (orders.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            rvOrders.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            rvOrders.visibility = android.view.View.VISIBLE
        }
    }
}