package com.example.sistemamovimiento.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sistemamovimiento.MainActivity
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.UserRepository
import com.example.sistemamovimiento.data.UserSession
import com.example.sistemamovimiento.databinding.FragmentSettingsBinding
import com.example.sistemamovimiento.utils.SessionManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    private var isPasswordVisible = false

    // Permiso para SMS
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            binding.settingAlertSms.settingSwitch.isChecked = isGranted
            if (isGranted) {
                saveSmsPreference(true)
                Toast.makeText(context, "Permiso SMS concedido", Toast.LENGTH_SHORT).show()
            } else {
                saveSmsPreference(false)
                Toast.makeText(context, "Permiso denegado. No se enviarán SMS", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        userRepository = UserRepository(requireContext())

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarSettings)
        binding.toolbarSettings.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        updateUI()
        setupEditButtons()

        // SOLO Configuración de Notificaciones (Ya sin sensibilidad ni email)
        setupNotifications()

        binding.buttonLogout.setOnClickListener {
            UserSession.logout(requireContext())
            sessionManager.clearSession()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnToggleVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            updateUI()
        }
    }

    private fun setupNotifications() {
        val prefs = requireContext().getSharedPreferences("SETTINGS", AppCompatActivity.MODE_PRIVATE)

        // 1. Alertas Realtime (Notificaciones Push locales)
        val realtime = binding.settingAlertRealtime
        realtime.settingTitle.text = "Alertas en tiempo real"
        realtime.settingSwitch.isChecked = prefs.getBoolean("alert_realtime", true)
        realtime.settingSwitch.setOnCheckedChangeListener { _, v ->
            prefs.edit().putBoolean("alert_realtime", v).apply()
        }

        // 2. Alertas SMS
        val sms = binding.settingAlertSms
        sms.settingTitle.text = "Alertas por SMS"
        sms.settingSwitch.isChecked = prefs.getBoolean("alert_sms", false)

        // Al hacer click en el switch de SMS
        sms.settingSwitch.setOnClickListener {
            val isChecked = sms.settingSwitch.isChecked
            if (isChecked) {
                checkSmsPermission() // Verificar permiso antes de activar
            } else {
                saveSmsPreference(false)
            }
        }
    }

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED) {
            saveSmsPreference(true)
        } else {
            // Pedir permiso
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    private fun saveSmsPreference(enabled: Boolean) {
        val prefs = requireContext().getSharedPreferences("SETTINGS", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().putBoolean("alert_sms", enabled).apply()
    }

    private fun updateUI() {
        val currentUser = sessionManager.getSession()
        if (currentUser != null) {
            binding.profileName.text = currentUser.nombre
            binding.tvCurrentName.text = currentUser.nombre
            binding.tvCurrentPhone.text = currentUser.telefono
            binding.tvCurrentEmail.text = currentUser.correo

            if (isPasswordVisible) {
                binding.tvCurrentPassword.text = currentUser.contrasena
                binding.btnToggleVisibility.setColorFilter(resources.getColor(R.color.accent_blue, null))
            } else {
                binding.tvCurrentPassword.text = "*".repeat(currentUser.contrasena.length)
                binding.btnToggleVisibility.setColorFilter(resources.getColor(R.color.grey_text, null))
            }
        }
    }

    // ... (setupEditButtons y showEditDialog quedan IGUAL que antes) ...
    private fun setupEditButtons() {
        // Copia aquí tus funciones setupEditButtons y showEditDialog del código anterior
        // No han cambiado, solo asegúrate de que estén aquí.
        binding.btnEditName.setOnClickListener {
            val user = sessionManager.getSession() ?: return@setOnClickListener
            showEditDialog("Editar Nombre", user.nombre) { newValue ->
                user.nombre = newValue
                userRepository.updateUser(user)
                sessionManager.saveSession(user)
                updateUI()
                Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun showEditDialog(title: String, currentValue: String, isPhone: Boolean = false, isPassword: Boolean = false, onSave: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        val input = EditText(requireContext())
        input.setText(if (isPassword) "" else currentValue)
        if (isPhone) input.inputType = InputType.TYPE_CLASS_PHONE
        else if (isPassword) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = "Nueva contraseña"
        } else input.inputType = InputType.TYPE_CLASS_TEXT
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)
        builder.setPositiveButton("Guardar") { _, _ -> onSave(input.text.toString()) }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
