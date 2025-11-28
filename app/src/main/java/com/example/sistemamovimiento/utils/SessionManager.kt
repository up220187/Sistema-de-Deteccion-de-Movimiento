package com.example.sistemamovimiento.utils

import android.content.Context
import com.example.sistemamovimiento.models.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(user: User) {
        val json = gson.toJson(user)
        prefs.edit().putString("logged_user", json).apply()
    }

    fun getSession(): User? {
        val json = prefs.getString("logged_user", null)
        return if (json != null) gson.fromJson(json, User::class.java) else null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLogged(): Boolean = getSession() != null
}
