package com.example.foodordering.model

enum class OrderStatus(val value: String) {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    PREPARING("PREPARING"),
    READY("READY"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    companion object {
        fun from(value: String): OrderStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}