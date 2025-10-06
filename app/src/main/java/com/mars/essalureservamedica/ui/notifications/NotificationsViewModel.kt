package com.mars.essalureservamedica.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.entity.Notificacion
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notificaciones = MutableLiveData<List<Notificacion>>()
    val notificaciones: LiveData<List<Notificacion>> = _notificaciones

    private val _notificacionesNoLeidas = MutableLiveData<List<Notificacion>>()
    val notificacionesNoLeidas: LiveData<List<Notificacion>> = _notificacionesNoLeidas

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
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            // Observar todas las notificaciones
            repository.getNotificacionesByUserId(userId).observeForever { notificaciones ->
                _notificaciones.value = notificaciones
            }

            // Observar notificaciones no leídas
            repository.getNotificacionesNoLeidasByUserId(userId).observeForever { noLeidas ->
                _notificacionesNoLeidas.value = noLeidas
            }

            // Observar contador de no leídas
            repository.getCountNotificacionesNoLeidas(userId).observeForever { count ->
                _countNoLeidas.value = count
            }
        }
    }

    fun marcarComoLeida(notificacionId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.marcarNotificacionComoLeida(notificacionId)
                _operationResult.value = "Notificación marcada como leída"
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar notificación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun marcarTodasComoLeidas() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    repository.marcarTodasNotificacionesComoLeidas(userId)
                    _operationResult.value = "Todas las notificaciones marcadas como leídas"
                } catch (e: Exception) {
                    _errorMessage.value = "Error al marcar notificaciones: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun limpiarNotificacionesLeidas() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    repository.deleteNotificacionesLeidas(userId)
                    _operationResult.value = "Notificaciones leídas eliminadas"
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