package com.example.foodordering.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FoodOrderingSession", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
    }

    fun createLoginSession(userId: Int, name: String, phone: String, role: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getName(): String {
        return prefs.getString(KEY_NAME, "") ?: ""
    }

    fun getPhone(): String {
        return prefs.getString(KEY_PHONE, "") ?: ""
    }

    fun getRole(): String {
        return prefs.getString(KEY_ROLE, "user") ?: "user"
    }

    fun isAdmin(): Boolean {
        return getRole().equals("admin", ignoreCase = true)
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}