package com.mars.essalureservamedica.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.mars.essalureservamedica.data.firebase.FirebaseAuthService

/**
 * Administra los datos de la sesión del usuario, admitiendo tanto usuarios de Firebase como locales (Room/SharedPreferences).
 * Esta clase se encarga de guardar, recuperar y borrar la información de la sesión del usuario.
 * También proporciona métodos para migrar una sesión local a una sesión de Firebase.
 */
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
        private const val KEY_MIGRATION_COMPLETED = "migration_completed"
    }
    
    /**
     * Guarda los datos de la sesión del usuario en SharedPreferences.
     *
     * @param userId El ID único del usuario.
     * @param userName El nombre del usuario.
     * @param userEmail La dirección de correo electrónico del usuario.
     * @param isFirebaseUser Indicador de si el usuario es un usuario de Firebase. El valor predeterminado es verdadero.
     */
    fun saveUserSession(userId: String, userName: String, userEmail: String, isFirebaseUser: Boolean = true) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_EMAIL, userEmail)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_FIREBASE_USER, isFirebaseUser)
        editor.apply()
    }
    
    /**
     * Recupera el ID de usuario como un número entero.
     * Para los usuarios de Firebase, devuelve un hash del UID.
     * Para los usuarios locales, devuelve el ID de número entero almacenado.
     *
     * @return El ID de usuario como un Int, o -1 si no se encuentra.
     */
    fun getUserId(): Int {
        return if (isFirebaseUser()) {
            // Para usuarios de Firebase, usamos un hash del UID como ID entero
            firebaseAuth.currentUser?.uid?.hashCode()?.let { Math.abs(it) } ?: -1
        } else {
            prefs.getString(KEY_USER_ID, null)?.toIntOrNull() ?: -1
        }
    }

    /**
     * Recupera el ID de usuario como una cadena.
     * Para los usuarios de Firebase, devuelve el UID.
     * Para los usuarios locales, devuelve el ID almacenado.
     *
     * @return El ID de usuario como una cadena, o "-1" si no se encuentra.
     */
    fun getUserIdAsString(): String {
        return if (isFirebaseUser()) {
            // Para usuarios de Firebase, devolvemos el UID directamente
            firebaseAuth.currentUser?.uid ?: "-1"
        } else {
            // Para usuarios de Room, convertimos el ID a String
            prefs.getString(KEY_USER_ID, null) ?: "-1"
        }
    }

    /**
     * Recupera el ID de usuario de Firebase.
     *
     * @return El UID de Firebase como una cadena, o nulo si el usuario no es un usuario de Firebase.
     */
    fun getFirebaseUserId(): String? {
        return if (isFirebaseUser()) {
            firebaseAuth.currentUser?.uid
        } else {
            null
        }
    }
    
    /**
     * Recupera el nombre del usuario.
     * Prioriza el nombre de Firebase Auth si está disponible; de lo contrario, recurre a SharedPreferences.
     *
     * @return El nombre del usuario, o nulo si no se encuentra.
     */
    fun getUserName(): String? {
        // Priorizar Firebase Auth si está disponible
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return firebaseUser.displayName ?: prefs.getString(KEY_USER_NAME, null)
        }
        // Fallback a SharedPreferences
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Recupera el correo electrónico del usuario.
     * Prioriza el correo electrónico de Firebase Auth si está disponible; de lo contrario, recurre a SharedPreferences.
     *
     * @return El correo electrónico del usuario, o nulo si no se encuentra.
     */
    fun getUserEmail(): String? {
        // Priorizar Firebase Auth si está disponible
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return firebaseUser.email
        }
        // Fallback a SharedPreferences
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Comprueba si el usuario ha iniciado sesión actualmente.
     * Comprueba primero Firebase Auth y luego recurre a SharedPreferences.
     *
     * @return Verdadero si el usuario ha iniciado sesión, falso en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        // Verificar Firebase Auth primero
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && isFirebaseUser()) {
            return true
        }
        // Fallback a SharedPreferences
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Comprueba si el usuario actual es un usuario de Firebase.
     *
     * @return Verdadero si el usuario es un usuario de Firebase, falso en caso contrario.
     */
    fun isFirebaseUser(): Boolean {
        return prefs.getBoolean(KEY_IS_FIREBASE_USER, false)
    }
    
    /**
     * Borra la sesión de usuario actual.
     * Si el usuario es un usuario de Firebase, cierra la sesión de Firebase.
     * También borra todos los datos de SharedPreferences.
     */
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
    
    /**
     * Migra una sesión de usuario local (p. ej., Room) a una sesión de Firebase.
     * Guarda el nombre de usuario y el correo electrónico existentes con el nuevo ID de usuario de Firebase.
     *
     * @param firebaseUserId El nuevo ID de usuario de Firebase.
     */
    fun migrateToFirebaseSession(firebaseUserId: String) {
        val currentUserName = getUserName()
        val currentUserEmail = getUserEmail()
        
        if (currentUserName != null && currentUserEmail != null) {
            saveUserSession(firebaseUserId, currentUserName, currentUserEmail, true)
        }
    }

    /**
     * Comprueba si se ha completado la migración de la sesión a Firebase.
     *
     * @return Verdadero si la migración se ha completado, falso en caso contrario.
     */
    fun isMigrationCompleted(): Boolean = prefs.getBoolean(KEY_MIGRATION_COMPLETED, false)
    
    /**
     * Establece el estado de finalización de la migración.
     *
     * @param completed El estado de la migración.
     */
    fun setMigrationCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_MIGRATION_COMPLETED, completed).apply()
    }
}
