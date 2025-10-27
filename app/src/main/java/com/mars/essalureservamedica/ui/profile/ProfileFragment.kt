package com.mars.essalureservamedica.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mars.essalureservamedica.databinding.FragmentProfileBinding
import com.mars.essalureservamedica.ui.auth.AuthActivity
import com.mars.essalureservamedica.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        profileViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                binding.etNombreCompleto.setText(it.nombreCompleto)
                binding.etEmail.setText(it.email)
            }
        })

        profileViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnUpdateProfile.isEnabled = !isLoading
        })

        profileViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(requireContext(), it.getOrNull(), Toast.LENGTH_SHORT).show()
                    clearPasswordFields()
                } else {
                    Toast.makeText(requireContext(), it.exceptionOrNull()?.message, Toast.LENGTH_LONG).show()
                }
                profileViewModel.clearUpdateResult()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun updateProfile() {
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validar nueva contraseña si se proporcionó
        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return
            }
        }

        profileViewModel.updateProfile(
            nombreCompleto = nombreCompleto,
            email = email,
            currentPassword = currentPassword,
            newPassword = if (newPassword.isNotEmpty()) newPassword else null
        )
    }

    private fun clearPasswordFields() {
        binding.etCurrentPassword.setText("")
        binding.etNewPassword.setText("")
        binding.etConfirmPassword.setText("")
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        sessionManager.clearSession()
        
        // Navegar a AuthActivity
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}