package com.mars.essalureservamedica.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.databinding.FragmentHomeBinding
import com.mars.essalureservamedica.ui.auth.AuthActivity
import com.mars.essalureservamedica.utils.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupUI()
        setupObservers()
        setupClickListeners()
        
        // Cargar datos
        homeViewModel.loadStats()
    }

    private fun setupUI() {
        // Mostrar nombre del usuario
        val userName = sessionManager.getUserName()
        binding.tvUserName.text = "Hola, $userName"
    }

    private fun setupObservers() {
        homeViewModel.totalDoctors.observe(viewLifecycleOwner, Observer { count ->
            binding.tvTotalDoctors.text = count.toString()
        })

        homeViewModel.totalAppointments.observe(viewLifecycleOwner, Observer { count ->
            binding.tvTotalAppointments.text = count.toString()
        })
    }

    private fun setupClickListeners() {
        binding.btnViewDoctors.setOnClickListener {
            findNavController().navigate(R.id.nav_doctors)
        }

        binding.btnViewAppointments.setOnClickListener {
            findNavController().navigate(R.id.nav_appointments)
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}