package com.mars.essalureservamedica.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.mars.essalureservamedica.data.firebase.FirebaseAuthService

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthService = FirebaseAuthService()
    
    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_FIREBASE_USER = "is_firebase_user"
    }
    
    fun saveUserSession(userId: String, userName: String, userEmail: String, isFirebaseUser: Boolean = true) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_EMAIL, userEmail)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_FIREBASE_USER, isFirebaseUser)
        editor.apply()
    }
    
    fun getUserId(): Int {
        return if (isFirebaseUser()) {
            // Para usuarios de Firebase, usamos un hash del UID como ID entero
            firebaseAuth.currentUser?.uid?.hashCode()?.let { Math.abs(it) } ?: -1
        } else {
            prefs.getString(KEY_USER_ID, null)?.toIntOrNull() ?: -1
        }
    }

    fun getFirebaseUserId(): String? {
        return if (isFirebaseUser()) {
            firebaseAuth.currentUser?.uid
        } else {
            null
        }
    }
    
    fun getUserName(): String? {
        // Priorizar Firebase Auth si está disponible
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return firebaseUser.displayName ?: prefs.getString(KEY_USER_NAME, null)
        }
        // Fallback a SharedPreferences
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    fun getUserEmail(): String? {
        // Priorizar Firebase Auth si está disponible
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return firebaseUser.email
        }
        // Fallback a SharedPreferences
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun isLoggedIn(): Boolean {
        // Verificar Firebase Auth primero
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return true
        }
        // Fallback a SharedPreferences
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun isFirebaseUser(): Boolean {
        return prefs.getBoolean(KEY_IS_FIREBASE_USER, false)
    }
    
    fun clearSession() {
        // Cerrar sesión de Firebase si es usuario de Firebase
        if (isFirebaseUser()) {
            firebaseAuthService.signOut()
        }
        
        // Limpiar SharedPreferences
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
    
    // Método para migrar sesión de Room a Firebase
    fun migrateToFirebaseSession(firebaseUserId: String) {
        val currentUserName = getUserName()
        val currentUserEmail = getUserEmail()
        
        if (currentUserName != null && currentUserEmail != null) {
            saveUserSession(firebaseUserId, currentUserName, currentUserEmail, true)
        }
    }
}