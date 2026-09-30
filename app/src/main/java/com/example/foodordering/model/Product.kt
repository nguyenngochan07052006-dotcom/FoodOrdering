package com.example.foodordering.model

data class Product(
    val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val price: Double,
    val description: String,
    val image: String,              // tên file ảnh hoặc đường dẫn
    val status: String              // "available" hoặc "unavailable"
)