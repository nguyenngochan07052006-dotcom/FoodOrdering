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


        // ===== 6 Danh mục (thêm Topping) =====
        val categories = listOf(
            "Cà phê máy",      // 1
            "Trà sữa",         // 2
            "Trà trái cây",    // 3
            "Đá xay",          // 4
            "Bánh ngọt",       // 5
            "Topping"          // 6
        )
        categories.forEach { name ->
            val values = ContentValues().apply { put("name", name) }
            db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
        }


        // ===== Sản phẩm (40 món cũ + 7 topping) =====
        // Cột: category_id, name, price, description, image, status, label
        data class ProductSeed(
            val categoryId: Int,
            val name: String,
            val price: Double,
            val description: String,
            val image: String = "",
            val status: String = "available",
            val label: String = ""
        )


        val products = listOf(
            // 1. Cà phê máy
            ProductSeed(1, "Espresso", 35000.0, "Espresso đậm đà", label = "Bán chạy"),
            ProductSeed(1, "Espresso Doppio", 45000.0, "Double Espresso"),
            ProductSeed(1, "Americano", 40000.0, "Americano"),
            ProductSeed(1, "Cappuccino", 50000.0, "Cappuccino", label = "Hot"),
            ProductSeed(1, "Latte", 50000.0, "Caffè Latte"),
            ProductSeed(1, "Mocha", 55000.0, "Caffè Mocha"),
            ProductSeed(1, "Caramel Macchiato", 55000.0, "Caramel Macchiato", label = "Mới"),
            ProductSeed(1, "Cà phê sữa ", 35000.0, "Iced Espresso with Condensed Milk", label = "Bán chạy"),


            // 2. Trà sữa
            ProductSeed(2, "Trà sữa truyền thống", 45000.0, "Classic Milk Tea", label = "Bán chạy"),
            ProductSeed(2, "Trà sữa Ô Long", 50000.0, "Oolong Milk Tea"),
            ProductSeed(2, "Trà sữa Chôm Chôm", 48000.0, "Rambutan Milk Tea"),
            ProductSeed(2, "Trà sữa Lài", 50000.0, "Jasmine Milk Tea"),
            ProductSeed(2, "Trà sữa Matcha", 55000.0, "Matcha Milk Tea", label = "Mới"),
            ProductSeed(2, "Trà sữa Socola", 52000.0, "Chocolate Milk Tea"),
            ProductSeed(2, "Trà sữa Đào", 52000.0, "Peach Milk Tea"),


            // 3. Trà trái cây
            ProductSeed(3, "Trà đào cam sả", 50000.0, "Peach Orange Lemongrass Tea", label = "Bán chạy"),
            ProductSeed(3, "Trà vải", 48000.0, "Lychee Tea"),
            ProductSeed(3, "Trà chanh dây", 48000.0, "Passion Fruit Tea"),
            ProductSeed(3, "Trà dâu", 50000.0, "Strawberry Tea"),
            ProductSeed(3, "Trà xoài", 50000.0, "Mango Tea"),
            ProductSeed(3, "Trà táo xanh", 48000.0, "Green Apple Tea"),
            ProductSeed(3, "Trà nhiệt đới", 55000.0, "Tropical Fruit Tea", label = "Hot"),
            ProductSeed(3, "Trà tắc mật ong", 45000.0, "Honey Kumquat Tea"),


            // 4. Đá xay
            ProductSeed(4, "Cà phê đá xay", 48000.0, "Coffee Frappé"),
            ProductSeed(4, "Mocha đá xay", 52000.0, "Mocha Frappé"),
            ProductSeed(4, "Caramel đá xay", 52000.0, "Caramel Frappé", label = "Hot"),
            ProductSeed(4, "Matcha đá xay", 50000.0, "Matcha Frappé"),
            ProductSeed(4, "Socola đá xay", 50000.0, "Chocolate Frappé"),
            ProductSeed(4, "Cookies & Cream đá xay", 55000.0, "Cookies & Cream Frappé", label = "Mới"),
            ProductSeed(4, "Dâu đá xay", 50000.0, "Strawberry Frappé"),
            ProductSeed(4, "Việt quất đá xay", 52000.0, "Blueberry Frappé"),


            // 5. Bánh ngọt
            ProductSeed(5, "Bánh Tiramisu", 35000.0, "Tiramisu", label = "Bán chạy"),
            ProductSeed(5, "Bánh Red Velvet", 35000.0, "Red Velvet Cake"),
            ProductSeed(5, "Bánh phô mai", 38000.0, "Cheesecake"),
            ProductSeed(5, "Bánh Mousse Socola", 35000.0, "Chocolate Mousse Cake"),
            ProductSeed(5, "Bánh Mousse chanh dây", 35000.0, "Passion Fruit Mousse Cake"),
            ProductSeed(5, "Bánh Croissant bơ", 32000.0, "Butter Croissant"),
            ProductSeed(5, "Bánh Brownie Socola", 35000.0, "Chocolate Brownie"),
            ProductSeed(5, "Bánh su kem", 30000.0, "Cream Puff"),


            // 6. Topping (7 món)
            ProductSeed(6, "Trân châu đen", 6000.0, "Black Tapioca Pearls"),
            ProductSeed(6, "Trân châu trắng", 6000.0, "White Tapioca Pearls"),
            ProductSeed(6, "Thạch cà phê", 6000.0, "Coffee Jelly"),
            ProductSeed(6, "Thạch trái cây", 6000.0, "Fruit Jelly"),
            ProductSeed(6, "Pudding trứng", 8000.0, "Egg Pudding", label = "Hot"),
            ProductSeed(6, "Kem cheese", 8000.0, "Cheese Foam", label = "Mới"),
            ProductSeed(6, "Nha đam", 6000.0, "Aloe Vera")
        )
        products.forEach { p ->
            val values = ContentValues().apply {
                put("category_id", p.categoryId)
                put("name", p.name)
                put("price", p.price)
                put("description", p.description)
                put("image", p.image)
                put("status", p.status)
                put("label", p.label)          // ← cần có cột label trong bảng products
            }
            db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values)
        }


        db.close()
    }
}

