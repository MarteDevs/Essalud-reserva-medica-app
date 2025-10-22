package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.entity.Cita
import com.mars.essalureservamedica.data.entity.DoctorConFrecuencia
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val sessionManager: SessionManager

    private val _totalDoctors = MutableLiveData<Int>()
    val totalDoctors: LiveData<Int> = _totalDoctors

    private val _totalAppointments = MutableLiveData<Int>()
    val totalAppointments: LiveData<Int> = _totalAppointments

    init {
        repository = AppRepository.getInstance(application)
        sessionManager = SessionManager(application)
    }
    private val _doctoresFrecuentes = MutableLiveData<List<DoctorConFrecuencia>>()
    val doctoresFrecuentes: LiveData<List<DoctorConFrecuencia>> = _doctoresFrecuentes


    private val _citasUsuario = MutableLiveData<List<Cita>>()
    val citasUsuario: LiveData<List<Cita>> = _citasUsuario

    fun loadStats() {
        viewModelScope.launch {
            try {
                // Cargar total de doctores
                val doctorCount = repository.getDoctorCount()
                _totalDoctors.value = doctorCount

                // Cargar total de citas del usuario actual
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val userAppointments = repository.getCitasByUserId(userId).value ?: emptyList()
                    _totalAppointments.value = userAppointments.size
                } else {
                    _totalAppointments.value = 0
                }
            } catch (e: Exception) {
                _totalDoctors.value = 0
                _totalAppointments.value = 0
            }
        }
    }


    fun loadDoctoresFrecuentes() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val doctores = repository.getDoctoresFrecuentes(userId)
                _doctoresFrecuentes.value = doctores
            } else {
                _doctoresFrecuentes.value = emptyList()
            }
        }
    }


    fun loadCitasUsuario() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                repository.getCitasByUserId(userId).observeForever { citas ->
                    _citasUsuario.value = citas
                }
            } else {
                _citasUsuario.value = emptyList()
            }
        }
    }

    suspend fun getDoctorById(doctorId: Int) = repository.getDoctorById(doctorId)

}