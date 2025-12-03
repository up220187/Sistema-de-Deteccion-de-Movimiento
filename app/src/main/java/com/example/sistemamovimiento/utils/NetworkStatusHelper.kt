package com.example.sistemamovimiento.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Este objeto será un Singleton para que toda la app comparta el mismo estado de red.
object NetworkStatusHelper {

    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable: LiveData<Boolean> get() = _isNetworkAvailable

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Cuando una red está disponible, lo notificamos como 'true'.
            _isNetworkAvailable.postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Cuando se pierde la conexión, lo notificamos como 'false'.
            _isNetworkAvailable.postValue(false)
        }
    }

    // Esta función se debe llamar una sola vez al inicio de la app.
    fun register(context: Context) {
        if (this::connectivityManager.isInitialized) return // Ya está registrado

        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Creamos una solicitud para escuchar cualquier tipo de red (WIFI, Celular)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Verificamos el estado inicial al registrar
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val initialStatus = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isNetworkAvailable.postValue(initialStatus)
    }

    fun unregister() {
        if (this::connectivityManager.isInitialized) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
