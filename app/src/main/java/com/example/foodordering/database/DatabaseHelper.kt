package com.example.foodordering.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodOrdering.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_PRODUCTS = "products"
        const val TABLE_CART = "cart"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_ITEMS = "order_items"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Bảng Users
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                phone TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                name TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'user'
            )
            """.trimIndent()
        )

        // Bảng Categories
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Bảng Products
        db.execSQL(
            """
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                description TEXT,
                image TEXT,
                status TEXT NOT NULL DEFAULT 'available',
                label TEXT DEFAULT '',
                FOREIGN KEY (category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
            """.trimIndent()
        )

        // Bảng Cart
        db.execSQL(
            """
            CREATE TABLE $TABLE_CART (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY (product_id) REFERENCES $TABLE_PRODUCTS(id)
            )
            """.trimIndent()
        )

        // Bảng Orders
        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                created_at TEXT NOT NULL,
                order_code TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id)
            )
            """.trimIndent()
        )

        // Bảng Order Items
        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES $TABLE_ORDERS(id),
                FOREIGN KEY (product_id) REFERENCES $TABLE_PRODUCTS(id)
            )
            """.trimIndent()
        )

        // ==================== DỮ LIỆU MẪU ====================

        // 1. Tài khoản Admin
        val admin = ContentValues().apply {
            put("phone", "0123456789")
            put("password", "admin123")
            put("name", "Admin")
            put("role", "admin")
        }
        db.insert(TABLE_USERS, null, admin)

        // 2. Tài khoản User thường
        val user = ContentValues().apply {
            put("phone", "0987654321")
            put("password", "user123")
            put("name", "Nguyễn Văn A")
            put("role", "user")
        }
        db.insert(TABLE_USERS, null, user)

        // 3. Danh mục mẫu
        val categories = listOf("Cà phê", "Trà sữa", "Nước ép", "Sinh tố", "Đồ ăn nhẹ")
        categories.forEach { name ->
            val values = ContentValues().apply { put("name", name) }
            db.insert(TABLE_CATEGORIES, null, values)
        }

        // 4. Sản phẩm mẫu
        data class ProductSeed(
            val categoryId: Int,
            val name: String,
            val price: Double,
            val description: String,
            val status: String
        )

        val products = listOf(
            ProductSeed(1, "Cà phê đen", 25000.0, "Cà phê phin truyền thống", "available"),
            ProductSeed(1, "Cà phê sữa", 30000.0, "Cà phê sữa đá đậm đà", "available"),
            ProductSeed(1, "Bạc xỉu", 32000.0, "Bạc xỉu nóng/đá", "available"),
            ProductSeed(2, "Trà sữa truyền thống", 35000.0, "Trà sữa đậm vị", "available"),
            ProductSeed(2, "Trà sữa matcha", 40000.0, "Matcha Nhật Bản", "available"),
            ProductSeed(3, "Nước ép cam", 35000.0, "Cam tươi nguyên chất", "available"),
            ProductSeed(3, "Nước ép ổi", 30000.0, "Ổi hồng Đài Loan", "available"),
            ProductSeed(4, "Sinh tố bơ", 40000.0, "Bơ sáp Đắk Lắk", "available"),
            ProductSeed(5, "Bánh mì thịt", 25000.0, "Bánh mì nóng giòn", "available"),
            ProductSeed(5, "Xôi mặn", 20000.0, "Xôi mặn đầy đủ", "available")
        )

        products.forEach { p ->
            val values = ContentValues().apply {
                put("category_id", p.categoryId)
                put("name", p.name)
                put("price", p.price)
                put("description", p.description)
                put("status", p.status)
                put("image", "")
            }
            db.insert(TABLE_PRODUCTS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}