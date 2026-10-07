package com.example.foodordering.model

data class CartDisplayItem(
    val cartId: Int,
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String
) {
    val total: Double
        get() = price * quantity
}