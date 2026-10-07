package com.example.foodordering.model

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val password: String,
    val role: String
)