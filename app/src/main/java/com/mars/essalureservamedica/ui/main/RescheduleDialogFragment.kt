package com.mars.essalureservamedica.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.databinding.DialogRescheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class RescheduleDialogFragment : DialogFragment() {

    private var _binding: DialogRescheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentsViewModel by viewModels({ requireParentFragment() })

    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    private var citaToReschedule: CitaWithDoctorInfo? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val ARG_CITA = "cita"

        fun newInstance(cita: CitaWithDoctorInfo): RescheduleDialogFragment {
            val fragment = RescheduleDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_CITA, cita)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRescheduleBinding.inflate(layoutInflater)
        
        citaToReschedule = arguments?.getSerializable(ARG_CITA) as? CitaWithDoctorInfo

        setupViews()
        setupClickListeners()

        return androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Reprogramar Cita")
            .setPositiveButton("Confirmar") { _, _ ->
                reprogramarCita()
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    private fun setupViews() {
        citaToReschedule?.let { cita ->
            binding.tvCurrentAppointment.text = "Cita actual: ${dateFormat.format(cita.fechaHora)} a las ${timeFormat.format(cita.fechaHora)}"
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Mínimo mañana

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                binding.tvSelectedDate.text = "Fecha: ${dateFormat.format(selectedDate!!.time)}"
                binding.tvSelectedDate.visibility = android.view.View.VISIBLE
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
                binding.tvSelectedTime.text = "Hora: ${timeFormat.format(selectedTime!!.time)}"
                binding.tvSelectedTime.visibility = android.view.View.VISIBLE
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun reprogramarCita() {
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Por favor selecciona fecha y hora", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaFechaHora = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate!!.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate!!.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate!!.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        citaToReschedule?.let { cita ->
            viewModel.reprogramarCita(cita.id, nuevaFechaHora)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}