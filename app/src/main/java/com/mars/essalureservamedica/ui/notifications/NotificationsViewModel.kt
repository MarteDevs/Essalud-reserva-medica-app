package com.mars.essalureservamedica.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.models.NotificacionFirestore
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _notificaciones = MutableLiveData<List<NotificacionFirestore>>()
    val notificaciones: LiveData<List<NotificacionFirestore>> = _notificaciones

    private val _notificacionesNoLeidas = MutableLiveData<List<NotificacionFirestore>>()
    val notificacionesNoLeidas: LiveData<List<NotificacionFirestore>> = _notificacionesNoLeidas

    private val _countNoLeidas = MutableLiveData<Int>()
    val countNoLeidas: LiveData<Int> = _countNoLeidas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _operationResult = MutableLiveData<String?>()
    val operationResult: LiveData<String?> = _operationResult

    init {
        loadNotificaciones()
    }

    private fun loadNotificaciones() {
        val userId = sessionManager.getUserIdAsString()
        if (userId != "-1") {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    
                    // Cargar todas las notificaciones desde Firestore
                    val notificacionesResult = firestoreService.getNotificacionesByUserId(userId)
                    if (notificacionesResult.isSuccess) {
                        val notificaciones = notificacionesResult.getOrNull() ?: emptyList()
                        _notificaciones.value = notificaciones
                        
                        // Filtrar notificaciones no leídas
                        val noLeidas = notificaciones.filter { !it.leida }
                        _notificacionesNoLeidas.value = noLeidas
                        _countNoLeidas.value = noLeidas.size
                    } else {
                        _errorMessage.value = "Error al cargar notificaciones desde Firestore"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error de conexión: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = firestoreService.marcarNotificacionComoLeida(notificacionId)
                if (result.isSuccess) {
                    _operationResult.value = "Notificación marcada como leída"
                    // Recargar notificaciones para actualizar la UI
                    loadNotificaciones()
                } else {
                    _errorMessage.value = "Error al marcar notificación como leída"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar notificación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun marcarTodasComoLeidas() {
        val userId = sessionManager.getUserIdAsString()
        if (userId != "-1") {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    val result = firestoreService.marcarTodasNotificacionesComoLeidas(userId)
                    if (result.isSuccess) {
                        _operationResult.value = "Todas las notificaciones marcadas como leídas"
                        // Recargar notificaciones para actualizar la UI
                        loadNotificaciones()
                    } else {
                        _errorMessage.value = "Error al marcar todas las notificaciones como leídas"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error al marcar notificaciones: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun limpiarNotificacionesLeidas() {
        val userId = sessionManager.getUserIdAsString()
        if (userId != "-1") {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    val result = firestoreService.deleteNotificacionesLeidas(userId)
                    if (result.isSuccess) {
                        _operationResult.value = "Notificaciones leídas eliminadas"
                        // Recargar notificaciones para actualizar la UI
                        loadNotificaciones()
                    } else {
                        _errorMessage.value = "Error al eliminar notificaciones leídas"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error al limpiar notificaciones: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}