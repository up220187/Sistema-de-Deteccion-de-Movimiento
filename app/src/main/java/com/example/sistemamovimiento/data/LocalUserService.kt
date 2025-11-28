package com.example.sistemamovimiento.data

import android.content.Context
import com.example.sistemamovimiento.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalUserService(private val context: Context) {

    fun loadUsers(): List<User> {
        val inputStream = context.resources.openRawResource(
            context.resources.getIdentifier("users", "raw", context.packageName)
        )

        val json = inputStream.bufferedReader().use { it.readText() }

        val type = object : TypeToken<Map<String, List<User>>>() {}.type
        val data: Map<String, List<User>> = Gson().fromJson(json, type)

        return data["users"] ?: emptyList()
    }
}