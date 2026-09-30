package com.example.foodordering.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.database.DatabaseSeeder
import com.example.foodordering.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Seed dữ liệu lần đầu
        DatabaseSeeder(this).seedIfNeeded()

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        // Nếu đã đăng nhập rồi thì chuyển thẳng
        if (sessionManager.isLoggedIn()) {
            navigateByRole()
            return
        }

        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            login()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))

            sessionManager.saveLogin(id, name, email, role)
            cursor.close()
            db.close()

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            navigateByRole()
        } else {
            cursor.close()
            db.close()
            Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateByRole() {
        if (sessionManager.isAdmin()) {
            startActivity(Intent(this, com.example.foodordering.admin.AdminDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, com.example.foodordering.product.HomeActivity::class.java))
        }
        finish()
    }
}