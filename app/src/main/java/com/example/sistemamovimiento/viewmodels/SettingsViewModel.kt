package com.example.sistemamovimiento.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.sistemamovimiento.data.UserSession

class SettingsViewModel(private val context: Context) : ViewModel() {

    fun logout() {
        UserSession.logout(context)
    }
}
