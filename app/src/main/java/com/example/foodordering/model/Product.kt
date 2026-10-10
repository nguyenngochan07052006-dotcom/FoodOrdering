package com.example.foodordering.model

data class Product(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val price: Double,
    val description: String,
    val image: String,
    val status: String,
    val label: String = "",
)