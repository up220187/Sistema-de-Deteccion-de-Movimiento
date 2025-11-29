package com.example.sistemamovimiento.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sistemamovimiento.MainActivity
import com.example.sistemamovimiento.data.UserSession
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
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarSettings)
        binding.toolbarSettings.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 1. Cargar datos del usuario y mostrarlos
        loadUserData()

        // 2. Configurar botones de edición
        setupEditButtons()

        // 3. Configurar Switches de Notificaciones (Código existente)
        setupNotifications()

        // 4. Sensibilidad (Código existente)
        setupSensitivity()

        // 5. Logout (Código existente)
        binding.buttonLogout.setOnClickListener {
            UserSession.logout(requireContext())
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)

        // Intentamos obtener datos guardados localmente, si no existen, usamos los de la sesión
        val sessionName = UserSession.currentUser?.nombre ?: "Usuario"
        val sessionPhone = UserSession.currentUser?.telefono ?: "---"
        val sessionPass = UserSession.currentUser?.contrasena ?: "******"
        val sessionEmail = UserSession.currentUser?.correo ?: "correo@ejemplo.com"

        // Si se editó previamente, estará en SharedPreferences
        val savedName = prefs.getString("saved_name", sessionName)
        val savedPhone = prefs.getString("saved_phone", sessionPhone)
        val savedPass = prefs.getString("saved_pass", sessionPass)

        // Asignar a la vista
        binding.profileName.text = savedName
        binding.tvCurrentName.text = savedName
        binding.tvCurrentPhone.text = savedPhone
        binding.tvCurrentPassword.text = "*".repeat(savedPass?.length ?: 6) // Ocultar pass visualmente
        binding.tvCurrentEmail.text = sessionEmail // El correo usualmente no se edita fácil, lo dejamos fijo
    }

    private fun setupEditButtons() {
        val prefs = requireContext().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)

        // Editar Nombre
        binding.btnEditName.setOnClickListener {
            val currentVal = binding.tvCurrentName.text.toString()
            showEditDialog("Editar Nombre", currentVal) { newValue ->
                prefs.edit().putString("saved_name", newValue).apply()
                // Actualizar UI y Sesión temporal
                binding.profileName.text = newValue
                binding.tvCurrentName.text = newValue
                UserSession.currentUser?.nombre = newValue
                Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
        }

        // Editar Teléfono
        binding.btnEditPhone.setOnClickListener {
            val currentVal = binding.tvCurrentPhone.text.toString()
            showEditDialog("Editar Teléfono", currentVal, isPhone = true) { newValue ->
                prefs.edit().putString("saved_phone", newValue).apply()
                binding.tvCurrentPhone.text = newValue
                UserSession.currentUser?.telefono = newValue
                Toast.makeText(context, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
            }
        }

        // Editar Contraseña
        binding.btnEditPassword.setOnClickListener {
            // No mostramos la contraseña actual en el cuadro de texto por seguridad, solo vacío
            showEditDialog("Cambiar Contraseña", "", isPassword = true) { newValue ->
                if (newValue.isNotEmpty()) {
                    prefs.edit().putString("saved_pass", newValue).apply()
                    binding.tvCurrentPassword.text = "*".repeat(newValue.length)
                    UserSession.currentUser?.contrasena = newValue
                    Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función auxiliar para mostrar un cuadro de diálogo con un campo de texto
    private fun showEditDialog(
        title: String,
        currentValue: String,
        isPhone: Boolean = false,
        isPassword: Boolean = false,
        onSave: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        val input = EditText(requireContext())
        input.setText(currentValue)

        // Configurar tipo de entrada
        if (isPhone) {
            input.inputType = InputType.TYPE_CLASS_PHONE
        } else if (isPassword) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = "Nueva contraseña"
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }

        // Añadir un poco de margen al EditText
        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Guardar") { _, _ ->
            onSave(input.text.toString())
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun setupNotifications() {
        val prefs = requireContext().getSharedPreferences("SETTINGS", AppCompatActivity.MODE_PRIVATE)

        val realtime = binding.settingAlertRealtime
        val email = binding.settingAlertEmail
        val sms = binding.settingAlertSms

        realtime.settingTitle.text = "Alertas en tiempo real"
        email.settingTitle.text = "Alertas por correo"
        sms.settingTitle.text = "Alertas por SMS"

        realtime.settingSwitch.isChecked = prefs.getBoolean("alert_realtime", true)
        email.settingSwitch.isChecked = prefs.getBoolean("alert_email", true)
        sms.settingSwitch.isChecked = prefs.getBoolean("alert_sms", true)

        realtime.settingSwitch.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("alert_realtime", v).apply() }
        email.settingSwitch.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("alert_email", v).apply() }
        sms.settingSwitch.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("alert_sms", v).apply() }
    }

    private fun setupSensitivity() {
        binding.textSensitivityValue.setOnClickListener {
            val next = when (binding.textSensitivityValue.text.toString()) {
                "Baja" -> "Media"
                "Media" -> "Alta"
                else -> "Baja"
            }
            binding.textSensitivityValue.text = next
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
