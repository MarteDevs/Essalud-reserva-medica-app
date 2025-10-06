package com.mars.essalureservamedica.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        })

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    // Navegar al LoginFragment
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is AuthResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                null -> {
                    // No hacer nada
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Validaciones básicas
            when {
                fullName.isEmpty() -> {
                    binding.tilFullName.error = "El nombre completo es requerido"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.tilEmail.error = "El correo electrónico es requerido"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.tilPassword.error = "La contraseña es requerida"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                    return@setOnClickListener
                }
                else -> {
                    // Limpiar errores
                    binding.tilFullName.error = null
                    binding.tilEmail.error = null
                    binding.tilPassword.error = null
                    binding.tilConfirmPassword.error = null
                    
                    // Proceder con el registro
                    authViewModel.register(fullName, email, password)
                }
            }
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}