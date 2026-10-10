package com.example.foodordering.product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.foodordering.model.Product
import com.example.foodordering.order.OrdersActivity
import com.example.foodordering.profile.ProfileActivity
import com.example.foodordering.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var edtSearch: TextInputEditText

    private val categories = mutableListOf<Category>()
    private val allProducts = mutableListOf<Product>()
    private val displayedProducts = mutableListOf<Product>()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private var selectedCategoryId: Int = -1
    private var currentKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

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
        edtSearch = findViewById(R.id.edtSearch)

        val name = sessionManager.getName()
        tvWelcome.text = "Xin chào, $name!"
        if (name.isNotEmpty()) {
            tvAvatar.text = name.first().uppercaseChar().toString()
        }

        cardAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<Button>(R.id.btnOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        setupAdapters()
        setupSearch()
        loadCategories()
        loadAllProducts()
    }

    private fun setupAdapters() {
        categoryAdapter = CategoryAdapter(categories, selectedCategoryId) { category ->
            selectedCategoryId = category.id
            currentKeyword = ""
            edtSearch.setText("")
            tvCategoryTitle.text = if (category.id == -1) "Tất cả món" else category.name
            filterProducts()
        }
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        productAdapter = ProductAdapter(displayedProducts) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            intent.putExtra("PRODUCT_NAME", product.name)
            intent.putExtra("PRODUCT_PRICE", product.price)
            intent.putExtra("PRODUCT_DESC", product.description)
            intent.putExtra("PRODUCT_IMAGE", product.image)
            intent.putExtra("PRODUCT_STATUS", product.status)
            startActivity(intent)
        }
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }

    private fun setupSearch() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s?.toString()?.trim() ?: ""
                filterProducts()
            }
        })
    }

    private fun loadCategories() {
        categories.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM ${DatabaseHelper.TABLE_CATEGORIES}", null)

        categories.add(Category(id = -1, name = "Tất cả"))
        categories.add(Category(id = -2, name = "Bán chạy")) // danh mục ảo

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
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    label = cursor.getString(cursor.getColumnIndexOrThrow("label")) ?: ""
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

        var list = allProducts.toList()

        // Lọc theo danh mục
        when (selectedCategoryId) {
            -1 -> { /* tất cả */ }
            -2 -> { // Bán chạy: lấy top theo số lần xuất hiện trong order_items
                list = getBestSellingProducts()
            }
            else -> {
                list = list.filter { it.categoryId == selectedCategoryId }
            }
        }

        // Lọc theo từ khóa tìm kiếm
        if (currentKeyword.isNotEmpty()) {
            list = list.filter {
                it.name.contains(currentKeyword, ignoreCase = true) ||
                        it.description.contains(currentKeyword, ignoreCase = true)
            }
            tvCategoryTitle.text = "Kết quả: \"$currentKeyword\""
        } else {
            tvCategoryTitle.text = when (selectedCategoryId) {
                -1 -> "Tất cả món"
                -2 -> "Món bán chạy"
                else -> categories.find { it.id == selectedCategoryId }?.name ?: "Món"
            }
        }

        displayedProducts.addAll(list)
        productAdapter.notifyDataSetChanged()
    }

    private fun getBestSellingProducts(): List<Product> {
        val db = dbHelper.readableDatabase
        val countMap = mutableMapOf<Int, Int>()

        val cursor = db.rawQuery(
            """
            SELECT product_id, SUM(quantity) as total_qty
            FROM ${DatabaseHelper.TABLE_ORDER_ITEMS}
            GROUP BY product_id
            ORDER BY total_qty DESC
            LIMIT 10
            """.trimIndent(),
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val pid = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))
                val qty = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"))
                countMap[pid] = qty
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return allProducts
            .filter { countMap.containsKey(it.id) }
            .sortedByDescending { countMap[it.id] }
    }
}