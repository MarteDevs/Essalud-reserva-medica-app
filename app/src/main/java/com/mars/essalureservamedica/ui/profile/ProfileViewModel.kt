package com.mars.essalureservamedica.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.entity.User
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateResult = MutableLiveData<Result<String>?>()
    val updateResult: LiveData<Result<String>?> = _updateResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val user = repository.getUserById(userId)
                    _user.value = user
                } catch (e: Exception) {
                    _errorMessage.value = "Error al cargar perfil: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateProfile(nombreCompleto: String, email: String, currentPassword: String, newPassword: String?) {
        if (!validateProfileInput(nombreCompleto, email, currentPassword)) {
            return
        }

        val currentUser = _user.value
        if (currentUser == null) {
            _updateResult.value = Result.failure(Exception("Usuario no encontrado"))
            return
        }

        // Verificar contraseña actual
        if (currentUser.password != currentPassword) {
            _updateResult.value = Result.failure(Exception("Contraseña actual incorrecta"))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Verificar si el nuevo email ya existe (si cambió)
                if (email != currentUser.email) {
                    val existingUser = repository.getUserByEmail(email)
                    if (existingUser != null && existingUser.id != currentUser.id) {
                        _updateResult.value = Result.failure(Exception("El correo electrónico ya está en uso"))
                        _isLoading.value = false
                        return@launch
                    }
                }

                // Crear usuario actualizado
                val updatedUser = currentUser.copy(
                    nombreCompleto = nombreCompleto,
                    email = email,
                    password = newPassword ?: currentUser.password
                )

                // Actualizar en base de datos
                repository.updateUser(updatedUser)

                // Actualizar sesión si cambió el nombre o email
                sessionManager.saveUserSession(updatedUser.id.toString(), updatedUser.nombreCompleto, updatedUser.email, false)

                // Actualizar el usuario en el LiveData
                _user.value = updatedUser

                _updateResult.value = Result.success("Perfil actualizado exitosamente")
            } catch (e: Exception) {
                _updateResult.value = Result.failure(Exception("Error al actualizar perfil: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateProfileInput(nombreCompleto: String, email: String, currentPassword: String): Boolean {
        if (nombreCompleto.isBlank()) {
            _updateResult.value = Result.failure(Exception("El nombre completo es requerido"))
            return false
        }

        if (email.isBlank()) {
            _updateResult.value = Result.failure(Exception("El email es requerido"))
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _updateResult.value = Result.failure(Exception("Email inválido"))
            return false
        }

        if (currentPassword.isBlank()) {
            _updateResult.value = Result.failure(Exception("La contraseña actual es requerida"))
            return false
        }

        return true
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}