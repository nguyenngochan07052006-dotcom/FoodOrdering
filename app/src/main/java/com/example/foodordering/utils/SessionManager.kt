package com.example.foodordering.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FoodOrderingSession", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_LOGIN = "is_login"
    }

    fun saveLogin(userId: Int, name: String, phone: String, role: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_NAME, name)
            putString(KEY_PHONE, phone)
            putString(KEY_ROLE, role)
            putBoolean(KEY_IS_LOGIN, true)
            apply()
        }
    }

    fun logout() {
        // Chỉ xóa session đăng nhập, giữ lại remember + recent phones
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_NAME)
            remove(KEY_PHONE)
            remove(KEY_ROLE)
            putBoolean(KEY_IS_LOGIN, false)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGIN, false)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""

    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun isAdmin(): Boolean = getRole() == "ADMIN"

    // ===== Ghi nhớ tài khoản =====
    fun saveRememberAccount(phone: String, password: String) {
        prefs.edit().apply {
            putString("remember_phone", phone)
            putString("remember_password", password)
            putBoolean("is_remember", true)
            apply()
        }
    }

    fun clearRememberAccount() {
        prefs.edit().apply {
            remove("remember_phone")
            remove("remember_password")
            putBoolean("is_remember", false)
            apply()
        }
    }

    fun isRemember(): Boolean = prefs.getBoolean("is_remember", false)

    fun getRememberPhone(): String = prefs.getString("remember_phone", "") ?: ""

    fun getRememberPassword(): String = prefs.getString("remember_password", "") ?: ""

    // ===== Số điện thoại gần đây (gợi ý dropdown) =====
    fun addRecentPhone(phone: String) {
        val current = getRecentPhones().toMutableList()
        current.remove(phone)
        current.add(0, phone)
        if (current.size > 5) {
            current.removeAt(current.lastIndex)
        }
        prefs.edit()
            .putString("recent_phones", current.joinToString(","))
            .apply()
    }

    fun getRecentPhones(): List<String> {
        val raw = prefs.getString("recent_phones", "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split(",")
    }
}