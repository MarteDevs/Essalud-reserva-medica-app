package com.mars.essalureservamedica.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.entity.Cita
import com.mars.essalureservamedica.data.entity.Doctor
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _doctor = MutableLiveData<Doctor?>()
    val doctor: LiveData<Doctor?> = _doctor

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _appointmentResult = MutableLiveData<Result<Unit>?>()
    val appointmentResult: LiveData<Result<Unit>?> = _appointmentResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _horasOcupadas = MutableLiveData<List<String>>()
    val horasOcupadas: LiveData<List<String>> = _horasOcupadas

    fun loadDoctorDetails(doctorId: Int) {
        viewModelScope.launch {
            try {
                val doctorData = repository.getDoctorById(doctorId)
                _doctor.value = doctorData
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar información del doctor: ${e.message}"
            }
        }
    }

    // Esta función se ejecuta en segundo plano SIN bloquear la UI
    fun cargarHorasOcupadas(doctorId: Int, fecha: String) {
        viewModelScope.launch {
            try {
                // Convertir fecha string a timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaDate = dateFormat.parse(fecha)
                val fechaTimestamp = fechaDate?.time ?: return@launch

                // La operación de base de datos se hace aquí, en una corrutina
                val citas = repository.getCitasPorDoctorYFecha(doctorId, fechaTimestamp)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val horasOcupadasList = citas.map { timeFormat.format(it.fechaHora) }
                
                _horasOcupadas.postValue(horasOcupadasList) // Notifica a la UI cuando termina
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar horas ocupadas: ${e.message}"
                _horasOcupadas.postValue(emptyList())
            }
        }
    }

    fun scheduleAppointment(doctorId: Int, dateTime: Date, notes: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserId()
                if (userId == -1) {
                    _appointmentResult.value = Result.failure(Exception("Usuario no autenticado"))
                    return@launch
                }

                // Verificar si ya existe una cita en esa fecha y hora
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val horaSeleccionada = timeFormat.format(dateTime)
                val horasOcupadasActuales = _horasOcupadas.value ?: emptyList()
                
                if (horasOcupadasActuales.contains(horaSeleccionada)) {
                    _appointmentResult.value = Result.failure(Exception("Esta hora ya está ocupada"))
                    return@launch
                }

                val cita = Cita(
                    id = 0, // Se auto-genera
                    usuarioId = userId,
                    doctorId = doctorId,
                    fechaHora = dateTime,
                    estado = "Confirmada",
                    notas = notes.ifEmpty { null }
                )

                repository.insertCita(cita)
                _appointmentResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _appointmentResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _appointmentResult.value = null
        _errorMessage.value = null
    }
}