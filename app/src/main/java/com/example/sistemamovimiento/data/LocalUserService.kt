package com.example.sistemamovimiento.data

import android.content.Context
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class LocalUserService(private val context: Context) {

    private val fileName = "users.json"
    private val gson = Gson()

    // Obtiene el archivo de la memoria interna
    private fun getFile(): File {
        return File(context.filesDir, fileName)
    }


    // Inicializa el archivo si no existe en memoria interna, lo copia desde RAW
    private fun initializeFile() {
        val file = getFile()
        if (!file.exists()) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.users)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                file.writeText(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadUsers(): MutableList<User> {
        initializeFile() // Aseguramos que el archivo exista
        val file = getFile()

        if (!file.exists()) return mutableListOf()

        val json = file.readText()
        val type = object : TypeToken<Map<String, MutableList<User>>>() {}.type
        val data: Map<String, MutableList<User>> = gson.fromJson(json, type)

        return data["users"] ?: mutableListOf()
    }


    fun saveUser(updatedUser: User) {
        val users = loadUsers()

        // Buscamos el usuario por correo y lo reemplazamos
        val index = users.indexOfFirst { it.correo == updatedUser.correo }
        if (index != -1) {
            users[index] = updatedUser
            saveListToJSON(users)
        }
    }

    private fun saveListToJSON(users: List<User>) {
        val map = mapOf("users" to users)
        val jsonString = gson.toJson(map)
        getFile().writeText(jsonString)
    }
}