package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.data.entity.Doctor
import kotlinx.coroutines.launch

class DoctorsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors

    private val _filteredDoctors = MutableLiveData<List<Doctor>>()
    val filteredDoctors: LiveData<List<Doctor>> = _filteredDoctors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _especialidades = MutableLiveData<List<String>>()
    val especialidades: LiveData<List<String>> = _especialidades

    private var allDoctors: List<Doctor> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentEspecialidad: String = "Todas"

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Observar los datos directamente
                repository.getAllDoctors().observeForever { doctorsList ->
                    allDoctors = doctorsList
                    _doctors.value = doctorsList
                    _filteredDoctors.value = doctorsList
                    
                    // Extraer especialidades únicas
                    val especialidadesUnicas = mutableSetOf("Todas")
                    especialidadesUnicas.addAll(doctorsList.map { it.especialidad }.distinct())
                    _especialidades.value = especialidadesUnicas.toList()
                    
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _doctors.value = emptyList()
                _filteredDoctors.value = emptyList()
                _isLoading.value = false
            }
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
}