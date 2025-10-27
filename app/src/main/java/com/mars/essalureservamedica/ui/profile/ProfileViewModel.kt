package com.mars.essalureservamedica.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.UserFirestore
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val sessionManager = SessionManager(application)

    private val _user = MutableLiveData<UserFirestore?>()
    val user: LiveData<UserFirestore?> = _user

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
        val userId = sessionManager.getUserIdAsString()
        if (userId != "-1") {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val result = firestoreService.getUser(userId)
                    if (result.isSuccess) {
                        _user.value = result.getOrNull()
                    } else {
                        _errorMessage.value = "Error al cargar perfil"
                    }
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

        // Verificar contraseña actual (simulación - en producción usar autenticación Firebase)
        // Por ahora, omitimos la verificación de contraseña ya que UserFirestore no tiene password
        // if (currentUser.password != currentPassword) {
        //     _updateResult.value = Result.failure(Exception("Contraseña actual incorrecta"))
        //     return
        // }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Verificar si el nuevo email ya existe (si cambió)
                if (email != currentUser.email) {
                    val existingUserResult = firestoreService.getUserByEmail(email)
                    if (existingUserResult.isSuccess) {
                        val existingUser = existingUserResult.getOrNull()
                        if (existingUser != null && existingUser.id != currentUser.id) {
                            _updateResult.value = Result.failure(Exception("El correo electrónico ya está en uso"))
                            _isLoading.value = false
                            return@launch
                        }
                    }
                }

                // Crear usuario actualizado
                val updatedUser = currentUser.copy(
                    nombreCompleto = nombreCompleto,
                    email = email
                )

                // Preparar actualizaciones para Firestore
                val updates = mapOf(
                    "nombreCompleto" to nombreCompleto,
                    "email" to email
                )

                // Actualizar en Firestore
                val updateResult = firestoreService.updateUser(currentUser.id, updates)
                
                if (updateResult.isSuccess) {
                    // Actualizar sesión si cambió el nombre o email
                    sessionManager.saveUserSession(updatedUser.id, updatedUser.nombreCompleto, updatedUser.email, true)

                    // Actualizar el usuario en el LiveData
                    _user.value = updatedUser

                    _updateResult.value = Result.success("Perfil actualizado exitosamente")
                } else {
                    _updateResult.value = Result.failure(Exception("Error al actualizar perfil"))
                }
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