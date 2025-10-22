package com.mars.essalureservamedica.data.migration

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Servicio para migrar datos de colecciones con nombres en español a inglés
 */
class CollectionMigrationService {
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Migra todos los datos de las colecciones españolas a las inglesas
     */
    suspend fun migrateCollections(): Boolean {
        return try {
            Log.d("CollectionMigration", "Iniciando migración de colecciones...")
            
            // Migrar citas -> appointments
            val citasMigrated = migrateCitasToAppointments()
            Log.d("CollectionMigration", "Migración de citas: $citasMigrated")
            
            // Migrar calificaciones -> ratings
            val calificacionesMigrated = migrateCalificacionesToRatings()
            Log.d("CollectionMigration", "Migración de calificaciones: $calificacionesMigrated")
            
            // Migrar notificaciones -> notifications
            val notificacionesMigrated = migrateNotificacionesToNotifications()
            Log.d("CollectionMigration", "Migración de notificaciones: $notificacionesMigrated")
            
            val allMigrated = citasMigrated && calificacionesMigrated && notificacionesMigrated
            Log.d("CollectionMigration", "Migración completada: $allMigrated")
            
            allMigrated
        } catch (e: Exception) {
            Log.e("CollectionMigration", "Error durante la migración de colecciones", e)
            false
        }
    }
    
    /**
     * Migra datos de "citas" a "appointments"
     */
    private suspend fun migrateCitasToAppointments(): Boolean {
        return try {
            val citasSnapshot = db.collection("citas").get().await()
            
            if (citasSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay citas para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${citasSnapshot.size()} citas...")
            
            citasSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    // Copiar documento a la nueva colección
                    db.collection("appointments")
                        .document(document.id)
                        .set(data)
                        .await()
                }
            }
            
            Log.d("CollectionMigration", "Citas migradas exitosamente")
            true
        } catch (e: Exception) {
            Log.e("CollectionMigration", "Error migrando citas", e)
            false
        }
    }
    
    /**
     * Migra datos de "calificaciones" a "ratings"
     */
    private suspend fun migrateCalificacionesToRatings(): Boolean {
        return try {
            val calificacionesSnapshot = db.collection("calificaciones").get().await()
            
            if (calificacionesSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay calificaciones para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${calificacionesSnapshot.size()} calificaciones...")
            
            calificacionesSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    // Copiar documento a la nueva colección
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
     * Migra datos de "notificaciones" a "notifications"
     */
    private suspend fun migrateNotificacionesToNotifications(): Boolean {
        return try {
            val notificacionesSnapshot = db.collection("notificaciones").get().await()
            
            if (notificacionesSnapshot.isEmpty) {
                Log.d("CollectionMigration", "No hay notificaciones para migrar")
                return true
            }
            
            Log.d("CollectionMigration", "Migrando ${notificacionesSnapshot.size()} notificaciones...")
            
            notificacionesSnapshot.documents.forEach { document ->
                val data = document.data
                if (data != null) {
                    // Copiar documento a la nueva colección
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
     * Verifica que los datos se hayan migrado correctamente
     */
    suspend fun verifyMigration(): Map<String, Boolean> {
        return try {
            val citasCount = db.collection("citas").get().await().size()
            val appointmentsCount = db.collection("appointments").get().await().size()
            
            val calificacionesCount = db.collection("calificaciones").get().await().size()
            val ratingsCount = db.collection("ratings").get().await().size()
            
            val notificacionesCount = db.collection("notificaciones").get().await().size()
            val notificationsCount = db.collection("notifications").get().await().size()
            
            mapOf(
                "appointments" to (citasCount == appointmentsCount),
                "ratings" to (calificacionesCount == ratingsCount),
                "notifications" to (notificacionesCount == notificationsCount)
            )
        } catch (e: Exception) {
            Log.e("CollectionMigration", "Error verificando migración", e)
            mapOf(
                "appointments" to false,
                "ratings" to false,
                "notifications" to false
            )
        }
    }
}