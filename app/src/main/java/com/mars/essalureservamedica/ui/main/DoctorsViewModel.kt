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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Observar los datos directamente
                repository.getAllDoctors().observeForever { doctorsList ->
                    _doctors.value = doctorsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _doctors.value = emptyList()
                _isLoading.value = false
            }
        }
    }
}