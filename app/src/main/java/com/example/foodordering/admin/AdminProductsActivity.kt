package com.example.foodordering.admin

import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.Product
import com.example.foodordering.utils.SessionManager
import com.google.android.material.button.MaterialButton

class AdminProductsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var rvProducts: RecyclerView
    private lateinit var tvEmpty: TextView

    private val products = mutableListOf<Product>()
    private val categoryMap = mutableMapOf<Int, String>()   // id -> name
    private val categoryList = mutableListOf<Pair<Int, String>>() // để Spinner

    private lateinit var adapter: AdminProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_products)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnAddProduct).setOnClickListener {
            showProductDialog(null)
        }

        rvProducts = findViewById(R.id.rvProducts)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = AdminProductAdapter(
            products = products,
            categoryMap = categoryMap,
            onEdit = { product -> showProductDialog(product) },
            onToggleStatus = { product -> toggleStatus(product) },
            onDelete = { product -> confirmDelete(product) }
        )
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter

        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        categoryMap.clear()
        categoryList.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM ${DatabaseHelper.TABLE_CATEGORIES}", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                categoryMap[id] = name
                categoryList.add(id to name)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    private fun loadProducts() {
        products.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id, category_id, name, price, description, image, status, label
            FROM ${DatabaseHelper.TABLE_PRODUCTS}
            ORDER BY id DESC
            """.trimIndent(),
            null
        )

        if (cursor.moveToFirst()) {
            do {
                products.add(
                    Product(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
                        image = cursor.getString(cursor.getColumnIndexOrThrow("image")) ?: "",
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        label = cursor.getString(cursor.getColumnIndexOrThrow("label")) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        adapter.notifyDataSetChanged()

        if (products.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvProducts.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
        }
    }

    // ==================== THÊM / SỬA ====================
    private fun showProductDialog(product: Product?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_form, null)

        val edtName = dialogView.findViewById<EditText>(R.id.edtName)
        val edtPrice = dialogView.findViewById<EditText>(R.id.edtPrice)
        val edtDesc = dialogView.findViewById<EditText>(R.id.edtDesc)
        val edtLabel = dialogView.findViewById<EditText>(R.id.edtLabel)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        // Spinner danh mục
        val categoryNames = categoryList.map { it.second }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        // Nếu là sửa thì điền sẵn
        if (product != null) {
            edtName.setText(product.name)
            edtPrice.setText(product.price.toString())
            edtDesc.setText(product.description)
            edtLabel.setText(product.label)

            val catIndex = categoryList.indexOfFirst { it.first == product.categoryId }
            if (catIndex >= 0) spinnerCategory.setSelection(catIndex)
        }

        AlertDialog.Builder(this)
            .setTitle(if (product == null) "Thêm sản phẩm" else "Sửa sản phẩm")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = edtName.text.toString().trim()
                val priceStr = edtPrice.text.toString().trim()
                val desc = edtDesc.text.toString().trim()
                val label = edtLabel.text.toString().trim()
                val selectedCatIndex = spinnerCategory.selectedItemPosition

                if (name.isEmpty() || priceStr.isEmpty() || selectedCatIndex < 0) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = priceStr.toDoubleOrNull()
                if (price == null || price <= 0) {
                    Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val categoryId = categoryList[selectedCatIndex].first

                if (product == null) {
                    insertProduct(categoryId, name, price, desc, label)
                } else {
                    updateProduct(product.id, categoryId, name, price, desc, label)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun insertProduct(categoryId: Int, name: String, price: Double, desc: String, label: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category_id", categoryId)
            put("name", name)
            put("price", price)
            put("description", desc)
            put("image", "")
            put("status", "available")
            put("label", label)
        }
        val result = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values)
        db.close()

        if (result != -1L) {
            Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
            loadProducts()
        } else {
            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProduct(id: Int, categoryId: Int, name: String, price: Double, desc: String, label: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category_id", categoryId)
            put("name", name)
            put("price", price)
            put("description", desc)
            put("label", label)
        }
        val rows = db.update(DatabaseHelper.TABLE_PRODUCTS, values, "id = ?", arrayOf(id.toString()))
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            loadProducts()
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== ẨN / HIỆN ====================
    private fun toggleStatus(product: Product) {
        val newStatus = if (product.status == "available") "unavailable" else "available"
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", newStatus)
        }
        db.update(DatabaseHelper.TABLE_PRODUCTS, values, "id = ?", arrayOf(product.id.toString()))
        db.close()

        Toast.makeText(this, if (newStatus == "available") "Đã hiện sản phẩm" else "Đã ẩn sản phẩm", Toast.LENGTH_SHORT).show()
        loadProducts()
    }

    // ==================== XÓA ====================
    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa \"${product.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteProduct(id: Int) {
        val db = dbHelper.writableDatabase
        val rows = db.delete(DatabaseHelper.TABLE_PRODUCTS, "id = ?", arrayOf(id.toString()))
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
            loadProducts()
        } else {
            Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}