package com.example.foodordering.model

data class OrderItem(
    val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Double               // giá tại thời điểm đặt hàng
)