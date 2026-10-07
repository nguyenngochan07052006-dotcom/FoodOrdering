package com.example.foodordering.model

data class CartItem(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val quantity: Int
)