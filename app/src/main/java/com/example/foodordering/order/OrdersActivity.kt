package com.example.foodordering.order

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvOrders = findViewById(R.id.rvOrders)
        tvEmpty = findViewById(R.id.tvEmptyOrders)

        adapter = OrderAdapter(
            orders = orders,
            onReorder = { order -> reorder(order) },
            onItemClick = { order ->
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                startActivity(intent)
            }
        )

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

    // Quick Order - Đặt lại đơn cũ
    private fun reorder(order: Order) {
        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase

        // Lấy các món trong đơn cũ
        val cursor = db.rawQuery(
            """
            SELECT product_id, quantity
            FROM ${DatabaseHelper.TABLE_ORDER_ITEMS}
            WHERE order_id = ?
            """.trimIndent(),
            arrayOf(order.id.toString())
        )

        var addedCount = 0

        if (cursor.moveToFirst()) {
            do {
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))

                // Kiểm tra món còn available không
                val pCursor = db.rawQuery(
                    "SELECT status FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE id = ?",
                    arrayOf(productId.toString())
                )
                var status = "unavailable"
                if (pCursor.moveToFirst()) {
                    status = pCursor.getString(0)
                }
                pCursor.close()

                if (status != "available") continue

                // Kiểm tra đã có trong giỏ chưa
                val cCursor = db.rawQuery(
                    "SELECT id, quantity FROM ${DatabaseHelper.TABLE_CART} WHERE user_id = ? AND product_id = ?",
                    arrayOf(userId.toString(), productId.toString())
                )

                if (cCursor.moveToFirst()) {
                    val cartId = cCursor.getInt(0)
                    val oldQty = cCursor.getInt(1)
                    val values = ContentValues().apply {
                        put("quantity", oldQty + quantity)
                    }
                    db.update(DatabaseHelper.TABLE_CART, values, "id = ?", arrayOf(cartId.toString()))
                } else {
                    val values = ContentValues().apply {
                        put("user_id", userId)
                        put("product_id", productId)
                        put("quantity", quantity)
                    }
                    db.insert(DatabaseHelper.TABLE_CART, null, values)
                }
                cCursor.close()
                addedCount++
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        if (addedCount > 0) {
            Toast.makeText(this, "Đã thêm $addedCount món vào giỏ hàng", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Không còn món nào khả dụng để đặt lại", Toast.LENGTH_SHORT).show()
        }
    }
}