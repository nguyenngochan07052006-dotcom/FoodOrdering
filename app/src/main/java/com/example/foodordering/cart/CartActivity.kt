package com.example.foodordering.cart

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.CartDisplayItem
import com.example.foodordering.order.OrdersActivity
import com.example.foodordering.utils.QrHelper
import com.example.foodordering.utils.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button

    private val cartItems = mutableListOf<CartDisplayItem>()
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.isAdmin()) {
            finish()
            return
        }

        // Nút quay lại
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvCart = findViewById(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)

        adapter = CartAdapter(
            cartItems,
            onIncrease = { item -> updateQuantity(item, item.quantity + 1) },
            onDecrease = { item ->
                if (item.quantity > 1) {
                    updateQuantity(item, item.quantity - 1)
                } else {
                    deleteItem(item)
                }
            },
            onDelete = { item -> deleteItem(item) }
        )

        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        // Nút Đặt hàng → hiện QR (KHÔNG gọi CheckoutActivity)
        btnCheckout.setOnClickListener {
            placeOrderAndShowQr()
        }

        loadCart()
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    private fun loadCart() {
        cartItems.clear()
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
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        adapter.notifyDataSetChanged()
        updateTotal()
    }

    private fun updateQuantity(item: CartDisplayItem, newQty: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("quantity", newQty)
        }
        db.update(
            DatabaseHelper.TABLE_CART,
            values,
            "id = ?",
            arrayOf(item.cartId.toString())
        )
        db.close()
        loadCart()
    }

    private fun deleteItem(item: CartDisplayItem) {
        AlertDialog.Builder(this)
            .setTitle("Xóa món")
            .setMessage("Bạn có muốn xóa ${item.name} khỏi giỏ hàng?")
            .setPositiveButton("Xóa") { _, _ ->
                val db = dbHelper.writableDatabase
                db.delete(DatabaseHelper.TABLE_CART, "id = ?", arrayOf(item.cartId.toString()))
                db.close()
                loadCart()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.total }
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvTotal.text = "Tổng tiền: ${formatter.format(total)}"

        btnCheckout.isEnabled = cartItems.isNotEmpty()
        btnCheckout.alpha = if (cartItems.isEmpty()) 0.5f else 1f
    }

    // =====================================================
    // THANH TOÁN QR MÔ PHỎNG
    // =====================================================

    private fun placeOrderAndShowQr() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show()
            return
        }

        val total = cartItems.sumOf { it.total }
        val orderCode = "DH${System.currentTimeMillis() % 100000}"

        val orderId = saveOrderToDatabase(orderCode, total)

        if (orderId == -1L) {
            Toast.makeText(this, "Lỗi tạo đơn hàng", Toast.LENGTH_SHORT).show()
            return
        }

        showQrDialog(orderCode, total, orderId)
    }

    private fun showQrDialog(orderCode: String, total: Double, orderId: Long) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_payment, null)

        val imgQr = dialogView.findViewById<ImageView>(R.id.imgQr)
        val tvAmount = dialogView.findViewById<TextView>(R.id.tvQrAmount)
        val btnPaid = dialogView.findViewById<View>(R.id.btnPaid)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancelQr)

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvAmount.text = "Số tiền: ${formatter.format(total)}"

        val qrContent = """
            MÃ QR MÔ PHỎNG - KHÔNG MẤT TIỀN
            Ngân hàng: Vietcombank
            STK: 0123456789
            Chủ TK: QUAN CA PHE DEMO
            Số tiền: ${total.toLong()}
            Nội dung: $orderCode
        """.trimIndent()

        imgQr.setImageBitmap(QrHelper.generateQr(qrContent))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnPaid.setOnClickListener {
            updateOrderStatus(orderId, "paid")
            clearCart()
            dialog.dismiss()
            Toast.makeText(this, "Thanh toán thành công! Đơn #$orderCode", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }

        btnCancel.setOnClickListener {
            deleteOrder(orderId)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveOrderToDatabase(orderCode: String, total: Double): Long {
        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val createdAt = dateFormat.format(Date())

            val orderValues = ContentValues().apply {
                put("user_id", userId)
                put("order_code", orderCode)
                put("total", total)
                put("status", "pending")
                put("created_at", createdAt)
            }
            val orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, orderValues)

            if (orderId == -1L) return -1L

            for (item in cartItems) {
                val detailValues = ContentValues().apply {
                    put("order_id", orderId)
                    put("product_id", item.productId)
                    put("product_name", item.name)
                    put("quantity", item.quantity)
                    put("price", item.price)
                }
                db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, detailValues)
            }

            orderId
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        } finally {
            db.close()
        }
    }

    private fun updateOrderStatus(orderId: Long, status: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        db.update(
            DatabaseHelper.TABLE_ORDERS,
            values,
            "id = ?",
            arrayOf(orderId.toString())
        )
        db.close()
    }

    private fun deleteOrder(orderId: Long) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_ORDER_ITEMS, "order_id = ?", arrayOf(orderId.toString()))
        db.delete(DatabaseHelper.TABLE_ORDERS, "id = ?", arrayOf(orderId.toString()))
        db.close()
    }

    private fun clearCart() {
        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_CART, "user_id = ?", arrayOf(userId.toString()))
        db.close()
        cartItems.clear()
        adapter.notifyDataSetChanged()
        updateTotal()
    }
}