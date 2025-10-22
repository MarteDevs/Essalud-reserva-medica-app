package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

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
        sessionManager = SessionManager(application)
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Cargando estadísticas desde Firestore..."
            
            try {
                // Cargar estadísticas directamente desde Firestore
                loadStatsFromFirestore()
            } catch (e: Exception) {
                _syncStatus.value = "Error al cargar estadísticas: ${e.message}"
                _isLoading.value = false
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
            val userId = sessionManager.getUserIdAsString()
            if (userId != "-1") {
                // Usar Flow para obtener citas en tiempo real
                firestoreService.getUserCitasFlow(userId)
                    .catch { e ->
                        _syncStatus.value = "Error al cargar citas: ${e.message}"
                        _totalAppointments.value = 0
                        _recentActivity.value = "Error al cargar citas"
                        _isLoading.value = false
                    }
                    .collect { citas ->
                        _totalAppointments.value = citas.size
                        _recentActivity.value = if (citas.isNotEmpty()) {
                            "Última cita: ${citas.firstOrNull()?.let { formatDate(it.fecha) } ?: "N/A"}"
                        } else {
                            "No hay citas registradas"
                        }
                        _syncStatus.value = "Estadísticas cargadas desde Firestore (${citas.size} citas, ${_totalDoctors.value ?: 0} doctores)"
                        _isLoading.value = false
                    }
            } else {
                _totalAppointments.value = 0
                _recentActivity.value = "Usuario no autenticado"
                _syncStatus.value = "Usuario no autenticado"
                _isLoading.value = false
            }
            
        } catch (e: Exception) {
            _syncStatus.value = "Error de conexión: ${e.message}"
            _totalDoctors.value = 0
            _totalAppointments.value = 0
            _recentActivity.value = "Error al cargar datos"
            _isLoading.value = false
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

    // Función para recargar estadísticas desde Firestore
    fun refreshStats() {
        loadStats()
    }
}