package com.example.foodordering.model

data class InventoryItem(
    val id: Int,
    val code: String,
    val name: String,
    val groupName: String,
    val unit: String,
    var quantity: Double,
    val minQuantity: Double,
    val status: String
)