package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_settings)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE)

        // Includes
        val includeRealtime = findViewById<View>(R.id.setting_alert_realtime)
        val includeEmail = findViewById<View>(R.id.setting_alert_email)
        val includeSms = findViewById<View>(R.id.setting_alert_sms)

        val swRealtime = includeRealtime.findViewById<SwitchMaterial>(R.id.setting_switch)
        val swEmail = includeEmail.findViewById<SwitchMaterial>(R.id.setting_switch)
        val swSms = includeSms.findViewById<SwitchMaterial>(R.id.setting_switch)

        // cargar estado
        swRealtime.isChecked = prefs.getBoolean("alert_realtime", true)
        swEmail.isChecked = prefs.getBoolean("alert_email", true)
        swSms.isChecked = prefs.getBoolean("alert_sms", true)

        // listeners que guardan
        swRealtime.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_realtime", v).apply()
        }
        swEmail.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_email", v).apply()
        }
        swSms.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_sms", v).apply()
        }

        // Sensibilidad fake
        val sensitivityText = findViewById<TextView>(R.id.text_sensitivity_value)
        sensitivityText.setOnClickListener {
            val next = when (sensitivityText.text.toString()) {
                "Baja" -> "Media"
                "Media" -> "Alta"
                else -> "Baja"
            }
            sensitivityText.text = next
        }

        // Logout
        findViewById<Button>(R.id.button_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
