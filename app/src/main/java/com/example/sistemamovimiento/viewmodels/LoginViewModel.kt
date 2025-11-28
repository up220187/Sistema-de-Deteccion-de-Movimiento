package com.example.sistemamovimiento.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.sistemamovimiento.data.UserRepository
import com.example.sistemamovimiento.utils.SessionManager

class LoginViewModel(context: Context) : ViewModel() {

    private val repo = UserRepository(context)
    private val session = SessionManager(context)

    fun login(email: String, password: String): Boolean {
        val user = repo.login(email, password)
        return if (user != null) {
            session.saveSession(user)
            true
        } else {
            false
        }
    }

    fun isLogged(): Boolean = session.isLogged()
}
