package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.database.AppDatabase
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val sessionManager: SessionManager
    private val firestoreService = FirestoreService()

    private val _totalDoctors = MutableLiveData<Int>()
    val totalDoctors: LiveData<Int> = _totalDoctors

    private val _totalAppointments = MutableLiveData<Int>()
    val totalAppointments: LiveData<Int> = _totalAppointments

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _recentActivity = MutableLiveData<String>()
    val recentActivity: LiveData<String> = _recentActivity

    init {
        repository = AppRepository.getInstance(application)
        sessionManager = SessionManager(application)
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Cargando estadísticas..."
            
            try {
                // 1. Cargar estadísticas desde Firestore (fuente principal)
                loadStatsFromFirestore()
                
                // 2. Sincronizar con datos locales como fallback
                syncWithLocalStats()
                
            } catch (e: Exception) {
                // En caso de error, usar datos locales
                loadStatsFromRoom()
            }
        }
    }

    private suspend fun loadStatsFromFirestore() {
        try {
            // Cargar total de doctores desde Firestore
            val doctorsResult = firestoreService.getAllDoctors()
            if (doctorsResult.isSuccess) {
                val doctorCount = doctorsResult.getOrNull()?.size ?: 0
                _totalDoctors.value = doctorCount
            }

            // Cargar citas del usuario actual desde Firestore
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val userIdString = userId.toString()
                
                // Usar Flow para obtener citas en tiempo real
                firestoreService.getUserCitasFlow(userIdString)
                    .catch { e ->
                        _syncStatus.value = "Error al cargar citas, usando datos locales"
                        loadAppointmentsFromRoom()
                    }
                    .collect { citas ->
                        _totalAppointments.value = citas.size
                        _recentActivity.value = if (citas.isNotEmpty()) {
                            "Última cita: ${citas.firstOrNull()?.let { formatDate(it.fecha) } ?: "N/A"}"
                        } else {
                            "No hay citas registradas"
                        }
                        _syncStatus.value = "Datos actualizados desde Firestore"
                    }
            } else {
                _totalAppointments.value = 0
                _recentActivity.value = "Usuario no autenticado"
            }
            
            _isLoading.value = false
            
        } catch (e: Exception) {
            _syncStatus.value = "Error de conexión, usando datos locales"
            loadStatsFromRoom()
        }
    }

    private suspend fun loadStatsFromRoom() {
        try {
            // Cargar total de doctores desde Room
            val doctorCount = repository.getDoctorCount()
            _totalDoctors.value = doctorCount

            // Cargar total de citas del usuario actual desde Room
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val userAppointments = repository.getCitasByUserId(userId).value ?: emptyList()
                _totalAppointments.value = userAppointments.size
                
                _recentActivity.value = if (userAppointments.isNotEmpty()) {
                    "Última cita: ${userAppointments.firstOrNull()?.fechaHora?.let { formatDate(it.time) } ?: "N/A"}"
                } else {
                    "No hay citas registradas"
                }
            } else {
                _totalAppointments.value = 0
                _recentActivity.value = "Usuario no autenticado"
            }
            
            _syncStatus.value = "Usando datos locales"
            
        } catch (e: Exception) {
            _totalDoctors.value = 0
            _totalAppointments.value = 0
            _recentActivity.value = "Error al cargar datos"
            _syncStatus.value = "Error al cargar estadísticas"
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun loadAppointmentsFromRoom() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            val userAppointments = repository.getCitasByUserId(userId).value ?: emptyList()
            _totalAppointments.value = userAppointments.size
        }
    }

    private suspend fun syncWithLocalStats() {
        try {
            // Sincronizar doctores con Room para cache
            val doctorsResult = firestoreService.getAllDoctors()
            if (doctorsResult.isSuccess) {
                val firestoreDoctors = doctorsResult.getOrNull() ?: emptyList()
                // Aquí podrías sincronizar los doctores con Room si es necesario
            }
            
            // Sincronizar citas con Room para cache
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val citasResult = firestoreService.getUserCitasFlow(userId.toString())
                // La sincronización de citas se maneja en el flow
            }
            
        } catch (e: Exception) {
            // Error en sincronización, continuar con datos actuales
        }
    }

    // Función para formatear fechas
    private fun formatDate(timestamp: Long): String {
        return try {
            val date = java.util.Date(timestamp)
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Fecha no válida"
        }
    }

    // Función para forzar actualización manual
    fun refreshStats() {
        loadStats()
    }
}