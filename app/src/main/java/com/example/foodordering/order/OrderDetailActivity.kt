package com.example.foodordering.order

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var orderId: Int = -1
    private var orderStatus: String = "pending"

    private lateinit var layoutTimeline: LinearLayout
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var btnReorder: MaterialButton

    private val orderItems = mutableListOf<OrderItemDisplay>()
    private lateinit var itemAdapter: OrderItemAdapter

    data class OrderItemDisplay(
        val productName: String,
        val quantity: Int,
        val price: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        orderId = intent.getIntExtra("ORDER_ID", -1)
        if (orderId == -1) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        layoutTimeline = findViewById(R.id.layoutTimeline)
        rvOrderItems = findViewById(R.id.rvOrderItems)
        btnReorder = findViewById(R.id.btnReorder)

        itemAdapter = OrderItemAdapter(orderItems)
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        rvOrderItems.adapter = itemAdapter

        loadOrderInfo()
        loadOrderItems()
        buildTimeline()

        btnReorder.setOnClickListener {
            reorder()
        }
    }

    private fun loadOrderInfo() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT order_code, total, status, created_at
            FROM ${DatabaseHelper.TABLE_ORDERS}
            WHERE id = ?
            """.trimIndent(),
            arrayOf(orderId.toString())
        )

        if (cursor.moveToFirst()) {
            val code = cursor.getString(cursor.getColumnIndexOrThrow("order_code"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
            orderStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))

            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

            findViewById<TextView>(R.id.tvOrderCode).text = "Mã đơn: $code"
            findViewById<TextView>(R.id.tvOrderDate).text = "Ngày đặt: $createdAt"
            findViewById<TextView>(R.id.tvOrderTotal).text = "Tổng tiền: ${formatter.format(total)}"
        }
        cursor.close()
        db.close()
    }

    private fun loadOrderItems() {
        orderItems.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT oi.quantity, oi.price, p.name
            FROM ${DatabaseHelper.TABLE_ORDER_ITEMS} oi
            JOIN ${DatabaseHelper.TABLE_PRODUCTS} p ON oi.product_id = p.id
            WHERE oi.order_id = ?
            """.trimIndent(),
            arrayOf(orderId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                orderItems.add(
                    OrderItemDisplay(
                        productName = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        itemAdapter.notifyDataSetChanged()
    }

    private fun buildTimeline() {
        layoutTimeline.removeAllViews()

        val steps = listOf(
            "pending" to "Chờ xác nhận",
            "confirmed" to "Đã xác nhận",
            "preparing" to "Đang pha chế",
            "ready" to "Sẵn sàng lấy",
            "completed" to "Hoàn thành"
        )

        val currentIndex = when (orderStatus.lowercase()) {
            "pending", "paid" -> 0
            "confirmed" -> 1
            "preparing" -> 2
            "ready" -> 3
            "completed" -> 4
            "cancelled" -> -1
            else -> 0
        }

        if (orderStatus.lowercase() == "cancelled") {
            addTimelineStep("Đã hủy", isActive = true, isLast = true, isCancelled = true)
            return
        }

        steps.forEachIndexed { index, pair ->
            val isActive = index <= currentIndex
            val isLast = index == steps.size - 1
            addTimelineStep(pair.second, isActive, isLast)
        }
    }

    private fun addTimelineStep(title: String, isActive: Boolean, isLast: Boolean, isCancelled: Boolean = false) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        // Circle
        val circle = TextView(this).apply {
            width = 24
            height = 24
            gravity = Gravity.CENTER
            text = if (isActive) "✓" else ""
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(
                when {
                    isCancelled -> Color.parseColor("#C62828")
                    isActive -> Color.parseColor("#6D4C41")
                    else -> Color.parseColor("#BDBDBD")
                }
            )
            // Bo tròn
            background = resources.getDrawable(android.R.drawable.presence_online, null).mutate().apply {
                setTint(
                    when {
                        isCancelled -> Color.parseColor("#C62828")
                        isActive -> Color.parseColor("#6D4C41")
                        else -> Color.parseColor("#BDBDBD")
                    }
                )
            }
        }

        // Text
        val tv = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(
                when {
                    isCancelled -> Color.parseColor("#C62828")
                    isActive -> Color.parseColor("#5D4037")
                    else -> Color.parseColor("#9E9E9E")
                }
            )
            setPadding(16, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        row.addView(circle)
        row.addView(tv)
        layoutTimeline.addView(row)

        // Đường nối
        if (!isLast) {
            val line = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(4, 24).apply {
                    marginStart = 10
                }
                setBackgroundColor(
                    if (isActive) Color.parseColor("#6D4C41") else Color.parseColor("#E0E0E0")
                )
            }
            layoutTimeline.addView(line)
        }
    }

    private fun reorder() {
        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase

        val cursor = db.rawQuery(
            "SELECT product_id, quantity FROM ${DatabaseHelper.TABLE_ORDER_ITEMS} WHERE order_id = ?",
            arrayOf(orderId.toString())
        )

        var addedCount = 0

        if (cursor.moveToFirst()) {
            do {
                val productId = cursor.getInt(0)
                val quantity = cursor.getInt(1)

                // Kiểm tra available
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

                // Thêm / cập nhật giỏ
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

    // Adapter nội bộ cho danh sách món
    inner class OrderItemAdapter(
        private val items: List<OrderItemDisplay>
    ) : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvItemName)
            val tvQty: TextView = view.findViewById(R.id.tvItemQty)
            val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_order_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            holder.tvName.text = item.productName
            holder.tvQty.text = "x${item.quantity}"
            holder.tvPrice.text = formatter.format(item.price * item.quantity)
        }

        override fun getItemCount() = items.size
    }
}