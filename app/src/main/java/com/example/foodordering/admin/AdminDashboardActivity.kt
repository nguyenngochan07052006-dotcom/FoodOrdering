package com.example.foodordering.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.utils.SessionManager

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)

        // Chỉ cho Admin vào
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<TextView>(R.id.tvAdminName).text = "Xin chào, ${sessionManager.getName()}"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}