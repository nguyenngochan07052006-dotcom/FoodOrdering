package com.example.foodordering.model

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String          // "USER" hoặc "ADMIN"
)