package com.example.foodordering.admin

import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.Order
import com.example.foodordering.utils.SessionManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var rvAdminOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var chipGroupStatus: ChipGroup

    private val allOrders = mutableListOf<Order>()
    private val displayedOrders = mutableListOf<Order>()
    private lateinit var adapter: AdminOrderAdapter

    private var currentFilter: String = "all" // all, pending, preparing, ready, completed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_orders)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvAdminOrders = findViewById(R.id.rvAdminOrders)
        tvEmpty = findViewById(R.id.tvEmpty)
        chipGroupStatus = findViewById(R.id.chipGroupStatus)

        adapter = AdminOrderAdapter(
            orders = displayedOrders,
            onNextStatus = { order -> updateToNextStatus(order) },
            onCancel = { order -> confirmCancelOrder(order) }
        )

        rvAdminOrders.layoutManager = LinearLayoutManager(this)
        rvAdminOrders.adapter = adapter

        setupFilterChips()
        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun setupFilterChips() {
        chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val chipId = checkedIds[0]
            currentFilter = when (chipId) {
                R.id.chipPending -> "pending"
                R.id.chipPreparing -> "preparing"
                R.id.chipReady -> "ready"
                R.id.chipCompleted -> "completed"
                else -> "all"
            }
            filterOrders()
        }
    }

    private fun loadOrders() {
        allOrders.clear()
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
                val order = Order(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    orderCode = cursor.getString(cursor.getColumnIndexOrThrow("order_code"))
                )
                allOrders.add(order)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        filterOrders()
    }

    private fun filterOrders() {
        displayedOrders.clear()

        when (currentFilter) {
            "pending" -> {
                displayedOrders.addAll(allOrders.filter {
                    it.status.lowercase() in listOf("pending", "paid")
                })
            }
            "preparing" -> {
                displayedOrders.addAll(allOrders.filter {
                    it.status.lowercase() in listOf("confirmed", "preparing")
                })
            }
            "ready" -> {
                displayedOrders.addAll(allOrders.filter {
                    it.status.lowercase() == "ready"
                })
            }
            "completed" -> {
                displayedOrders.addAll(allOrders.filter {
                    it.status.lowercase() == "completed"
                })
            }
            else -> {
                displayedOrders.addAll(allOrders)
            }
        }

        adapter.notifyDataSetChanged()

        if (displayedOrders.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvAdminOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvAdminOrders.visibility = View.VISIBLE
        }
    }

    private fun updateToNextStatus(order: Order) {
        val nextStatus = when (order.status.lowercase()) {
            "pending", "paid" -> "confirmed"
            "confirmed" -> "preparing"
            "preparing" -> "ready"
            "ready" -> "completed"
            else -> return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", nextStatus)
        }
        val rows = db.update(
            DatabaseHelper.TABLE_ORDERS,
            values,
            "id = ?",
            arrayOf(order.id.toString())
        )
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Đã cập nhật trạng thái đơn ${order.orderCode}", Toast.LENGTH_SHORT).show()
            loadOrders()
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmCancelOrder(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy đơn")
            .setMessage("Bạn có chắc muốn hủy đơn ${order.orderCode}?")
            .setPositiveButton("Hủy đơn") { _, _ ->
                cancelOrder(order)
            }
            .setNegativeButton("Không", null)
            .show()
    }

    private fun cancelOrder(order: Order) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", "cancelled")
        }
        val rows = db.update(
            DatabaseHelper.TABLE_ORDERS,
            values,
            "id = ?",
            arrayOf(order.id.toString())
        )
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Đã hủy đơn ${order.orderCode}", Toast.LENGTH_SHORT).show()
            loadOrders()
        } else {
            Toast.makeText(this, "Hủy đơn thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}