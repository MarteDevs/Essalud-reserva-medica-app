package com.mars.essalureservamedica.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.ui.notifications.NotificationsViewModel
import com.mars.essalureservamedica.ui.profile.ProfileViewModel
import com.mars.essalureservamedica.utils.SessionManager

class ViewModelFactory(
    private val repository: AppRepository,
    private val sessionManager: SessionManager,
    private val application: android.app.Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}