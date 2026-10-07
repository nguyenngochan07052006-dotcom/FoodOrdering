package com.example.foodordering.auth

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.utils.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        edtName = findViewById(R.id.edtName)
        edtPhone = findViewById(R.id.edtPhone)
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
        val phone = edtPhone.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra số điện thoại hợp lệ (đơn giản)
        if (phone.length < 9 || phone.length > 11) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase

        // Kiểm tra số điện thoại đã tồn tại chưa
        val cursor = db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_USERS} WHERE phone = ?",
            arrayOf(phone)
        )
        if (cursor.count > 0) {
            cursor.close()
            db.close()
            Toast.makeText(this, "Số điện thoại đã được sử dụng", Toast.LENGTH_SHORT).show()
            return
        }
        cursor.close()

        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
            put("password", password)
            put("role", "USER")
        }

        val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()

        if (result != -1L) {
            val session = SessionManager(this)
            session.addRecentPhone(phone)

            AlertDialog.Builder(this)
                .setTitle("Đăng ký thành công")
                .setMessage("Bạn có muốn lưu tài khoản này để lần sau đăng nhập nhanh không?")
                .setPositiveButton("Lưu") { _, _ ->
                    session.saveRememberAccount(phone, password)
                    Toast.makeText(this, "Đã lưu tài khoản", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Không") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}