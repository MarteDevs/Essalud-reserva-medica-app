package com.mars.essalureservamedica.ui.doctor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.DoctorFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ScheduleSlot(
    val date: String,
    val time: String,
    val isAvailable: Boolean
)

class DoctorDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()

    private val _doctor = MutableLiveData<DoctorFirestore?>()
    val doctor: LiveData<DoctorFirestore?> = _doctor

    private val _availableSchedules = MutableLiveData<List<ScheduleSlot>>()
    val availableSchedules: LiveData<List<ScheduleSlot>> = _availableSchedules

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDoctorDetails(doctorId: String) {
        viewModelScope.launch {
            try {
                val result = firestoreService.getDoctor(doctorId)
                if (result.isSuccess) {
                    _doctor.value = result.getOrNull()
                } else {
                    _doctor.value = null
                }
            } catch (e: Exception) {
                _doctor.value = null
            }
        }
    }

    fun loadAvailableSchedules(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Generar horarios de ejemplo para los próximos 7 días
                val schedules = generateSampleSchedules()
                _availableSchedules.value = schedules
            } catch (e: Exception) {
                _availableSchedules.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateSampleSchedules(): List<ScheduleSlot> {
        val schedules = mutableListOf<ScheduleSlot>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Generar horarios para los próximos 7 días
        for (day in 1..7) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val date = dateFormat.format(calendar.time)
            
            // Horarios de mañana
            schedules.add(ScheduleSlot(date, "09:00 AM", true))
            schedules.add(ScheduleSlot(date, "10:00 AM", true))
            schedules.add(ScheduleSlot(date, "11:00 AM", false)) // Ocupado
            
            // Horarios de tarde
            schedules.add(ScheduleSlot(date, "02:00 PM", true))
            schedules.add(ScheduleSlot(date, "03:00 PM", true))
            schedules.add(ScheduleSlot(date, "04:00 PM", true))
        }
        
        return schedules
    }
}