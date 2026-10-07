package com.example.foodordering.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodOrdering.db"
        private const val DATABASE_VERSION = 4          // tăng version vì có thay đổi schema

        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_PRODUCTS = "products"
        const val TABLE_CART = "cart_items"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_ITEMS = "order_items"
    }

    override fun onCreate(db: SQLiteDatabase) {

        // 1. Users (đăng nhập bằng số điện thoại)
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 2. Categories
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 3. Products
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
                FOREIGN KEY (category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
            """.trimIndent()
        )

        // 4. Cart
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

        // 5. Orders
        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                order_code TEXT NOT NULL UNIQUE,
                total REAL NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id)
            )
            """.trimIndent()
        )

        // 6. Order Items (chi tiết đơn hàng)
        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES $TABLE_ORDERS(id),
                FOREIGN KEY (product_id) REFERENCES $TABLE_PRODUCTS(id)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xóa toàn bộ bảng cũ rồi tạo lại
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}