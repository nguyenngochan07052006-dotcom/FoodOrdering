package com.example.foodordering.product

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.cart.CartActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.Category
import com.example.foodordering.order.OrdersActivity
import com.example.foodordering.profile.ProfileActivity
import com.example.foodordering.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvCategoryTitle: TextView

    private val categories = mutableListOf<Category>()
    private val allProducts = mutableListOf<Product>()
    private val displayedProducts = mutableListOf<Product>()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private var selectedCategoryId: Int = -1   // -1 = tất cả

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        // Kiểm tra quyền
        if (!sessionManager.isLoggedIn() || sessionManager.isAdmin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)
        val cardAvatar = findViewById<MaterialCardView>(R.id.cardAvatar)

        rvCategories = findViewById(R.id.rvCategories)
        rvProducts = findViewById(R.id.rvProducts)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)

        val name = sessionManager.getName()
        tvWelcome.text = "Xin chào, $name!"
        if (name.isNotEmpty()) {
            tvAvatar.text = name.first().uppercaseChar().toString()
        }

        // Avatar → Profile
        cardAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Nút Giỏ hàng
        findViewById<Button>(R.id.btnCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Nút Đơn hàng
        findViewById<Button>(R.id.btnOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        setupAdapters()
        loadCategories()
        loadAllProducts()
    }

    private fun setupAdapters() {
        // Category Adapter
        categoryAdapter = CategoryAdapter(categories, selectedCategoryId) { category ->
            selectedCategoryId = category.id
            tvCategoryTitle.text = category.name
            filterProducts()
        }
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        // Product Adapter
        productAdapter = ProductAdapter(displayedProducts) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            intent.putExtra("PRODUCT_NAME", product.name)
            intent.putExtra("PRODUCT_PRICE", product.price)
            intent.putExtra("PRODUCT_DESC", product.description)
            intent.putExtra("PRODUCT_IMAGE", product.image)
            startActivity(intent)
        }
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }

    private fun loadCategories() {
        categories.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM ${DatabaseHelper.TABLE_CATEGORIES}", null)

        // Thêm mục "Tất cả"
        categories.add(Category(id = -1, name = "Tất cả"))

        if (cursor.moveToFirst()) {
            do {
                categories.add(
                    Category(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        categoryAdapter.notifyDataSetChanged()
    }

    private fun loadAllProducts() {
        allProducts.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE status = 'available'",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val product = Product(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
                    image = cursor.getString(cursor.getColumnIndexOrThrow("image")) ?: "",
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                )
                allProducts.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        filterProducts()
    }

    private fun filterProducts() {
        displayedProducts.clear()

        if (selectedCategoryId == -1) {
            displayedProducts.addAll(allProducts)
            tvCategoryTitle.text = "Tất cả món"
        } else {
            displayedProducts.addAll(allProducts.filter { it.categoryId == selectedCategoryId })
        }

        productAdapter.notifyDataSetChanged()
    }
}