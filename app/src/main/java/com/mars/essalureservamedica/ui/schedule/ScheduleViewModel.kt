package com.mars.essalureservamedica.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.CitaFirestore
import com.mars.essalureservamedica.data.firebase.models.DoctorFirestore
import com.mars.essalureservamedica.data.firebase.models.NotificacionFirestore
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val sessionManager = SessionManager(application)

    private val _doctor = MutableLiveData<DoctorFirestore?>()
    val doctor: LiveData<DoctorFirestore?> = _doctor

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _appointmentResult = MutableLiveData<Result<Unit>?>()
    val appointmentResult: LiveData<Result<Unit>?> = _appointmentResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _horasOcupadas = MutableLiveData<List<String>>()
    val horasOcupadas: LiveData<List<String>> = _horasOcupadas

    fun loadDoctorDetails(doctorId: String) {
        viewModelScope.launch {
            try {
                val result = firestoreService.getDoctor(doctorId)
                if (result.isSuccess) {
                    _doctor.value = result.getOrNull()
                } else {
                    _errorMessage.value = "Error al cargar información del doctor"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar información del doctor: ${e.message}"
            }
        }
    }

    // Esta función se ejecuta en segundo plano SIN bloquear la UI
    fun cargarHorasOcupadas(doctorId: String, fecha: String) {
        viewModelScope.launch {
            try {
                // Convertir fecha string a timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaDate = dateFormat.parse(fecha)
                val fechaTimestamp = fechaDate?.time ?: return@launch

                // Obtener citas desde Firestore
                val result = firestoreService.getCitasByDoctorAndDate(doctorId, fechaTimestamp)
                if (result.isSuccess) {
                    val citas = result.getOrNull() ?: emptyList()
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val horasOcupadasList = citas.map { it.hora }
                    
                    _horasOcupadas.postValue(horasOcupadasList) // Notifica a la UI cuando termina
                } else {
                    _errorMessage.value = "Error al cargar horas ocupadas"
                    _horasOcupadas.postValue(emptyList())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar horas ocupadas: ${e.message}"
                _horasOcupadas.postValue(emptyList())
            }
        }
    }

    fun scheduleAppointment(doctorId: String, dateTime: Date, notes: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserIdAsString()
                if (userId == "-1") {
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

                val cita = CitaFirestore(
                    id = "", // Se auto-genera en Firestore
                    usuarioId = userId,
                    doctorId = doctorId,
                    fecha = dateTime.time,
                    hora = horaSeleccionada,
                    estado = "Confirmada",
                    motivo = notes
                )

                val result = firestoreService.createCita(cita)
                
                if (result.isSuccess) {
                    val citaId = result.getOrNull()
                    if (citaId != null) {
                        // Crear notificación automática para la nueva cita
                        val notificacion = NotificacionFirestore(
                            usuarioId = userId,
                            titulo = "Nueva cita agendada",
                            mensaje = "Su cita ha sido agendada exitosamente",
                            tipo = "CITA_CONFIRMADA",
                            citaId = citaId
                        )
                        firestoreService.createNotificacion(notificacion)
                    }
                    _appointmentResult.value = Result.success(Unit)
                } else {
                    _appointmentResult.value = Result.failure(Exception("Error al crear la cita"))
                }
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