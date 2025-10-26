package com.mars.essalureservamedica.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mars.essalureservamedica.databinding.ActivityScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()
    
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        val doctorId = intent.getStringExtra("doctor_id")
        if (doctorId != null && doctorId.isNotEmpty()) {
            viewModel.loadDoctorDetails(doctorId)
        }

        // Pre-llenar fecha y hora si vienen del intent
        intent.getStringExtra("schedule_date")?.let { date ->
            binding.etDate.setText(date)
        }
        intent.getStringExtra("schedule_time")?.let { time ->
            binding.etTime.setText(time)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Agendar Cita"
        }
    }

    private fun setupClickListeners() {
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.etTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnConfirmAppointment.setOnClickListener {
            scheduleAppointment()
        }
    }

    private fun observeViewModel() {
        viewModel.doctor.observe(this) { doctor ->
            doctor?.let {
                binding.apply {
                    tvDoctorName.text = it.nombre
                    tvSpecialty.text = it.especialidad
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnConfirmAppointment.isEnabled = !isLoading
        }

        viewModel.appointmentResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(this, "Cita agendada exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, it.exceptionOrNull()?.message ?: "Error al agendar la cita", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observa los cambios para actualizar la UI sin congelarla
        viewModel.horasOcupadas.observe(this) { horas ->
            // Aquí actualizas tu RecyclerView o los botones de las horas,
            // deshabilitando las que ya están ocupadas.
            android.util.Log.d("ScheduleActivity", "Horas ocupadas recibidas: $horas")
            // Mostrar información al usuario sobre las horas ocupadas
            if (horas.isNotEmpty()) {
                val horasOcupadasText = "Horas ocupadas: ${horas.joinToString(", ")}"
                Toast.makeText(this, horasOcupadasText, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val fechaSeleccionada = dateFormat.format(selectedDate!!.time)
                binding.etDate.setText(fechaSeleccionada)
                
                // Cargar horas ocupadas cuando se selecciona una fecha
                val doctorId = intent.getStringExtra("doctor_id")
                if (doctorId != null && doctorId.isNotEmpty()) {
                    viewModel.cargarHorasOcupadas(doctorId, fechaSeleccionada)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // No permitir fechas pasadas
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                binding.etTime.setText(timeFormat.format(selectedTime!!.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun scheduleAppointment() {
        val doctorId = intent.getStringExtra("doctor_id")
        val date = binding.etDate.text.toString()
        val time = binding.etTime.text.toString()
        val notes = binding.etNotes.text.toString()

        if (doctorId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Doctor no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una hora", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Por favor selecciona fecha y hora válidas", Toast.LENGTH_SHORT).show()
            return
        }

        // Combinar fecha y hora
        val appointmentDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate!!.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate!!.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate!!.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        viewModel.scheduleAppointment(doctorId, appointmentDateTime.time, notes)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}