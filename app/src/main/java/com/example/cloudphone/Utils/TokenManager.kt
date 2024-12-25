package com.example.cloudphone.Utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {

    private const val PREFS_NAME = "CloudPhonePrefs"
    private const val TOKEN_KEY = "TOKEN"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Lưu token vào SharedPreferences
    fun saveToken(context: Context, token: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    // Lấy token từ SharedPreferences
    fun getToken(context: Context): String? {
        val token = getSharedPreferences(context).getString(TOKEN_KEY, null)
        Log.d("token",token ?: "null")
        return token
    }

    // Xóa token
    fun clearToken(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
}
