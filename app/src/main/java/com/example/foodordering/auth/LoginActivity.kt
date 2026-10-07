package com.example.foodordering.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R
import com.example.foodordering.admin.AdminDashboardActivity
import com.example.foodordering.database.DatabaseHelper
import com.example.foodordering.database.DatabaseSeeder
import com.example.foodordering.product.HomeActivity
import com.example.foodordering.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var edtPhone: AutoCompleteTextView
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

        edtPhone = findViewById(R.id.edtPhone)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Gợi ý số điện thoại gần đây
        setupPhoneSuggestions()

        // Tự điền nếu đã chọn Lưu trước đó
        if (sessionManager.isRemember()) {
            edtPhone.setText(sessionManager.getRememberPhone())
            edtPassword.setText(sessionManager.getRememberPassword())
        }

        btnLogin.setOnClickListener {
            login()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupPhoneSuggestions() {
        val recentPhones = sessionManager.getRecentPhones()

        if (recentPhones.isNotEmpty()) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                recentPhones
            )
            edtPhone.setAdapter(adapter)

            edtPhone.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    edtPhone.showDropDown()
                }
            }

            edtPhone.setOnClickListener {
                edtPhone.showDropDown()
            }
        }
    }

    private fun login() {
        val phone = edtPhone.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE phone = ? AND password = ?",
            arrayOf(phone, password)
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))

            sessionManager.saveLogin(id, name, phone, role)

            // Lưu vào danh sách số điện thoại gần đây
            sessionManager.addRecentPhone(phone)

            cursor.close()
            db.close()

            // Hỏi có muốn lưu tài khoản không
            AlertDialog.Builder(this)
                .setTitle("Lưu tài khoản?")
                .setMessage("Bạn có muốn lưu số điện thoại và mật khẩu để lần sau đăng nhập nhanh hơn không?")
                .setPositiveButton("Lưu") { _, _ ->
                    sessionManager.saveRememberAccount(phone, password)
                    Toast.makeText(this, "Đã lưu tài khoản", Toast.LENGTH_SHORT).show()
                    navigateByRole()
                }
                .setNegativeButton("Không") { _, _ ->
                    sessionManager.clearRememberAccount()
                    navigateByRole()
                }
                .setCancelable(false)
                .show()

        } else {
            cursor.close()
            db.close()
            Toast.makeText(this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
        }
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