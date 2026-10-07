package com.example.foodordering.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.admin.AdminDashboardActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.product.HomeActivity
import com.example.foodordering.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Nếu đã đăng nhập rồi thì chuyển thẳng
        if (sessionManager.isLoggedIn()) {
            navigateByRole()
            return
        }

        val edtPhone = findViewById<TextInputEditText>(R.id.edtPhone)
        val edtPassword = findViewById<TextInputEditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(phone, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(phone: String, password: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, role FROM ${DatabaseHelper.TABLE_USERS} WHERE phone = ? AND password = ?",
            arrayOf(phone, password)
        )

        if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))

            // Lưu session
            sessionManager.createLoginSession(userId, name, phone, role)

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            navigateByRole()
        } else {
            Toast.makeText(this, "Sai số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
        db.close()
    }

    private fun navigateByRole() {
        if (sessionManager.isAdmin()) {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        finish()
    }
}