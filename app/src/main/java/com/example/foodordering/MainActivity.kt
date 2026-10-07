package com.example.foodordering

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.admin.AdminDashboardActivity
import com.example.foodordering.product.HomeActivity
import com.example.foodordering.utils.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                // Đã đăng nhập Admin → vào Dashboard
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            } else {
                // Đã đăng nhập User → vào Home
                startActivity(Intent(this, HomeActivity::class.java))
            }
        } else {
            // Chưa đăng nhập → vào Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // đóng MainActivity luôn
    }
}