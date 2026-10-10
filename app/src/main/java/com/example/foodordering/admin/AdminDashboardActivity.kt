package com.example.foodordering.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private lateinit var tvPendingCount: TextView
    private lateinit var tvPreparingCount: TextView
    private lateinit var tvReadyCount: TextView
    private lateinit var tvCompletedCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Chỉ cho phép Admin
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvPreparingCount = findViewById(R.id.tvPreparingCount)
        tvReadyCount = findViewById(R.id.tvReadyCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        findViewById<MaterialCardView>(R.id.cardManageOrders).setOnClickListener {
            startActivity(Intent(this, AdminOrdersActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardManageProducts).setOnClickListener {
            startActivity(Intent(this, AdminProductsActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardManageInventory).setOnClickListener {
            startActivity(Intent(this, AdminInventoryActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardManageProducts).setOnClickListener {
            startActivity(Intent(this, AdminProductsActivity::class.java))
        }
        loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    private fun loadStatistics() {
        val db = dbHelper.readableDatabase

        // Đếm theo từng trạng thái
        val pending = countOrdersByStatus(db, listOf("pending", "paid"))
        val preparing = countOrdersByStatus(db, listOf("confirmed", "preparing"))
        val ready = countOrdersByStatus(db, listOf("ready"))
        val completed = countOrdersByStatus(db, listOf("completed"))

        tvPendingCount.text = pending.toString()
        tvPreparingCount.text = preparing.toString()
        tvReadyCount.text = ready.toString()
        tvCompletedCount.text = completed.toString()

        db.close()
    }

    private fun countOrdersByStatus(db: android.database.sqlite.SQLiteDatabase, statuses: List<String>): Int {
        if (statuses.isEmpty()) return 0

        val placeholders = statuses.joinToString(",") { "?" }
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_ORDERS} WHERE status IN ($placeholders)",
            statuses.toTypedArray()
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}