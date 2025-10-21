package com.mars.essalureservamedica.data.migration

import android.content.Context
import android.util.Log
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
    private val migrationService = DatabaseMigrationService(
        userDao = database.userDao(),
        doctorDao = database.doctorDao(),
        citaDao = database.citaDao(),
        calificacionDao = database.calificacionDao(),
        notificacionDao = database.notificacionDao(),
        firestoreService = firestoreService
    )

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
                    throw Exception("Error al migrar usuarios")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando doctores...")
                }
                val doctorsResult = migrationService.migrateDoctors()
                if (!doctorsResult) {
                    throw Exception("Error al migrar doctores")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando citas...")
                }
                val citasResult = migrationService.migrateCitas()
                if (!citasResult) {
                    throw Exception("Error al migrar citas")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando calificaciones...")
                }
                val calificacionesResult = migrationService.migrateCalificaciones()
                if (!calificacionesResult) {
                    throw Exception("Error al migrar calificaciones")
                }

                withContext(Dispatchers.Main) {
                    onProgress("Migrando notificaciones...")
                }
                val notificacionesResult = migrationService.migrateNotificaciones()
                if (!notificacionesResult) {
                    throw Exception("Error al migrar notificaciones")
                }

                // Verificar migración
                withContext(Dispatchers.Main) {
                    onProgress("Verificando integridad de datos...")
                }
                val verificationResult = migrationService.verifyMigration()
                val allVerified = verificationResult.values.all { it }
                if (!allVerified) {
                    throw Exception("Error en la verificación de datos migrados")
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
                    onError("Error durante la migración: ${e.message}")
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