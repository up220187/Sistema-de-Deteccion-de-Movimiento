package com.example.sistemamovimiento.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sistemamovimiento.databinding.FragmentLoginBinding
import com.example.sistemamovimiento.viewmodels.LoginViewModel
import com.example.sistemamovimiento.viewmodels.LoginViewModelFactory

class FragmentLogin : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1️⃣ Si ya hay sesión, ir directo al Home
        if (viewModel.isLogged()) {
            findNavController().navigate(
                FragmentLoginDirections.actionLoginFragmentToHomeFragment()
            )
            return
        }

        // 2️⃣ Botón iniciar sesión
        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val pass = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = viewModel.login(email, pass)

            if (success) {
                Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    FragmentLoginDirections.actionLoginFragmentToHomeFragment()
                )
            } else {
                Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
