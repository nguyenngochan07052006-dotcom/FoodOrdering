package com.example.foodordering.admin

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.Order
import com.example.foodordering.model.OrderStatus
import com.example.foodordering.utils.SessionManager

class OrderManagementActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var rvOrders: RecyclerView

    private val orders = mutableListOf<Order>()
    private lateinit var adapter: AdminOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_management)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish()
            return
        }

        rvOrders = findViewById(R.id.rvAdminOrders)

        adapter = AdminOrderAdapter(orders) { order ->
            showStatusDialog(order)
        }

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        orders.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id, user_id, total, status, created_at, order_code
            FROM ${DatabaseHelper.TABLE_ORDERS}
            ORDER BY created_at DESC
            """.trimIndent(),
            null
        )

        if (cursor.moveToFirst()) {
            do {
                orders.add(
                    Order(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        orderCode = cursor.getString(cursor.getColumnIndexOrThrow("order_code"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        adapter.notifyDataSetChanged()
    }

    private fun showStatusDialog(order: Order) {
        val statuses = arrayOf(
            "PENDING - Chờ xác nhận",
            "CONFIRMED - Đã xác nhận",
            "PREPARING - Đang chuẩn bị",
            "READY - Sẵn sàng",
            "COMPLETED - Hoàn thành",
            "CANCELLED - Hủy đơn"
        )

        val values = arrayOf(
            OrderStatus.PENDING.value,
            OrderStatus.CONFIRMED.value,
            OrderStatus.PREPARING.value,
            OrderStatus.READY.value,
            OrderStatus.COMPLETED.value,
            OrderStatus.CANCELLED.value
        )

        AlertDialog.Builder(this)
            .setTitle("Cập nhật trạng thái\n${order.orderCode}")
            .setItems(statuses) { _, which ->
                updateStatus(order.id, values[which])
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateStatus(orderId: Int, newStatus: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", newStatus)
        }
        db.update(DatabaseHelper.TABLE_ORDERS, values, "id = ?", arrayOf(orderId.toString()))
        db.close()

        Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show()
        loadOrders()
    }
}