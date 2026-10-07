package com.example.foodordering.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.R

class AdminProductsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tạm thời dùng layout trống hoặc layout bất kỳ
        setContentView(R.layout.activity_admin_dashboard) // dùng tạm
        Toast.makeText(this, "Chức năng Quản lý sản phẩm sẽ được hoàn thiện sau", Toast.LENGTH_SHORT).show()
        finish()
    }
}