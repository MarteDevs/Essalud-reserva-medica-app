package com.mars.essalureservamedica.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.database.AppDatabase
import com.mars.essalureservamedica.data.entity.User
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppRepository
    private val sessionManager: SessionManager = SessionManager(application)
    
    private val _authResult = MutableLiveData<AuthResult?>()
    val authResult: LiveData<AuthResult?> = _authResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        repository = AppRepository.getInstance(application)
    }
    
    fun register(nombreCompleto: String, email: String, password: String) {
        if (!validateRegistrationInput(nombreCompleto, email, password)) {
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Verificar si el email ya existe
                val existingUser = repository.getUserByEmail(email)
                if (existingUser != null) {
                    _authResult.value = AuthResult.Error("El correo electrónico ya está registrado")
                    _isLoading.value = false
                    return@launch
                }
                
                // Crear nuevo usuario
                val newUser = User(
                    nombreCompleto = nombreCompleto,
                    email = email,
                    password = password
                )
                
                val userId = repository.insertUser(newUser)
                if (userId > 0) {
                    // Guardar sesión
                    sessionManager.saveUserSession(userId.toInt(), nombreCompleto, email)
                    _authResult.value = AuthResult.Success("Registro exitoso")
                } else {
                    _authResult.value = AuthResult.Error("Error al registrar usuario")
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) {
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmailAndPassword(email, password)
                if (user != null) {
                    // Guardar sesión
                    sessionManager.saveUserSession(user.id, user.nombreCompleto, user.email)
                    _authResult.value = AuthResult.Success("Inicio de sesión exitoso")
                } else {
                    _authResult.value = AuthResult.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun validateRegistrationInput(
        nombreCompleto: String,
        email: String,
        password: String
    ): Boolean {
        when {
            nombreCompleto.isBlank() -> {
                _authResult.value = AuthResult.Error("El nombre completo es requerido")
                return false
            }
            email.isBlank() -> {
                _authResult.value = AuthResult.Error("El correo electrónico es requerido")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authResult.value = AuthResult.Error("Formato de correo electrónico inválido")
                return false
            }
            password.isBlank() -> {
                _authResult.value = AuthResult.Error("La contraseña es requerida")
                return false
            }
            password.length < 6 -> {
                _authResult.value = AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
                return false
            }
        }
        return true
    }
    
    private fun validateLoginInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _authResult.value = AuthResult.Error("El correo electrónico es requerido")
                return false
            }
            password.isBlank() -> {
                _authResult.value = AuthResult.Error("La contraseña es requerida")
                return false
            }
        }
        return true
    }
    
    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
    
    fun clearAuthResult() {
        _authResult.value = null
    }
}

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}