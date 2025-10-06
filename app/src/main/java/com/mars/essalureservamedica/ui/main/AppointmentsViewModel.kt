package com.mars.essalureservamedica.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class AppointmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _appointments = MutableLiveData<List<CitaWithDoctorInfo>>()
    val appointments: LiveData<List<CitaWithDoctorInfo>> = _appointments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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
}