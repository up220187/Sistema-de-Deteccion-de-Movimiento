package com.example.sistemamovimiento.data

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object UserSession {
    private const val PREFS = "USER_SESSION"
    private const val KEY_LOGGED = "is_logged"

    fun login(context: Context) {
        context.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGGED, true)
            .apply()
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun isLogged(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(KEY_LOGGED, false)
    }
}

