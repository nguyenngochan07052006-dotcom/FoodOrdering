package com.example.foodordering.product

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.auth.LoginActivity
import com.example.foodordering.utils.SessionManager

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)

        // Kiểm tra quyền: nếu không phải USER thì đá về Login
        if (!sessionManager.isLoggedIn() || sessionManager.isAdmin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Xin chào, ${sessionManager.getName()}!"
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)

        val name = sessionManager.getName()
        tvWelcome.text = "Xin chào, $name!"

// Lấy chữ cái đầu làm avatar
        if (name.isNotEmpty()) {
            tvAvatar.text = name.first().uppercaseChar().toString()
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}