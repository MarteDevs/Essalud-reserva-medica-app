package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.CitaFirestore
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.data.firebase.models.CalificacionFirestore
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.util.*

class AppointmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val sessionManager = SessionManager(application)

    private val _appointments = MutableLiveData<List<CitaWithDoctorFirestore>>()
    val appointments: LiveData<List<CitaWithDoctorFirestore>> = _appointments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationResult = MutableLiveData<Result<String>?>()
    val operationResult: LiveData<Result<String>?> = _operationResult

    private val _historialCitas = MutableLiveData<List<CitaWithDoctorFirestore>>()
    val historialCitas: LiveData<List<CitaWithDoctorFirestore>> = _historialCitas

    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserIdAsString()
                if (userId != "-1") {
                    // Usar Flow para obtener citas en tiempo real desde Firestore
                    firestoreService.getUserCitasFlow(userId)
                        .catch { e ->
                            _operationResult.value = Result.failure(Exception("Error al cargar citas: ${e.message}"))
                            _appointments.value = emptyList()
                            _isLoading.value = false
                        }
                        .collect { citas ->
                            // Convertir CitaFirestore a CitaWithDoctorFirestore
                            val citasWithDoctor = citas.mapNotNull { cita ->
                                try {
                                    val doctorResult = firestoreService.getDoctor(cita.doctorId)
                                    if (doctorResult.isSuccess) {
                                        val doctor = doctorResult.getOrNull()
                                        if (doctor != null) {
                                            CitaWithDoctorFirestore.from(cita, doctor)
                                        } else null
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            _appointments.value = citasWithDoctor
                            _isLoading.value = false
                        }
                } else {
                    _appointments.value = emptyList()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(Exception("Error al cargar citas: ${e.message}"))
                _appointments.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun cancelarCita(citaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firestoreService.updateCitaEstado(citaId, "CANCELADA")
                if (result.isSuccess) {
                    _operationResult.value = Result.success("Cita cancelada exitosamente")
                    // Las citas se actualizarán automáticamente a través del Flow
                } else {
                    _operationResult.value = Result.failure(Exception("Error al cancelar la cita"))
                }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reprogramarCita(citaId: String, nuevaFecha: Long, nuevaHora: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mapOf(
                    "fecha" to nuevaFecha,
                    "hora" to nuevaHora,
                    "estado" to "REPROGRAMADA"
                )
                val result = firestoreService.updateCita(citaId, updates)
                if (result.isSuccess) {
                    _operationResult.value = Result.success("Cita reprogramada exitosamente")
                    // Las citas se actualizarán automáticamente a través del Flow
                } else {
                    _operationResult.value = Result.failure(Exception("Error al reprogramar la cita"))
                }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCitaEstado(citaId: String, estado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firestoreService.updateCitaEstado(citaId, estado)
                if (result.isSuccess) {
                    _operationResult.value = Result.success("Estado actualizado a $estado")
                    // Las citas se actualizarán automáticamente a través del Flow
                } else {
                    _operationResult.value = Result.failure(Exception("Error al actualizar el estado"))
                }
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
                val userId = sessionManager.getUserIdAsString()
                if (userId != "-1") {
                    // Usar Flow para obtener historial de citas desde Firestore
                    firestoreService.getUserCitasFlow(userId)
                        .catch { e ->
                            _operationResult.value = Result.failure(Exception("Error al cargar historial: ${e.message}"))
                            _historialCitas.value = emptyList()
                            _isLoading.value = false
                        }
                        .collect { citas ->
                            // Filtrar citas pasadas o completadas para el historial
                            val fechaActual = System.currentTimeMillis()
                            val historial = citas.filter { cita ->
                                cita.fecha < fechaActual || cita.estado in listOf("COMPLETADA", "CANCELADA")
                            }
                            
                            // Convertir CitaFirestore a CitaWithDoctorFirestore
                            val historialWithDoctor = historial.mapNotNull { cita ->
                                try {
                                    val doctorResult = firestoreService.getDoctor(cita.doctorId)
                                    if (doctorResult.isSuccess) {
                                        val doctor = doctorResult.getOrNull()
                                        if (doctor != null) {
                                            CitaWithDoctorFirestore.from(cita, doctor)
                                        } else null
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            _historialCitas.value = historialWithDoctor
                            _isLoading.value = false
                        }
                } else {
                    _historialCitas.value = emptyList()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(Exception("Error al cargar historial: ${e.message}"))
                _historialCitas.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }

    fun submitRating(calificacion: CalificacionFirestore) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firestoreService.addCalificacion(calificacion)
                if (result.isSuccess) {
                    _operationResult.value = Result.success("Calificación enviada exitosamente")
                } else {
                    _operationResult.value = Result.failure(Exception("Error al enviar la calificación"))
                }
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun canRateAppointment(citaId: String): Boolean {
        return try {
            val result = firestoreService.getCalificacionByCitaId(citaId)
            result.isSuccess && result.getOrNull() == null // Solo se puede calificar si no existe una calificación previa
        } catch (e: Exception) {
            false
        }
    }
}