package com.example.foodordering.admin

import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.model.InventoryItem
import com.example.foodordering.utils.SessionManager
import com.google.android.material.chip.ChipGroup

class AdminInventoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var rvInventory: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var chipGroup: ChipGroup

    private val allItems = mutableListOf<InventoryItem>()
    private val displayedItems = mutableListOf<InventoryItem>()
    private lateinit var adapter: AdminInventoryAdapter

    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_inventory)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvInventory = findViewById(R.id.rvInventory)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvLowStock = findViewById(R.id.tvLowStock)
        chipGroup = findViewById(R.id.chipGroup)

        adapter = AdminInventoryAdapter(displayedItems) { item ->
            showUpdateQuantityDialog(item)
        }
        rvInventory.layoutManager = LinearLayoutManager(this)
        rvInventory.adapter = adapter

        setupFilter()
        loadInventory()
    }

    private fun setupFilter() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            currentFilter = when (checkedIds[0]) {
                R.id.chipBaoBi -> "Bao bì"
                R.id.chipTopping -> "Topping"
                R.id.chipNguyenLieu -> "Nguyên liệu nền"
                R.id.chipSiro -> "Siro & hương vị"
                else -> "all"
            }
            filterItems()
        }
    }

    private fun loadInventory() {
        allItems.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id, code, name, group_name, unit, quantity, min_quantity, status
            FROM ${DatabaseHelper.TABLE_INVENTORY}
            ORDER BY group_name, name
            """.trimIndent(),
            null
        )

        if (cursor.moveToFirst()) {
            do {
                allItems.add(
                    InventoryItem(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        code = cursor.getString(cursor.getColumnIndexOrThrow("code")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        groupName = cursor.getString(cursor.getColumnIndexOrThrow("group_name")),
                        unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        minQuantity = cursor.getDouble(cursor.getColumnIndexOrThrow("min_quantity")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        // Cập nhật thống kê
        tvTotalItems.text = allItems.size.toString()
        tvLowStock.text = allItems.count { it.quantity <= it.minQuantity }.toString()

        filterItems()
    }

    private fun filterItems() {
        displayedItems.clear()

        if (currentFilter == "all") {
            displayedItems.addAll(allItems)
        } else {
            displayedItems.addAll(allItems.filter { it.groupName == currentFilter })
        }

        adapter.notifyDataSetChanged()

        if (displayedItems.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvInventory.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvInventory.visibility = View.VISIBLE
        }
    }

    private fun showUpdateQuantityDialog(item: InventoryItem) {
        val input = EditText(this).apply {
            hint = "Nhập số lượng mới"
            setText(item.quantity.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Cập nhật tồn kho")
            .setMessage("${item.name}\nĐơn vị: ${item.unit}")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newQtyStr = input.text.toString().trim()
                val newQty = newQtyStr.toDoubleOrNull()

                if (newQty == null || newQty < 0) {
                    Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateQuantity(item.id, newQty)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateQuantity(id: Int, newQuantity: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("quantity", newQuantity)
        }
        val rows = db.update(
            DatabaseHelper.TABLE_INVENTORY,
            values,
            "id = ?",
            arrayOf(id.toString())
        )
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            loadInventory()
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}