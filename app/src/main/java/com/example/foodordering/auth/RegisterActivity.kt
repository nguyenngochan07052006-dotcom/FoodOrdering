package com.example.foodordering.auth

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            register()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun register() {
        val name = edtName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase

        // Kiểm tra email đã tồn tại chưa
        val cursor = db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_USERS} WHERE email = ?",
            arrayOf(email)
        )
        if (cursor.count > 0) {
            cursor.close()
            db.close()
            Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show()
            return
        }
        cursor.close()

        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
            put("role", "USER")
        }

        val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()

        if (result != -1L) {
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}