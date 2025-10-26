package com.mars.essalureservamedica.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.databinding.DialogRescheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class RescheduleDialogFragment : DialogFragment() {

    private var _binding: DialogRescheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentsViewModel by viewModels({ requireParentFragment() })

    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    private var citaToReschedule: CitaWithDoctorFirestore? = null

    private val displayDateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val currentAppointmentFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("es", "ES"))

    companion object {
        private const val ARG_CITA = "cita"

        fun newInstance(cita: CitaWithDoctorFirestore): RescheduleDialogFragment {
            val fragment = RescheduleDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_CITA, cita)
            fragment.arguments = args
            return fragment
        }
    }

    // ========== INICIO DE LA CORRECCIÓN DEL TOOLTIP ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Forzamos a este diálogo a usar el tema principal de nuestra app.
        // Esto le da acceso a nuestro estilo de tooltip personalizado.
        setStyle(STYLE_NORMAL, R.style.Theme_EssaluReservaMedica)
    }
    // ========== FIN DE LA CORRECCIÓN DEL TOOLTIP ==========

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRescheduleBinding.inflate(layoutInflater)

        citaToReschedule = arguments?.getSerializable(ARG_CITA) as? CitaWithDoctorFirestore

        setupViews()
        setupClickListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Confirmar") { _, _ ->
                reprogramarCita()
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    private fun setupViews() {
        citaToReschedule?.let { cita ->
            binding.tvCurrentAppointment.text = currentAppointmentFormat.format(cita.fechaHora)
        }
    }

    private fun setupClickListeners() {
        binding.etSelectDate.setOnClickListener {
            showDatePicker()
        }
        binding.etSelectTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                binding.etSelectDate.setText(displayDateFormat.format(selectedDate!!.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = calendar.timeInMillis
        }.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                binding.etSelectTime.setText(displayTimeFormat.format(selectedTime!!.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun reprogramarCita() {
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Por favor selecciona fecha y hora", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaFechaHora = Calendar.getInstance().apply {
            time = selectedDate!!.time
            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        citaToReschedule?.let { cita ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val nuevaHora = timeFormat.format(nuevaFechaHora)
            viewModel.reprogramarCita(cita.id, nuevaFechaHora.time, nuevaHora)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
