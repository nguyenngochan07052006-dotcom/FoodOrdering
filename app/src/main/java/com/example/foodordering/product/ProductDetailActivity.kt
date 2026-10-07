package com.example.foodordering.product

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var productId: Int = -1
    private var productName: String = ""
    private var productPrice: Double = 0.0
    private var productDesc: String = ""
    private var productStatus: String = "available"
    private var quantity: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Nút quay lại
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Lấy dữ liệu từ Intent
        productId = intent.getIntExtra("PRODUCT_ID", -1)
        productName = intent.getStringExtra("PRODUCT_NAME") ?: ""
        productPrice = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        productDesc = intent.getStringExtra("PRODUCT_DESC") ?: ""
        productStatus = intent.getStringExtra("PRODUCT_STATUS") ?: "available"

        val tvName = findViewById<TextView>(R.id.tvProductName)
        val tvPrice = findViewById<TextView>(R.id.tvProductPrice)
        val tvDesc = findViewById<TextView>(R.id.tvProductDesc)
        val tvQuantity = findViewById<TextView>(R.id.tvQuantity)
        val btnMinus = findViewById<Button>(R.id.btnMinus)
        val btnPlus = findViewById<Button>(R.id.btnPlus)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        tvName.text = productName
        tvPrice.text = formatter.format(productPrice)
        tvDesc.text = productDesc
        tvQuantity.text = quantity.toString()

        // ===== KIỂM TRA MÓN UNAVAILABLE =====
        if (productStatus != "available") {
            Toast.makeText(this, "Món này hiện không còn bán", Toast.LENGTH_LONG).show()
            btnAddToCart.isEnabled = false
            btnAddToCart.text = "Hết hàng"
            btnAddToCart.alpha = 0.5f

            // Khóa luôn nút tăng giảm số lượng
            btnMinus.isEnabled = false
            btnPlus.isEnabled = false
            btnMinus.alpha = 0.5f
            btnPlus.alpha = 0.5f
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }

        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun addToCart() {
        // Kiểm tra lại lần nữa trước khi thêm
        if (productStatus != "available") {
            Toast.makeText(this, "Món này hiện không còn bán", Toast.LENGTH_SHORT).show()
            return
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId()
        val db = dbHelper.writableDatabase

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        val cursor = db.rawQuery(
            "SELECT id, quantity FROM ${DatabaseHelper.TABLE_CART} WHERE user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString())
        )

        if (cursor.moveToFirst()) {
            // Đã có → tăng số lượng
            val cartId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val oldQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            val newQty = oldQty + quantity

            val values = ContentValues().apply {
                put("quantity", newQty)
            }
            db.update(DatabaseHelper.TABLE_CART, values, "id = ?", arrayOf(cartId.toString()))
        } else {
            // Chưa có → thêm mới
            val values = ContentValues().apply {
                put("user_id", userId)
                put("product_id", productId)
                put("quantity", quantity)
            }
            db.insert(DatabaseHelper.TABLE_CART, null, values)
        }

        cursor.close()
        db.close()

        Toast.makeText(this, "Đã thêm $quantity $productName vào giỏ", Toast.LENGTH_SHORT).show()
        finish()
    }
}