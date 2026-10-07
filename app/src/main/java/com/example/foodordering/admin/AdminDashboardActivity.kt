package com.example.foodordering.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        // Kiểm tra quyền Admin
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvWelcome = findViewById<TextView>(R.id.tvAdminWelcome)
        val tvPending = findViewById<TextView>(R.id.tvPendingCount)
        val tvPreparing = findViewById<TextView>(R.id.tvPreparingCount)
        val tvCompleted = findViewById<TextView>(R.id.tvCompletedCount)

        tvWelcome.text = "Xin chào, ${sessionManager.getName()}"

        // Đếm đơn theo trạng thái
        updateCounts(tvPending, tvPreparing, tvCompleted)

        findViewById<Button>(R.id.btnManageOrders).setOnClickListener {
            startActivity(Intent(this, OrderManagementActivity::class.java))
        }

        findViewById<Button>(R.id.btnManageProducts).setOnClickListener {
            android.widget.Toast.makeText(this, "Tính năng đang được phát triển", android.widget.Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLogoutAdmin).setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        val tvPending = findViewById<TextView>(R.id.tvPendingCount)
        val tvPreparing = findViewById<TextView>(R.id.tvPreparingCount)
        val tvCompleted = findViewById<TextView>(R.id.tvCompletedCount)
        updateCounts(tvPending, tvPreparing, tvCompleted)
    }

    private fun updateCounts(tvPending: TextView, tvPreparing: TextView, tvCompleted: TextView) {
        val db = dbHelper.readableDatabase

        fun countByStatus(status: String): Int {
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_ORDERS} WHERE status = ?",
                arrayOf(status)
            )
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()
            return count
        }

        tvPending.text = countByStatus("PENDING").toString()
        tvPreparing.text = (countByStatus("CONFIRMED") + countByStatus("PREPARING") + countByStatus("READY")).toString()
        tvCompleted.text = countByStatus("COMPLETED").toString()

        db.close()
    }
}