package com.example.sistemamovimiento.data

import android.content.Context
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class LocalUserService(private val context: Context) {

    private val fileName = "users.json"

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