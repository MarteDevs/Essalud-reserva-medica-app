package com.mars.essalureservamedica.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mars.essalureservamedica.databinding.FragmentAppointmentsBinding
import com.mars.essalureservamedica.ui.main.adapter.AppointmentsAdapter

class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentsViewModel by viewModels()
    private lateinit var appointmentsAdapter: AppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadAppointments()
    }

    private fun setupRecyclerView() {
        appointmentsAdapter = AppointmentsAdapter { citaWithDoctorInfo ->
            // Manejar click en cita - navegar a detalles o mostrar opciones
            // Por ahora solo mostrar un Toast
            Toast.makeText(requireContext(), "Cita con Dr. ${citaWithDoctorInfo.doctorNombre}", Toast.LENGTH_SHORT).show()
        }

        binding.rvAppointments.apply {
            adapter = appointmentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.appointments.observe(viewLifecycleOwner) { appointments ->
            appointmentsAdapter.submitList(appointments)
            binding.tvEmptyState.visibility = if (appointments.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}