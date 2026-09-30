package com.example.foodordering.model

data class Order(
    val id: Int = 0,
    val userId: Int,
    val total: Double,
    val status: String,             // PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
    val createdAt: String,          // thời gian tạo đơn
    val orderCode: String           // mã đơn dễ đọc, ví dụ: ORD-20260930-001
)