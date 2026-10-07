package com.example.foodordering.database

import android.content.ContentValues
import android.content.Context

class DatabaseSeeder(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun seedIfNeeded() {
        val db = dbHelper.writableDatabase

        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS}", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count > 0) {
            db.close()
            return
        }

        // ===== Tài khoản Admin =====
        val admin = ContentValues().apply {
            put("name", "Admin")
            put("phone", "0900000000")
            put("password", "admin123")
            put("role", "ADMIN")
        }
        db.insert(DatabaseHelper.TABLE_USERS, null, admin)

        // ===== Tài khoản User demo =====
        val user = ContentValues().apply {
            put("name", "Nguyen Van A")
            put("phone", "0912345678")
            put("password", "user123")
            put("role", "USER")
        }
        db.insert(DatabaseHelper.TABLE_USERS, null, user)

        // ===== 5 Danh mục =====
        val categories = listOf(
            "Cà phê máy",
            "Trà sữa",
            "Trà trái cây",
            "Đá xay",
            "Bánh ngọt"
        )
        categories.forEach { name ->
            val values = ContentValues().apply { put("name", name) }
            db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
        }

        // ===== 40 sản phẩm =====
        // category_id: 1=Cà phê máy, 2=Trà sữa, 3=Trà trái cây, 4=Đá xay, 5=Bánh ngọt
        val products = listOf(
            // 1. Cà phê máy
            arrayOf(1, "Espresso", 35000.0, "Espresso đậm đà", "", "available"),
            arrayOf(1, "Espresso Doppio", 45000.0, "Double Espresso", "", "available"),
            arrayOf(1, "Americano", 40000.0, "Americano", "", "available"),
            arrayOf(1, "Cappuccino", 50000.0, "Cappuccino", "", "available"),
            arrayOf(1, "Latte", 50000.0, "Caffè Latte", "", "available"),
            arrayOf(1, "Mocha", 55000.0, "Caffè Mocha", "", "available"),
            arrayOf(1, "Caramel Macchiato", 60000.0, "Caramel Macchiato", "", "available"),
            arrayOf(1, "Cà phê sữa đá Espresso", 45000.0, "Iced Espresso with Condensed Milk", "", "available"),

            // 2. Trà sữa
            arrayOf(2, "Trà sữa truyền thống", 45000.0, "Classic Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa trân châu đường đen", 55000.0, "Brown Sugar Pearl Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa Ô Long", 50000.0, "Oolong Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa Thái xanh", 48000.0, "Thai Green Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa khoai môn", 50000.0, "Taro Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa Matcha", 55000.0, "Matcha Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa Socola", 52000.0, "Chocolate Milk Tea", "", "available"),
            arrayOf(2, "Trà sữa dâu", 52000.0, "Strawberry Milk Tea", "", "available"),

            // 3. Trà trái cây
            arrayOf(3, "Trà đào cam sả", 50000.0, "Peach Orange Lemongrass Tea", "", "available"),
            arrayOf(3, "Trà vải", 48000.0, "Lychee Tea", "", "available"),
            arrayOf(3, "Trà chanh dây", 48000.0, "Passion Fruit Tea", "", "available"),
            arrayOf(3, "Trà dâu", 50000.0, "Strawberry Tea", "", "available"),
            arrayOf(3, "Trà xoài", 50000.0, "Mango Tea", "", "available"),
            arrayOf(3, "Trà táo xanh", 48000.0, "Green Apple Tea", "", "available"),
            arrayOf(3, "Trà nhiệt đới", 55000.0, "Tropical Fruit Tea", "", "available"),
            arrayOf(3, "Trà tắc mật ong", 45000.0, "Honey Kumquat Tea", "", "available"),

            // 4. Đá xay
            arrayOf(4, "Cà phê đá xay", 58000.0, "Coffee Frappé", "", "available"),
            arrayOf(4, "Mocha đá xay", 62000.0, "Mocha Frappé", "", "available"),
            arrayOf(4, "Caramel đá xay", 62000.0, "Caramel Frappé", "", "available"),
            arrayOf(4, "Matcha đá xay", 60000.0, "Matcha Frappé", "", "available"),
            arrayOf(4, "Socola đá xay", 60000.0, "Chocolate Frappé", "", "available"),
            arrayOf(4, "Cookies & Cream đá xay", 65000.0, "Cookies & Cream Frappé", "", "available"),
            arrayOf(4, "Dâu đá xay", 60000.0, "Strawberry Frappé", "", "available"),
            arrayOf(4, "Việt quất đá xay", 62000.0, "Blueberry Frappé", "", "available"),

            // 5. Bánh ngọt
            arrayOf(5, "Bánh Tiramisu", 55000.0, "Tiramisu", "", "available"),
            arrayOf(5, "Bánh Red Velvet", 55000.0, "Red Velvet Cake", "", "available"),
            arrayOf(5, "Bánh phô mai", 58000.0, "Cheesecake", "", "available"),
            arrayOf(5, "Bánh Mousse Socola", 55000.0, "Chocolate Mousse Cake", "", "available"),
            arrayOf(5, "Bánh Mousse chanh dây", 55000.0, "Passion Fruit Mousse Cake", "", "available"),
            arrayOf(5, "Bánh Croissant bơ", 42000.0, "Butter Croissant", "", "available"),
            arrayOf(5, "Bánh Brownie Socola", 45000.0, "Chocolate Brownie", "", "available"),
            arrayOf(5, "Bánh su kem", 40000.0, "Cream Puff", "", "available")
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