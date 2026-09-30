package com.example.foodordering.database

import android.content.ContentValues
import android.content.Context

class DatabaseSeeder(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun seedIfNeeded() {
        val db = dbHelper.writableDatabase

        // Kiểm tra đã có dữ liệu chưa
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS}", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count > 0) {
            db.close()
            return // Đã có dữ liệu rồi thì không seed nữa
        }

        // ===== Tạo tài khoản Admin =====
        val admin = ContentValues().apply {
            put("name", "Admin")
            put("email", "admin@cafe.com")
            put("password", "admin123")
            put("role", "ADMIN")
        }
        db.insert(DatabaseHelper.TABLE_USERS, null, admin)

        // ===== Tạo tài khoản User =====
        val user = ContentValues().apply {
            put("name", "Nguyen Van A")
            put("email", "user@gmail.com")
            put("password", "user123")
            put("role", "USER")
        }
        db.insert(DatabaseHelper.TABLE_USERS, null, user)

        // ===== Tạo danh mục =====
        val categories = listOf("Cà phê", "Trà sữa", "Trà trái cây", "Đá xay", "Bánh ngọt")
        categories.forEach { name ->
            val values = ContentValues().apply {
                put("name", name)
            }
            db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
        }

        // ===== Tạo sản phẩm mẫu =====
        val products = listOf(
            // category_id, name, price, description, image, status
            arrayOf(1, "Cà phê đen", 25000.0, "Cà phê phin truyền thống", "", "available"),
            arrayOf(1, "Cà phê sữa", 30000.0, "Cà phê sữa đá đậm đà", "", "available"),
            arrayOf(1, "Bạc xỉu", 32000.0, "Bạc xỉu đá ngọt dịu", "", "available"),
            arrayOf(2, "Trà sữa truyền thống", 35000.0, "Trà sữa đậm vị", "", "available"),
            arrayOf(2, "Trà sữa matcha", 40000.0, "Matcha Nhật Bản", "", "available"),
            arrayOf(2, "Trà sữa khoai môn", 42000.0, "Khoai môn béo thơm", "", "available"),
            arrayOf(3, "Trà đào", 38000.0, "Trà đào cam sả", "", "available"),
            arrayOf(3, "Trà vải", 38000.0, "Trà vải thiều", "", "available"),
            arrayOf(4, "Đá xay chocolate", 45000.0, "Chocolate đá xay béo", "", "available"),
            arrayOf(5, "Bánh tiramisu", 35000.0, "Bánh tiramisu mềm mịn", "", "available")
        )

        products.forEach { p ->
            val values = ContentValues().apply {
                put("category_id", p[0] as Int)
                put("name", p[1] as String)
                put("price", p[2] as Double)
                put("description", p[3] as String)
                put("image", p[4] as String)
                put("status", p[5] as String)
            }
            db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values)
        }

        db.close()
    }
}