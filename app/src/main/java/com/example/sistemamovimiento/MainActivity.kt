package com.example.sistemamovimiento
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemamovimiento.utils.NetworkStatusHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // este solo contiene fragment container

        NetworkStatusHelper.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Es buena práctica liberarlo cuando la actividad principal se destruye.
        NetworkStatusHelper.unregister()
    }
}

