package com.mars.essalureservamedica.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.data.entity.EstadoCita
import com.mars.essalureservamedica.databinding.FragmentAppointmentsBinding
import com.mars.essalureservamedica.ui.main.adapter.AppointmentsAdapter
import com.mars.essalureservamedica.ui.rating.RatingDialogFragment
import com.mars.essalureservamedica.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

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
                // Mostrar detalles de la cita
                showAppointmentDetails(citaWithDoctorInfo)
            },
            onCancelClick = { citaWithDoctorInfo ->
                // Mostrar confirmación para cancelar
                showCancelConfirmation(citaWithDoctorInfo)
            },
            onRescheduleClick = { citaWithDoctorInfo ->
                // Mostrar diálogo para reprogramar
                showRescheduleDialog(citaWithDoctorInfo)
            },
            onRateClick = { citaWithDoctorInfo ->
                // Mostrar diálogo para calificar
                showRatingDialog(citaWithDoctorInfo)
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

    private fun showAppointmentDetails(cita: CitaWithDoctorFirestore) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_appointment_details, null)
        
        val estado = EstadoCita.fromString(cita.estado)
        
        // Configurar los datos en las vistas
        dialogView.findViewById<TextView>(R.id.tvDoctorName).text = "Dr. ${cita.doctorNombre}"
        dialogView.findViewById<TextView>(R.id.tvSpecialty).text = cita.doctorEspecialidad
        dialogView.findViewById<TextView>(R.id.tvDateTime).text = 
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(cita.fechaHora)
        dialogView.findViewById<TextView>(R.id.tvStatus).text = estado.displayName
        
        // Mostrar motivo solo si existe
        val motivoLayout = dialogView.findViewById<LinearLayout>(R.id.llMotivo)
        val motivoText = dialogView.findViewById<TextView>(R.id.tvMotivo)
        if (cita.motivo.isNotEmpty()) {
            motivoLayout.visibility = View.VISIBLE
            motivoText.text = cita.motivo
        } else {
            motivoLayout.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        // Configurar el botón cerrar
        dialogView.findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        // Hacer el fondo del diálogo transparente para mostrar nuestro diseño personalizado
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialog.show()
    }

    private fun showCancelConfirmation(cita: CitaWithDoctorFirestore) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que deseas cancelar la cita con Dr. ${cita.doctorNombre}?")
            .setPositiveButton("Sí, Cancelar") { _, _ ->
                viewModel.cancelarCita(cita.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showRescheduleDialog(cita: CitaWithDoctorFirestore) {
        val dialog = RescheduleDialogFragment.newInstance(cita)
        dialog.show(childFragmentManager, "RescheduleDialog")
    }

    private fun showRatingDialog(cita: CitaWithDoctorFirestore) {
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()
        
        if (userId != -1) {
            val dialog = RatingDialogFragment.newInstance(cita, userId)
            dialog.show(childFragmentManager, "RatingDialog")
        } else {
            Toast.makeText(requireContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}