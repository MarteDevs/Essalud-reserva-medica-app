package com.mars.essalureservamedica.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.databinding.FragmentAppointmentsBinding
import com.mars.essalureservamedica.ui.main.adapter.AppointmentsAdapter
import com.mars.essalureservamedica.utils.SessionManager

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
        appointmentsAdapter = AppointmentsAdapter(
            onAppointmentClick = { citaWithDoctorInfo ->
                showAppointmentDetails(citaWithDoctorInfo)
            },
            onCancelClick = { citaWithDoctorInfo ->
                showCancelConfirmation(citaWithDoctorInfo)
            },
            onRescheduleClick = { citaWithDoctorInfo ->
                showRescheduleDialog(citaWithDoctorInfo)
            },
            onRateClick = {
                Toast.makeText(requireContext(), "Función 'Calificar' no implementada aún.", Toast.LENGTH_SHORT).show()
            }
        )

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

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(requireContext(), it.getOrNull(), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(),
                        it.exceptionOrNull()?.message ?: "Error en la operación",
                        Toast.LENGTH_LONG).show()
                }
                viewModel.clearOperationResult()
            }
        }
    }

    private fun showAppointmentDetails(cita: CitaWithDoctorInfo) {
        val prettyDateFormat = java.text.SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy, hh:mm a", java.util.Locale("es", "ES"))

        val message = """
            Doctor: ${cita.doctorNombre}
            Especialidad: ${cita.doctorEspecialidad}
            Fecha: ${prettyDateFormat.format(cita.fechaHora)}
            Estado: ${cita.estado}
            ${if (!cita.notas.isNullOrEmpty()) "\nNotas: ${cita.notas}" else ""}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog_Futuristic)
            .setTitle("Detalles de la Cita")
            .setMessage(message)
            .setPositiveButton("Cerrar", null)
            .show()
    }


    private fun showCancelConfirmation(cita: CitaWithDoctorInfo) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog_Futuristic)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que deseas cancelar la cita con ${cita.doctorNombre}?")
            .setPositiveButton("Sí, Cancelar") { _, _ ->
                viewModel.cancelarCita(cita.id)
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun showRescheduleDialog(cita: CitaWithDoctorInfo) {
        val dialog = RescheduleDialogFragment.newInstance(cita)
        dialog.show(childFragmentManager, "RescheduleDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
