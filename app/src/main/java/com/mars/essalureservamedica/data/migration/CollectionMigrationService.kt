package com.mars.essalureservamedica.data.migration

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Servicio de un solo uso diseñado para migrar colecciones de Cloud Firestore de nombres en español a sus equivalentes en inglés.
 * Este servicio copia documentos de colecciones antiguas (p. ej., "citas") a colecciones nuevas (p. ej., "appointments").
 */
class CollectionMigrationService {
    // Obtiene la instancia de FirebaseFirestore para interactuar con la base de datos.
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Orquesta el proceso de migración completo.
     * Llama a los métodos de migración individuales para cada colección y consolida el resultado.
     * @return [Boolean] - Devuelve `true` si todas las migraciones se completaron con éxito, de lo contrario, `false`.
     */
    suspend fun migrateCollections(): Boolean {
        return try {
            Log.d("CollectionMigration", "Iniciando migración de colecciones...")
            
            // Paso 1: Migrar la colección "citas" a "appointments".
            val citasMigrated = migrateCitasToAppointments()
            Log.d("CollectionMigration", "Migración de citas: $citasMigrated")
            
            // Paso 2: Migrar la colección "calificaciones" a "ratings".
            val calificacionesMigrated = migrateCalificacionesToRatings()
            Log.d("CollectionMigration", "Migración de calificaciones: $calificacionesMigrated")
            
            // Paso 3: Migrar la colección "notificaciones" a "notifications".
            val notificacionesMigrated = migrateNotificacionesToNotifications()
            Log.d("CollectionMigration", "Migración de notificaciones: $notificacionesMigrated")
            
            // Comprueba si todas las migraciones individuales fueron exitosas.
            val allMigrated = citasMigrated && calificacionesMigrated && notificacionesMigrated
            Log.d("CollectionMigration", "Migración completada: $allMigrated")
            
            allMigrated
        } catch (e: Exception) {
            // Captura cualquier excepción inesperada durante el proceso de migración.
            Log.e("CollectionMigration", "Error durante la migración de colecciones", e)
            false
        }
    }
    
    /**
     * Migra todos los documentos de la colección "citas" a la nueva colección "appointments".
     * @return [Boolean] - `true` si la migración fue exitosa o no había nada que migrar, `false` si ocurrió un error.
     */
    private suspend fun migrateCitasToAppointments(): Boolean {
        return try {
            // Obtiene todos los documentos de la colección "citas".
            val citasSnapshot = db.collection("citas").get().await()
            
            // Si no hay documentos, la migración se considera exitosa.
            if (citasSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay citas para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${citasSnapshot.size()} citas...")
            
            // Itera sobre cada documento en la colección original.
            citasSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    // Copia el documento a la nueva colección "appointments" usando el mismo ID de documento.
                    db.collection("appointments")
                        .document(document.id)
                        .set(data)
                        .await()
                }
            }
            
            Log.d("CollectionMigration", "Citas migradas exitosamente")
            true
        } catch (e: Exception) {
            // Registra cualquier error que ocurra específicamente durante la migración de citas.
            Log.e("CollectionMigration", "Error migrando citas", e)
            false
        }
    }
    
    /**
     * Migra todos los documentos de la colección "calificaciones" a la nueva colección "ratings".
     * @return [Boolean] - `true` si la migración fue exitosa, `false` si ocurrió un error.
     */
    private suspend fun migrateCalificacionesToRatings(): Boolean {
        return try {
            // Obtiene todos los documentos de la colección "calificaciones".
            val calificacionesSnapshot = db.collection("calificaciones").get().await()
            
            if (calificacionesSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay calificaciones para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${calificacionesSnapshot.size()} calificaciones...")
            
            // Itera y copia cada documento a la nueva colección "ratings".
            calificacionesSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    db.collection("ratings")
                        .document(document.id)
                        .set(data)
                        .await()
                }
            }
            
            Log.d("CollectionMigration", "Calificaciones migradas exitosamente")
            true
        } catch (e: Exception) {
            Log.e("CollectionMigration", "Error migrando calificaciones", e)
            false
        }
    }
    
    /**
     * Migra todos los documentos de la colección "notificaciones" a la nueva colección "notifications".
     * @return [Boolean] - `true` si la migración fue exitosa, `false` si ocurrió un error.
     */
    private suspend fun migrateNotificacionesToNotifications(): Boolean {
        return try {
            // Obtiene todos los documentos de la colección "notificaciones".
            val notificacionesSnapshot = db.collection("notificaciones").get().await()
            
            if (notificacionesSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay notificaciones para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${notificacionesSnapshot.size()} notificaciones...")
            
            // Itera y copia cada documento a la nueva colección "notifications".
            notificacionesSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    db.collection("notifications")
                        .document(document.id)
                        .set(data)
                        .await()
                }
            }
            
            Log.d("CollectionMigration", "Notificaciones migradas exitosamente")
            true
        } catch (e: Exception) {
            Log.e("CollectionMigration", "Error migrando notificaciones", e)
            false
        }
    }
    
    /**
     * Verifica si la migración se realizó correctamente comparando el número de documentos
     * en las colecciones antiguas y nuevas.
     * @return [Map<String, Boolean>] - Un mapa donde cada clave es el nombre de una nueva colección
     * y el valor es `true` si el recuento de documentos coincide con su antigua contraparte.
     */
    suspend fun verifyMigration(): Map<String, Boolean> {
        return try {
            // Obtiene el recuento de documentos de la colección de citas original y la nueva.
            val citasCount = db.collection("citas").get().await().size()
            val appointmentsCount = db.collection("appointments").get().await().size()
            
            // Obtiene el recuento de documentos de la colección de calificaciones original y la nueva.
            val calificacionesCount = db.collection("calificaciones").get().await().size()
            val ratingsCount = db.collection("ratings").get().await().size()
            
            // Obtiene el recuento de documentos de la colección de notificaciones original y la nueva.
            val notificacionesCount = db.collection("notificaciones").get().await().size()
            val notificationsCount = db.collection("notifications").get().await().size()
            
            // Devuelve un mapa que resume el resultado de la verificación para cada colección.
            mapOf(
                "appointments" to (citasCount == appointmentsCount),
                "ratings" to (calificacionesCount == ratingsCount),
                "notifications" to (notificacionesCount == notificationsCount)
            )
        } catch (e: Exception) {
            // Si ocurre un error durante la verificación, se asume que la migración falló.
            Log.e("CollectionMigration", "Error verificando migración", e)
            mapOf(
                "appointments" to false,
                "ratings" to false,
                "notifications" to false
            )
        }
    }
}
