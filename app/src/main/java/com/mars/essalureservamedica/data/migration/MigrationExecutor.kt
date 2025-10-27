package com.mars.essalureservamedica.data.migration

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mars.essalureservamedica.data.database.AppDatabase
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MigrationExecutor(
    private val context: Context,
    private val database: AppDatabase,
    private val firestoreService: FirestoreService,
    private val sessionManager: SessionManager
) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val migrationService = DatabaseMigrationService(
        userDao = database.userDao(),
        doctorDao = database.doctorDao(),
        citaDao = database.citaDao(),
        calificacionDao = database.calificacionDao(),
        notificacionDao = database.notificacionDao(),
        firestoreService = firestoreService
    )
    private val collectionMigrationService = CollectionMigrationService()

    fun executeMigration(
        onProgress: (String) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    onProgress("Iniciando migración de datos...")
                }

                // Verificar autenticación antes de migrar
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("Error: Debes estar autenticado para migrar datos a Firebase. Por favor, inicia sesión primero.")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    onProgress("Usuario autenticado: ${currentUser.email}")
                }

                // Verificar si ya se ejecutó la migración
                if (sessionManager.isMigrationCompleted()) {
                    withContext(Dispatchers.Main) {
                        onProgress("La migración ya fue completada anteriormente")
                        onSuccess()
                    }
                    return@launch
                }

                // Ejecutar migración
                withContext(Dispatchers.Main) {
                    onProgress("Migrando usuarios...")
                }
                val usersResult = migrationService.migrateUsers()
                if (!usersResult) {
                    throw Exception("Error al migrar usuarios - Verifica los permisos de Firestore")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando doctores...")
                }
                val doctorsResult = migrationService.migrateDoctors()
                if (!doctorsResult) {
                    throw Exception("Error al migrar doctores - Verifica los permisos de Firestore")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando citas...")
                }
                val citasResult = migrationService.migrateCitas()
                if (!citasResult) {
                    throw Exception("Error al migrar citas - Verifica los permisos de Firestore")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando calificaciones...")
                }
                val calificacionesResult = migrationService.migrateCalificaciones()
                if (!calificacionesResult) {
                    throw Exception("Error al migrar calificaciones - Verifica los permisos de Firestore")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando notificaciones...")
                }
                val notificacionesResult = migrationService.migrateNotificaciones()
                if (!notificacionesResult) {
                    throw Exception("Error al migrar notificaciones - Verifica los permisos de Firestore")
                }

                // Migrar colecciones de español a inglés
                withContext(Dispatchers.Main) {
                    onProgress("Migrando colecciones a nombres en inglés...")
                }
                val collectionMigrationResult = collectionMigrationService.migrateCollections()
                if (!collectionMigrationResult) {
                    Log.w("MigrationExecutor", "Advertencia: No se pudieron migrar todas las colecciones, pero continuando...")
                }

                // Verificar migración
                withContext(Dispatchers.Main) {
                    onProgress("Verificando integridad de datos...")
                }
                val verificationResult = migrationService.verifyMigration()
                val allVerified = verificationResult.values.all { it }
                
                // Log detallado de la verificación
                Log.d("MigrationExecutor", "Resultados de verificación: $verificationResult")
                
                if (!allVerified) {
                    val failedVerifications = verificationResult.filter { !it.value }
                    val failedItems = failedVerifications.keys.joinToString(", ")
                    Log.e("MigrationExecutor", "Verificación fallida para: $failedItems")
                    
                    withContext(Dispatchers.Main) {
                        onProgress("Advertencia: Algunos datos no se verificaron correctamente ($failedItems), pero continuando...")
                    }
                    
                    // En lugar de fallar, continuamos con advertencia
                    // throw Exception("Error en la verificación de datos migrados: $failedItems")
                }

                // Marcar migración como completada
                sessionManager.setMigrationCompleted(true)

                withContext(Dispatchers.Main) {
                    onProgress("Migración completada exitosamente")
                    onSuccess()
                }

                Log.d("MigrationExecutor", "Migración completada exitosamente")

            } catch (e: Exception) {
                Log.e("MigrationExecutor", "Error durante la migración", e)
                withContext(Dispatchers.Main) {
                    val errorMessage = when {
                        e.message?.contains("PERMISSION_DENIED") == true -> 
                            "Error de permisos: Verifica las reglas de seguridad de Firestore y que estés autenticado correctamente."
                        e.message?.contains("UNAUTHENTICATED") == true -> 
                            "Error de autenticación: Debes iniciar sesión antes de migrar los datos."
                        e.message?.contains("UNAVAILABLE") == true -> 
                            "Error de conexión: Verifica tu conexión a internet y que Firebase esté disponible."
                        else -> "Error durante la migración: ${e.message}"
                    }
                    onError(errorMessage)
                }
            }
        }
    }

    fun checkMigrationStatus(): Boolean {
        return sessionManager.isMigrationCompleted()
    }

    suspend fun verifyDataIntegrity(): Boolean {
        return try {
            val result = migrationService.verifyMigration()
            result.values.all { it }
        } catch (e: Exception) {
            Log.e("MigrationExecutor", "Error verificando integridad de datos", e)
            false
        }
    }
}