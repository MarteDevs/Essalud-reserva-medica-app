package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.database.AppDatabase
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
}