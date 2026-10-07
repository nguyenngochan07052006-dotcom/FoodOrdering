package com.example.foodordering.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.CartDisplayItem
import com.example.foodordering.order.CheckoutActivity
import com.example.foodordering.utils.SessionManager
import java.text.NumberFormat
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

        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Chuyển sang màn Checkout
            startActivity(Intent(this, CheckoutActivity::class.java))
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
        val values = android.content.ContentValues().apply {
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
}