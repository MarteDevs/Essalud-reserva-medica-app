package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.DoctorFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DoctorsViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()

    private val _doctors = MutableLiveData<List<DoctorFirestore>>()
    val doctors: LiveData<List<DoctorFirestore>> = _doctors

    private val _filteredDoctors = MutableLiveData<List<DoctorFirestore>>()
    val filteredDoctors: LiveData<List<DoctorFirestore>> = _filteredDoctors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _especialidades = MutableLiveData<List<String>>()
    val especialidades: LiveData<List<String>> = _especialidades

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private var allDoctors: List<DoctorFirestore> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentEspecialidad: String = "Todas"

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Cargando doctores desde Firestore..."
            
            try {
                // Cargar directamente desde Firestore
                loadDoctorsFromFirestore()
            } catch (e: Exception) {
                _syncStatus.value = "Error al cargar doctores: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadDoctorsFromFirestore() {
        firestoreService.getDoctorsFlow()
            .catch { e ->
                _syncStatus.value = "Error de conexión: ${e.message}"
                _isLoading.value = false
            }
            .collect { doctorsList ->
                allDoctors = doctorsList
                _doctors.value = doctorsList
                _filteredDoctors.value = doctorsList
                
                // Extraer especialidades únicas
                val especialidadesUnicas = mutableSetOf("Todas")
                especialidadesUnicas.addAll(doctorsList.map { it.especialidad }.distinct())
                _especialidades.value = especialidadesUnicas.toList()
                
                _isLoading.value = false
                _syncStatus.value = "Datos cargados desde Firestore (${doctorsList.size} doctores)"
            }
    }

    fun searchDoctors(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterByEspecialidad(especialidad: String) {
        currentEspecialidad = especialidad
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = allDoctors

        // Filtrar por especialidad
        if (currentEspecialidad != "Todas") {
            filteredList = filteredList.filter { 
                it.especialidad.equals(currentEspecialidad, ignoreCase = true) 
            }
        }

        // Filtrar por búsqueda de nombre
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { doctor ->
                doctor.nombre.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        _filteredDoctors.value = filteredList
    }

    fun clearFilters() {
        currentSearchQuery = ""
        currentEspecialidad = "Todas"
        _filteredDoctors.value = allDoctors
    }

    // Función para recargar datos desde Firestore
    fun refreshDoctors() {
        viewModelScope.launch {
            loadDoctorsFromFirestore()
        }
    }
}