package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.data.entity.Doctor
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.DoctorFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class DoctorsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val firestoreService = FirestoreService()

    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors

    private val _filteredDoctors = MutableLiveData<List<Doctor>>()
    val filteredDoctors: LiveData<List<Doctor>> = _filteredDoctors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _especialidades = MutableLiveData<List<String>>()
    val especialidades: LiveData<List<String>> = _especialidades

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private var allDoctors: List<Doctor> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentEspecialidad: String = "Todas"

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Sincronizando doctores..."
            
            try {
                // 1. Cargar desde Firestore (fuente principal)
                loadDoctorsFromFirestore()
                
                // 2. Sincronizar con Room para cache local
                syncWithLocalDatabase()
                
            } catch (e: Exception) {
                // En caso de error, cargar desde Room como fallback
                loadDoctorsFromRoom()
            }
        }
    }

    private suspend fun loadDoctorsFromFirestore() {
        firestoreService.getDoctorsFlow()
            .map { firestoreDoctors -> 
                firestoreDoctors.map { it.toDoctor() }
            }
            .catch { e ->
                _syncStatus.value = "Error de conexión, usando datos locales"
                loadDoctorsFromRoom()
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
                _syncStatus.value = "Datos actualizados desde Firestore"
            }
    }

    private suspend fun loadDoctorsFromRoom() {
        repository.getAllDoctors().observeForever { doctorsList ->
            if (doctorsList.isNotEmpty()) {
                allDoctors = doctorsList
                _doctors.value = doctorsList
                _filteredDoctors.value = doctorsList
                
                // Extraer especialidades únicas
                val especialidadesUnicas = mutableSetOf("Todas")
                especialidadesUnicas.addAll(doctorsList.map { it.especialidad }.distinct())
                _especialidades.value = especialidadesUnicas.toList()
                
                _syncStatus.value = "Usando datos locales"
            }
            _isLoading.value = false
        }
    }

    private suspend fun syncWithLocalDatabase() {
        try {
            // Obtener doctores de Firestore para sincronizar con Room
            val firestoreResult = firestoreService.getAllDoctors()
            if (firestoreResult.isSuccess) {
                val firestoreDoctors = firestoreResult.getOrNull() ?: emptyList()
                
                // Convertir y guardar en Room para cache
                firestoreDoctors.forEach { doctorFirestore ->
                    val doctor = doctorFirestore.toDoctor()
                    // Verificar si ya existe en Room antes de insertar
                    val existingDoctor = repository.getDoctorById(doctor.id)
                    if (existingDoctor == null) {
                        repository.insertDoctor(doctor)
                    } else {
                        // Actualizar doctor existente
                        repository.updateDoctor(doctor)
                    }
                }
            }
        } catch (e: Exception) {
            // Error en sincronización, continuar con datos de Firestore
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

    // Función de extensión para convertir DoctorFirestore a Doctor
    private fun DoctorFirestore.toDoctor(): Doctor {
        return Doctor(
            id = this.id.hashCode(), // Convertir String ID a Int para compatibilidad con Room
            nombre = this.nombre,
            especialidad = this.especialidad,
            experiencia = this.experiencia,
            disponibilidad = this.disponibilidad,
            foto = this.foto
        )
    }

    // Función para forzar sincronización manual
    fun forceSyncWithFirestore() {
        viewModelScope.launch {
            loadDoctorsFromFirestore()
        }
    }
}