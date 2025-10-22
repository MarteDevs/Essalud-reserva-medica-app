package com.mars.essalureservamedica.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mars.essalureservamedica.data.firebase.FirebaseAuthService
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.UserFirestore
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionManager: SessionManager = SessionManager(application)
    private val firebaseAuthService: FirebaseAuthService = FirebaseAuthService()
    private val firestoreService: FirestoreService = FirestoreService()
    
    private val _authResult = MutableLiveData<AuthResult?>()
    val authResult: LiveData<AuthResult?> = _authResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun register(nombreCompleto: String, email: String, password: String) {
        if (!validateRegistrationInput(nombreCompleto, email, password)) {
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Verificar si el usuario ya existe en Firestore
                val existingUserResult = firestoreService.getUserByEmail(email)
                if (existingUserResult.isSuccess && existingUserResult.getOrNull() != null) {
                    _authResult.value = AuthResult.Error("El email ya está registrado")
                    _isLoading.value = false
                    return@launch
                }
                
                // Registro con Firebase Auth
                val firebaseResult = firebaseAuthService.signUpWithEmailPassword(email, password)
                
                if (firebaseResult.isSuccess) {
                    val firebaseUser = firebaseAuthService.getCurrentUser()
                    if (firebaseUser != null) {
                        // Crear usuario en Firestore
                        val userFirestore = UserFirestore(
                            id = firebaseUser.uid,
                            nombreCompleto = nombreCompleto,
                            email = email
                        )
                        
                        val firestoreResult = firestoreService.createUser(userFirestore)
                        if (firestoreResult.isSuccess) {
                            // Guardar sesión
                            sessionManager.saveUserSession(
                                firebaseUser.uid,
                                nombreCompleto,
                                email,
                                true
                            )
                            _authResult.value = AuthResult.Success("Registro exitoso")
                        } else {
                            // Si falla Firestore, eliminar usuario de Firebase Auth
                            firebaseAuthService.signOut()
                            _authResult.value = AuthResult.Error("Error al guardar datos del usuario")
                        }
                    } else {
                        _authResult.value = AuthResult.Error("Error en el registro")
                    }
                } else {
                    _authResult.value = AuthResult.Error("Error en el registro: ${firebaseResult.exceptionOrNull()?.message}")
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
                // Login con Firebase Auth
                val firebaseResult = firebaseAuthService.signInWithEmailPassword(email, password)
                
                if (firebaseResult.isSuccess) {
                    val firebaseUser = firebaseAuthService.getCurrentUser()
                    if (firebaseUser != null) {
                        // Obtener datos del usuario desde Firestore
                        val userResult = firestoreService.getUser(firebaseUser.uid)
                        if (userResult.isSuccess) {
                            val userFirestore = userResult.getOrNull()
                            if (userFirestore != null) {
                                // Guardar sesión con datos de Firebase
                                sessionManager.saveUserSession(
                                    firebaseUser.uid,
                                    userFirestore.nombreCompleto,
                                    userFirestore.email,
                                    true
                                )
                                _authResult.value = AuthResult.Success("Inicio de sesión exitoso")
                            } else {
                                _authResult.value = AuthResult.Error("Error al obtener datos del usuario")
                            }
                        } else {
                            _authResult.value = AuthResult.Error("Error al obtener datos del usuario")
                        }
                    } else {
                        _authResult.value = AuthResult.Error("Error en la autenticación")
                    }
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