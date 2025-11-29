package com.example.sistemamovimiento.data

import android.content.Context
import com.example.sistemamovimiento.models.User

class UserRepository(context: Context) {

    private val service = LocalUserService(context)
    val users = service.loadUsers()

    fun login(email: String, password: String): User? {
        return users.find { it.correo == email && it.contrasena == password }
    }

    fun getUser(email: String): User? {
        return users.find { it.correo == email }
    }

    fun updateUser(user: User) {
        service.saveUser(user)
    }
}
