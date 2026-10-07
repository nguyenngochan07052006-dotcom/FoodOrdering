package com.example.foodordering.auth

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        val edtName = findViewById<TextInputEditText>(R.id.edtName)
        val edtPhone = findViewById<TextInputEditText>(R.id.edtPhone)
        val edtPassword = findViewById<TextInputEditText>(R.id.edtPassword)
        val edtConfirmPassword = findViewById<TextInputEditText>(R.id.edtConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val name = edtName.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registerUser(name, phone, password)
                }
            }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(name: String, phone: String, password: String) {
        val db = dbHelper.writableDatabase

        // Kiểm tra số điện thoại đã tồn tại chưa
        val cursor = db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_USERS} WHERE phone = ?",
            arrayOf(phone)
        )

        if (cursor.moveToFirst()) {
            Toast.makeText(this, "Số điện thoại đã được sử dụng", Toast.LENGTH_SHORT).show()
            cursor.close()
            db.close()
            return
        }
        cursor.close()

        // Thêm tài khoản mới với role = user
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
            put("password", password)
            put("role", "user")
        }

        val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()

        if (result != -1L) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
        }
    }
}