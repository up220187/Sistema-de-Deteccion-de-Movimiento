package com.example.sistemamovimiento.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Importa Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sistemamovimiento.MainActivity
import com.example.sistemamovimiento.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Toolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarSettings)
        binding.toolbarSettings.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val prefs = requireContext().getSharedPreferences("SETTINGS", AppCompatActivity.MODE_PRIVATE)

        // --- MANEJO DE OPCIONES DE CUENTA ---
        binding.settingEditUsername.setOnClickListener {
            // TODO: Implementar la navegación a la pantalla de edición de nombre de usuario o mostrar un Dialog
            Toast.makeText(context, "Abrir edición de nombre de usuario", Toast.LENGTH_SHORT).show()
        }

        binding.settingEditNumber.setOnClickListener {
            // TODO: Implementar la navegación a la pantalla de edición de número o mostrar un Dialog
            Toast.makeText(context, "Abrir edición de número", Toast.LENGTH_SHORT).show()
        }

        binding.settingChangePassword.setOnClickListener {
            // TODO: Implementar la navegación a la pantalla de cambio de contraseña o mostrar un Dialog
            Toast.makeText(context, "Abrir cambio de contraseña", Toast.LENGTH_SHORT).show()
        }
        // --- FIN MANEJO DE OPCIONES DE CUENTA ---

        // --- INCLUDES USANDO VIEWBINDING ---
        val realtime = binding.settingAlertRealtime
        val email = binding.settingAlertEmail
        val sms = binding.settingAlertSms

        // Títulos
        realtime.settingTitle.text = "Alertas en tiempo real"
        email.settingTitle.text = "Alertas por correo"
        sms.settingTitle.text = "Alertas por SMS"

        // Switches
        val swRealtime = realtime.settingSwitch
        val swEmail = email.settingSwitch
        val swSms = sms.settingSwitch

        // Valores guardados
        swRealtime.isChecked = prefs.getBoolean("alert_realtime", true)
        swEmail.isChecked = prefs.getBoolean("alert_email", true)
        swSms.isChecked = prefs.getBoolean("alert_sms", true)

        // Guardar cambios
        swRealtime.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_realtime", v).apply()
        }
        swEmail.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_email", v).apply()
        }
        swSms.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_sms", v).apply()
        }

        // Cambiar sensibilidad
        binding.textSensitivityValue.setOnClickListener {
            val next = when (binding.textSensitivityValue.text.toString()) {
                "Baja" -> "Media"
                "Media" -> "Alta"
                else -> "Baja"
            }
            binding.textSensitivityValue.text = next
        }

        // Logout
        binding.buttonLogout.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}