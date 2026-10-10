package com.example.foodordering.auth

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.admin.AdminDashboardActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.product.HomeActivity
import com.example.foodordering.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            navigateByRole()
            return
        }

        val edtPhone = findViewById<AutoCompleteTextView>(R.id.edtPhone)
        val edtPassword = findViewById<TextInputEditText>(R.id.edtPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            login(phone, password)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
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

    // ==================== QUÊN MẬT KHẨU ====================
    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Nhập số điện thoại"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Quên mật khẩu")
            .setMessage("Nhập số điện thoại đã đăng ký")
            .setView(input)
            .setPositiveButton("Tiếp tục") { _, _ ->
                val phone = input.text.toString().trim()
                if (phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                checkPhoneAndShowOtp(phone)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun checkPhoneAndShowOtp(phone: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_USERS} WHERE phone = ?",
            arrayOf(phone)
        )

        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Số điện thoại không tồn tại", Toast.LENGTH_SHORT).show()
            cursor.close()
            db.close()
            return
        }
        cursor.close()
        db.close()

        // Demo: mã OTP cố định
        val otpInput = EditText(this).apply {
            hint = "Nhập mã OTP (demo: 123456)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Xác minh OTP")
            .setMessage("Mã OTP đã gửi đến $phone\n(Demo dùng mã: 123456)")
            .setView(otpInput)
            .setPositiveButton("Xác nhận") { _, _ ->
                val otp = otpInput.text.toString().trim()
                if (otp == "123456") {
                    showNewPasswordDialog(phone)
                } else {
                    Toast.makeText(this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showNewPasswordDialog(phone: String) {
        val input = EditText(this).apply {
            hint = "Nhập mật khẩu mới (tối thiểu 6 ký tự)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Đặt mật khẩu mới")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newPass = input.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updatePassword(phone, newPass)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updatePassword(phone: String, newPassword: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("password", newPassword)
        }
        val rows = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "phone = ?",
            arrayOf(phone)
        )
        db.close()

        if (rows > 0) {
            Toast.makeText(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
        }
    }
}