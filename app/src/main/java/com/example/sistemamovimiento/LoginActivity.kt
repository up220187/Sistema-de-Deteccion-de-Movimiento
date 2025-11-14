package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<TextInputEditText>(R.id.edit_text_email)
        val pass = findViewById<TextInputEditText>(R.id.edit_text_password)
        val btnLogin = findViewById<Button>(R.id.button_sign_in)

        findViewById<Button>(R.id.button_sign_in).setOnClickListener {
            if (email.text.isNullOrBlank() || pass.text.isNullOrBlank()) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            } else {
                // simular login exitoso
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        findViewById<android.widget.TextView>(R.id.text_forgot_password).setOnClickListener {
            Toast.makeText(this, "Función no implementada (simulada)", Toast.LENGTH_SHORT).show()
        }
    }
}
