package com.example.sistemamovimiento.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sistemamovimiento.MainActivity
import com.example.sistemamovimiento.data.UserRepository
import com.example.sistemamovimiento.data.UserSession
import com.example.sistemamovimiento.databinding.FragmentSettingsBinding
import com.example.sistemamovimiento.utils.SessionManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Necesitamos estos dos para manejar la sesión y el guardado JSON
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos
        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository(requireContext())

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarSettings)
        binding.toolbarSettings.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        updateUI()
        setupEditButtons()
        setupNotifications()
        setupSensitivity()

        // Logout
        binding.buttonLogout.setOnClickListener {
            UserSession.logout(requireContext()) // Tu objeto singleton
            sessionManager.clearSession()        // Tu manager de preferencias

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun updateUI() {
        val currentUser = sessionManager.getSession()

        if (currentUser != null) {
            binding.profileName.text = currentUser.nombre
            binding.tvCurrentName.text = currentUser.nombre
            binding.tvCurrentPhone.text = currentUser.telefono
            binding.tvCurrentEmail.text = currentUser.correo // El correo es el ID, mejor no editarlo
            binding.tvCurrentPassword.text = "*".repeat(currentUser.contrasena.length)
        }
    }

    private fun setupEditButtons() {
        // Editar Nombre
        binding.btnEditName.setOnClickListener {
            val user = sessionManager.getSession() ?: return@setOnClickListener
            showEditDialog("Editar Nombre", user.nombre) { newValue ->

                // 1. Modificar objeto
                user.nombre = newValue

                // 2. Guardar en JSON (Permanente)
                userRepository.updateUser(user)

                // 3. Guardar en Sesión (Actual)
                sessionManager.saveSession(user)

                // 4. Actualizar Vista
                updateUI()
                Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
        }

        // Editar Teléfono
        binding.btnEditPhone.setOnClickListener {
            val user = sessionManager.getSession() ?: return@setOnClickListener
            showEditDialog("Editar Teléfono", user.telefono, isPhone = true) { newValue ->
                user.telefono = newValue
                userRepository.updateUser(user)
                sessionManager.saveSession(user)
                updateUI()
                Toast.makeText(context, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
            }
        }

        // Editar Contraseña
        binding.btnEditPassword.setOnClickListener {
            val user = sessionManager.getSession() ?: return@setOnClickListener
            showEditDialog("Cambiar Contraseña", "", isPassword = true) { newValue ->
                if (newValue.isNotEmpty()) {
                    user.contrasena = newValue
                    userRepository.updateUser(user)
                    sessionManager.saveSession(user)
                    updateUI()
                    Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Tus funciones auxiliares (sin cambios mayores) ---
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
        input.setText(if (isPassword) "" else currentValue)

        if (isPhone) {
            input.inputType = InputType.TYPE_CLASS_PHONE
        } else if (isPassword) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = "Nueva contraseña"
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Guardar") { _, _ -> onSave(input.text.toString()) }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun setupNotifications() {
        // (Tu código original de notificaciones aquí)
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
        // (Tu código original de sensibilidad aquí)
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
