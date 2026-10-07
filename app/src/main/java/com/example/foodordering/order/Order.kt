package com.example.foodordering.model

data class Order(
    val id: Int,
    val userId: Int,
    val total: Double,
    val status: String,
    val createdAt: String,
    val orderCode: String
)