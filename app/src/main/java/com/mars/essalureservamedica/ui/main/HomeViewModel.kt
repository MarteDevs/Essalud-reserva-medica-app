package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.data.firebase.models.DoctorFirestore
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val sessionManager = SessionManager(application)

    // Nombre de usuario
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    // Estadísticas generales
    private val _totalDoctors = MutableLiveData<Int>()
    val totalDoctors: LiveData<Int> = _totalDoctors

    private val _totalAppointments = MutableLiveData<Int>()
    val totalAppointments: LiveData<Int> = _totalAppointments

    private val _recentActivity = MutableLiveData<String>()
    val recentActivity: LiveData<String> = _recentActivity

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Datos de citas
    private val _appointments = MutableLiveData<List<CitaWithDoctorFirestore>>()
    val appointments: LiveData<List<CitaWithDoctorFirestore>> = _appointments

    // Top 3 doctores frecuentes
    private val _frequentDoctors = MutableLiveData<List<Pair<DoctorFirestore, Int>>>()
    val frequentDoctors: LiveData<List<Pair<DoctorFirestore, Int>>> = _frequentDoctors

    init {
        loadUser()
        loadTotalDoctors()
        loadAppointmentsFromFirestore()
    }

    fun loadUser() {
        viewModelScope.launch {
            val userId = sessionManager.getUserIdAsString()
            if (userId != "-1") {
                val result = firestoreService.getUser(userId)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _userName.value = user?.nombreCompleto ?: "Usuario"
                } else {
                    _userName.value = "Usuario"
                }
            } else {
                _userName.value = "Usuario"
            }
        }
    }

    /** Cargar total de doctores desde Firestore (o DAO si quieres local) */
    private fun loadTotalDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doctorsResult = firestoreService.getAllDoctors()
                if (doctorsResult.isSuccess) {
                    _totalDoctors.value = doctorsResult.getOrNull()?.size ?: 0
                } else {
                    _totalDoctors.value = 0
                }
            } catch (e: Exception) {
                _totalDoctors.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Cargar citas del usuario y calcular top 3 doctores */
    fun loadAppointmentsFromFirestore() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Cargando citas..."
            val userId = sessionManager.getUserIdAsString()
            if (userId == "-1") {
                _appointments.value = emptyList()
                _totalAppointments.value = 0
                _recentActivity.value = "Usuario no autenticado"
                _isLoading.value = false
                return@launch
            }

            firestoreService.getUserCitasFlow(userId)
                .catch { e ->
                    _appointments.value = emptyList()
                    _totalAppointments.value = 0
                    _recentActivity.value = "Error al cargar citas: ${e.message}"
                    _isLoading.value = false
                }
                .collect { citasFirestore ->
                    val citasWithDoctor: List<CitaWithDoctorFirestore> = citasFirestore.mapNotNull { cita ->
                        try {
                            val doctorResult = firestoreService.getDoctor(cita.doctorId)
                            if (doctorResult.isSuccess) {
                                val doctor = doctorResult.getOrNull()
                                doctor?.let { CitaWithDoctorFirestore.from(cita, it) }
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // Actualizar LiveData
                    _appointments.value = citasWithDoctor
                    _totalAppointments.value = citasWithDoctor.size

                    // Top 3 doctores frecuentes
                    val doctorCountMap = citasWithDoctor.groupingBy { it.doctorId }.eachCount()
                    val topDoctors = doctorCountMap.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { entry ->
                            val doctor = citasWithDoctor.first { it.doctorId == entry.key }
                            DoctorFirestore(
                                nombre = doctor.doctorNombre,
                                especialidad = doctor.doctorEspecialidad,
                                foto = doctor.doctorFoto,
                                rating = doctor.doctorRating
                            ) to entry.value
                        }
                    _frequentDoctors.value = topDoctors

                    // Actividad reciente
                    _recentActivity.value = if (citasWithDoctor.isNotEmpty()) {
                        val ahora = Date()
                        val proximaCita = citasWithDoctor
                            .filter { it.fechaHora.after(ahora) }
                            .minByOrNull { it.fechaHora.time }

                        if (proximaCita != null) {
                            "Próxima cita: ${formatDate(proximaCita.fechaHora.time)}"
                        } else {
                            "No hay próximas citas"
                        }
                    } else {
                        "No hay citas registradas"
                    }

                    _syncStatus.value = "Citas cargadas"
                    _isLoading.value = false
                }
        }
    }

    private fun formatDate(timestamp: Long): String = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    } catch (e: Exception) {
        "Fecha no válida"
    }

    fun refreshStats() {
        loadTotalDoctors()
        loadAppointmentsFromFirestore()
    }
}
