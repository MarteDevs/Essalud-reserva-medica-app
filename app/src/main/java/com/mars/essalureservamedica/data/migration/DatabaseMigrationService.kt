package com.mars.essalureservamedica.data.migration

import android.util.Log
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.firebase.models.*
import com.mars.essalureservamedica.data.dao.*
import com.mars.essalureservamedica.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class DatabaseMigrationService @Inject constructor(
    private val userDao: UserDao,
    private val doctorDao: DoctorDao,
    private val citaDao: CitaDao,
    private val calificacionDao: CalificacionDao,
    private val notificacionDao: NotificacionDao,
    private val firestoreService: FirestoreService
) {
    suspend fun migrateAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            migrateUsers()
            migrateDoctors()
            migrateCitas()
            migrateCalificaciones()
            migrateNotificaciones()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun migrateUsers(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Migrar usuarios
            val users = userDao.getAllUsers().value ?: emptyList()
            users.forEach { user: User ->
                val userFirestore = UserFirestore(
                    id = user.id.toString(),
                    nombreCompleto = user.nombreCompleto,
                    email = user.email,
                    createdAt = System.currentTimeMillis()
                )
                firestoreService.createUser(userFirestore).getOrThrow()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun migrateDoctors(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Migrar doctores
            val doctors = doctorDao.getAllDoctorsSync()
            doctors.forEach { doctor: Doctor ->
                val doctorFirestore = DoctorFirestore(
                    id = doctor.id.toString(),
                    nombre = doctor.nombre,
                    especialidad = doctor.especialidad,
                    experiencia = doctor.experiencia,
                    disponibilidad = doctor.disponibilidad,
                    foto = doctor.foto ?: ""
                )
               // firestoreService.addDoctor(doctorFirestore).getOrThrow()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun migrateCitas(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Migrar citas
            val citas = citaDao.getAllCitas().value ?: emptyList()
            citas.forEach { cita: Cita ->
                val citaFirestore = CitaFirestore(
                    id = cita.id.toString(),
                    usuarioId = cita.usuarioId.toString(),
                    doctorId = cita.doctorId.toString(),
                    fecha = cita.fechaHora.time,
                    hora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(cita.fechaHora),
                    estado = cita.estado,
                    motivo = cita.notas ?: "",
                    createdAt = System.currentTimeMillis()
                )
                firestoreService.createCita(citaFirestore).getOrThrow()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun migrateCalificaciones(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Migrar calificaciones
            val calificaciones = calificacionDao.getAllCalificacionesSync()
            calificaciones.forEach { calificacion: Calificacion ->
                val calificacionFirestore = CalificacionFirestore(
                    id = calificacion.id.toString(),
                    doctorId = calificacion.doctorId.toString(),
                    usuarioId = calificacion.usuarioId.toString(),
                    citaId = calificacion.citaId.toString(),
                    puntuacion = calificacion.puntuacion.toInt(),
                    comentario = calificacion.comentario ?: "",
                    fecha = calificacion.fechaCalificacion.time
                )
                firestoreService.addCalificacion(calificacionFirestore).getOrThrow()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun migrateNotificaciones(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Migrar notificaciones
            val notificaciones = notificacionDao.getAllNotificacionesSync()
            notificaciones.forEach { notificacion: Notificacion ->
                val notificacionFirestore = NotificacionFirestore(
                    id = notificacion.id.toString(),
                    usuarioId = notificacion.usuarioId.toString(),
                    titulo = notificacion.titulo,
                    mensaje = notificacion.mensaje,
                    tipo = notificacion.tipo,
                    leida = notificacion.leida,
                    fechaCreacion = notificacion.fechaCreacion.time,
                    citaId = notificacion.citaId?.toString()
                )
                firestoreService.createNotificacion(notificacionFirestore).getOrThrow()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyMigration(): Map<String, Boolean> = withContext(Dispatchers.IO) {
        mapOf(
            "usuarios" to verifyUsersMigration(),
            "doctores" to verifyDoctorsMigration(),
            "appointments" to verifyCitasMigration(),
            "ratings" to verifyCalificacionesMigration(),
            "notifications" to verifyNotificacionesMigration()
        )
    }

    private suspend fun verifyUsersMigration(): Boolean {
        return try {
            val roomUsers = userDao.getAllUsers().value?.size ?: 0
            val firestoreResult = firestoreService.getAllUsers()
            val firestoreUsers = firestoreResult.getOrNull()?.size ?: 0
            
            Log.d("DatabaseMigrationService", "Verificación usuarios - Room: $roomUsers, Firestore: $firestoreUsers")
            
            // Si no hay usuarios en Room, consideramos la migración exitosa
            if (roomUsers == 0) return true
            
            roomUsers == firestoreUsers
        } catch (e: Exception) {
            Log.e("DatabaseMigrationService", "Error verificando usuarios", e)
            false
        }
    }

    private suspend fun verifyDoctorsMigration(): Boolean {
        return try {
            val roomDoctors = doctorDao.getAllDoctorsSync().size
            val firestoreResult = firestoreService.getAllDoctors()
            val firestoreDoctors = firestoreResult.getOrNull()?.size ?: 0
            
            Log.d("DatabaseMigrationService", "Verificación doctores - Room: $roomDoctors, Firestore: $firestoreDoctors")
            
            // Si no hay doctores en Room, consideramos la migración exitosa
            if (roomDoctors == 0) return true
            
            roomDoctors == firestoreDoctors
        } catch (e: Exception) {
            Log.e("DatabaseMigrationService", "Error verificando doctores", e)
            false
        }
    }

    private suspend fun verifyCitasMigration(): Boolean {
        return try {
            val roomCitas = citaDao.getAllCitas().value?.size ?: 0
            val firestoreResult = firestoreService.getAllCitas()
            val firestoreCitas = firestoreResult.getOrNull()?.size ?: 0
            
            Log.d("DatabaseMigrationService", "Verificación citas - Room: $roomCitas, Firestore: $firestoreCitas")
            
            // Si no hay citas en Room, consideramos la migración exitosa
            if (roomCitas == 0) return true
            
            roomCitas == firestoreCitas
        } catch (e: Exception) {
            Log.e("DatabaseMigrationService", "Error verificando citas", e)
            false
        }
    }

    private suspend fun verifyCalificacionesMigration(): Boolean {
        return try {
            val roomCalificaciones = calificacionDao.getAllCalificacionesSync().size
            val firestoreResult = firestoreService.getAllCalificaciones()
            val firestoreCalificaciones = firestoreResult.getOrNull()?.size ?: 0
            
            Log.d("DatabaseMigrationService", "Verificación calificaciones - Room: $roomCalificaciones, Firestore: $firestoreCalificaciones")
            
            // Si no hay calificaciones en Room, consideramos la migración exitosa
            if (roomCalificaciones == 0) return true
            
            roomCalificaciones == firestoreCalificaciones
        } catch (e: Exception) {
            Log.e("DatabaseMigrationService", "Error verificando calificaciones", e)
            false
        }
    }

    private suspend fun verifyNotificacionesMigration(): Boolean {
        return try {
            val roomNotificaciones = notificacionDao.getAllNotificacionesSync().size
            val firestoreResult = firestoreService.getAllNotificaciones()
            val firestoreNotificaciones = firestoreResult.getOrNull()?.size ?: 0
            
            Log.d("DatabaseMigrationService", "Verificación notificaciones - Room: $roomNotificaciones, Firestore: $firestoreNotificaciones")
            
            // Si no hay notificaciones en Room, consideramos la migración exitosa
            if (roomNotificaciones == 0) return true
            
            roomNotificaciones == firestoreNotificaciones
        } catch (e: Exception) {
            Log.e("DatabaseMigrationService", "Error verificando notificaciones", e)
            false
        }
    }
}
