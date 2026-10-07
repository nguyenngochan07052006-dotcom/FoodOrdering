package com.example.foodordering.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        // Kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        tvName.text = sessionManager.getName()
        tvPhone.text = sessionManager.getPhone()
        tvRole.text = if (sessionManager.isAdmin()) "Quản trị viên" else "Khách hàng"

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    sessionManager.logout()
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity() // đóng hết activity cũ
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
