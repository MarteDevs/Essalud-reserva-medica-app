package com.mars.essalureservamedica.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    // Formatos para mostrar en la UI
    private val displayDateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    // Formato para la información de la cita actual
    private val currentAppointmentFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("es", "ES"))


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

        // Usar MaterialAlertDialogBuilder para un estilo consistente
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            // El título ya está en el layout, no es necesario aquí.
            // .setTitle("Reprogramar Cita")
            .setPositiveButton("Confirmar") { _, _ ->
                reprogramarCita()
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    private fun setupViews() {
        citaToReschedule?.let { cita ->
            // Usar el nuevo formato para el TextView de la cita actual
            binding.tvCurrentAppointment.text = currentAppointmentFormat.format(cita.fechaHora)
        }
    }

    private fun setupClickListeners() {
        // --- INICIO DE CAMBIOS ---
        // Ahora el listener se asigna al TextInputEditText, no a un botón
        binding.etSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.etSelectTime.setOnClickListener {
            showTimePicker()
        }
        // --- FIN DE CAMBIOS ---
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
                // --- INICIO DE CAMBIOS ---
                // Mostrar el texto dentro del EditText, no en un TextView separado
                binding.etSelectDate.setText(displayDateFormat.format(selectedDate!!.time))
                // Ya no necesitamos los tvSelectedDate/Time
                // binding.tvSelectedDate.visibility = android.view.View.GONE
                // --- FIN DE CAMBIOS ---
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
                // --- INICIO DE CAMBIOS ---
                // Mostrar el texto dentro del EditText
                binding.etSelectTime.setText(displayTimeFormat.format(selectedTime!!.time))
                // Ya no necesitamos los tvSelectedDate/Time
                // binding.tvSelectedTime.visibility = android.view.View.GONE
                // --- FIN DE CAMBIOS ---
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // Usar formato 12h (AM/PM)
        ).show()
    }

    private fun reprogramarCita() {
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Por favor selecciona fecha y hora", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaFechaHora = Calendar.getInstance().apply {
            time = selectedDate!!.time // Copia la fecha
            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY)) // Añade la hora
            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE)) // Añade los minutos
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
