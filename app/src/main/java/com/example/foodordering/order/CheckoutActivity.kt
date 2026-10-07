package com.example.foodordering.order

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.CartDisplayItem
import com.example.foodordering.model.OrderStatus
import com.example.foodordering.utils.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckoutActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private val cartItems = mutableListOf<CartDisplayItem>()
    private var totalAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.isAdmin()) {
            finish()
            return
        }

        val tvCustomerName = findViewById<TextView>(R.id.tvCustomerName)
        val tvCustomerPhone = findViewById<TextView>(R.id.tvCustomerPhone)
        val tvTotal = findViewById<TextView>(R.id.tvCheckoutTotal)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmOrder)

        tvCustomerName.text = "Khách hàng: ${sessionManager.getName()}"
        tvCustomerPhone.text = "SĐT: ${sessionManager.getPhone()}"

        loadCartAndCalculate()

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvTotal.text = "Tổng thanh toán: ${formatter.format(totalAmount)}"

        btnConfirm.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createOrder()
        }
    }

    private fun loadCartAndCalculate() {
        cartItems.clear()
        totalAmount = 0.0
        val userId = sessionManager.getUserId()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT c.id, c.product_id, p.name, p.price, c.quantity, p.image
            FROM ${DatabaseHelper.TABLE_CART} c
            JOIN ${DatabaseHelper.TABLE_PRODUCTS} p ON c.product_id = p.id
            WHERE c.user_id = ?
            """.trimIndent(),
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val item = CartDisplayItem(
                    cartId = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    image = cursor.getString(cursor.getColumnIndexOrThrow("image")) ?: ""
                )
                cartItems.add(item)
                totalAmount += item.total
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    private fun createOrder() {
        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase

        // Tạo mã đơn dễ đọc
        val orderCode = "DH" + SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date()) +
                "-" + (1000..9999).random()

        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Insert Order
        val orderValues = ContentValues().apply {
            put("user_id", userId)
            put("total", totalAmount)
            put("status", OrderStatus.PENDING.value)
            put("created_at", createdAt)
            put("order_code", orderCode)
        }
        val orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, orderValues)

        if (orderId == -1L) {
            db.close()
            Toast.makeText(this, "Tạo đơn thất bại", Toast.LENGTH_SHORT).show()
            return
        }

        // Insert Order Items
        cartItems.forEach { item ->
            val itemValues = ContentValues().apply {
                put("order_id", orderId)
                put("product_id", item.productId)
                put("quantity", item.quantity)
                put("price", item.price) // lưu giá tại thời điểm đặt
            }
            db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues)
        }

        // Xóa giỏ hàng sau khi đặt thành công
        db.delete(DatabaseHelper.TABLE_CART, "user_id = ?", arrayOf(userId.toString()))
        db.close()

        Toast.makeText(this, "Đặt hàng thành công!\nMã đơn: $orderCode", Toast.LENGTH_LONG).show()

        // Chuyển sang màn đơn hàng
        val intent = Intent(this, OrdersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}