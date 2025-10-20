package com.mars.essalureservamedica.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        setupAnimations()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAnimations() {
        val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right_content)
        binding.registerContainer.visibility = View.VISIBLE
        binding.registerContainer.startAnimation(slideIn)
    }
    private fun setupObservers() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is AuthResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
                null -> {
                    // Estado inicial
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

            when {
                fullName.isBlank() -> binding.tilFullName.error = "El nombre es requerido"
                email.isBlank() -> binding.tilEmail.error = "El correo es requerido"
                password.isBlank() -> binding.tilPassword.error = "La contraseña es requerida"
                password.length < 6 -> binding.tilPassword.error = "Debe tener al menos 6 caracteres"
                confirmPassword != password -> binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                else -> {
                    binding.tilFullName.error = null
                    binding.tilEmail.error = null
                    binding.tilPassword.error = null
                    binding.tilConfirmPassword.error = null
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
        authViewModel.clearAuthResult()
        _binding = null
    }
}
