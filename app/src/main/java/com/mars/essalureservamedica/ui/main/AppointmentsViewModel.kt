package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.data.entity.Calificacion
import com.mars.essalureservamedica.data.entity.EstadoCita
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.*

class AppointmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _appointments = MutableLiveData<List<CitaWithDoctorInfo>>()
    val appointments: LiveData<List<CitaWithDoctorInfo>> = _appointments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationResult = MutableLiveData<Result<String>?>()
    val operationResult: LiveData<Result<String>?> = _operationResult

    private val _historialCitas = MutableLiveData<List<CitaWithDoctorInfo>>()
    val historialCitas: LiveData<List<CitaWithDoctorInfo>> = _historialCitas

    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    // Observar los datos directamente
                    repository.getCitasWithDoctorByUserId(userId).observeForever { appointmentsList ->
                        _appointments.value = appointmentsList
                        _isLoading.value = false
                    }
                } else {
                    _appointments.value = emptyList()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _appointments.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun cancelarCita(citaId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCitaEstado(citaId, EstadoCita.CANCELADA.name)
                
                // Crear notificación automática para la cancelación
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val titulo = "Cita cancelada"
                    val mensaje = "Su cita ha sido cancelada exitosamente"
                    repository.crearNotificacionCita(userId, citaId, EstadoCita.CANCELADA.name, titulo, mensaje)
                }
                
                _operationResult.value = Result.success("Cita cancelada exitosamente")
                loadAppointments() // Recargar las citas
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reprogramarCita(citaId: Int, nuevaFechaHora: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.reprogramarCita(citaId, nuevaFechaHora)
                
                // Crear notificación automática para la reprogramación
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val titulo = "Cita reprogramada"
                    val mensaje = "Su cita ha sido reprogramada exitosamente"
                    repository.crearNotificacionCita(userId, citaId, EstadoCita.REPROGRAMADA.name, titulo, mensaje)
                }
                
                _operationResult.value = Result.success("Cita reprogramada exitosamente")
                loadAppointments() // Recargar las citas
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCitaEstado(citaId: Int, estado: EstadoCita) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCitaEstado(citaId, estado.name)
                
                // Crear notificación automática para el cambio de estado
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val titulo = "Estado de cita actualizado"
                    val mensaje = "Su cita ha sido ${estado.displayName.lowercase()}"
                    repository.crearNotificacionCita(userId, citaId, estado.name, titulo, mensaje)
                }
                
                _operationResult.value = Result.success("Estado actualizado a ${estado.displayName}")
                loadAppointments() // Recargar las citas
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHistorialCitas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val fechaActual = Date()
                    repository.getHistorialCitas(userId, fechaActual).observeForever { historial ->
                        _historialCitas.value = historial.map { cita ->
                            // Convertir Cita a CitaWithDoctorInfo
                            // Necesitaremos obtener la información del doctor
                            CitaWithDoctorInfo(
                                id = cita.id,
                                usuarioId = cita.usuarioId,
                                doctorId = cita.doctorId,
                                fechaHora = cita.fechaHora,
                                estado = cita.estado,
                                notas = cita.notas,
                                doctorNombre = "", // Se llenará con una consulta adicional
                                doctorEspecialidad = "" // Se llenará con una consulta adicional
                            )
                        }
                        _isLoading.value = false
                    }
                } else {
                    _historialCitas.value = emptyList()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _historialCitas.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }

    fun submitRating(calificacion: Calificacion) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertCalificacion(calificacion)
                _operationResult.value = Result.success("Calificación enviada exitosamente")
                loadAppointments() // Recargar las citas
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun canRateAppointment(citaId: Int): Boolean {
        return try {
            val existingRating = repository.getCalificacionByCitaId(citaId)
            existingRating == null // Solo se puede calificar si no existe una calificación previa
        } catch (e: Exception) {
            false
        }
    }
}