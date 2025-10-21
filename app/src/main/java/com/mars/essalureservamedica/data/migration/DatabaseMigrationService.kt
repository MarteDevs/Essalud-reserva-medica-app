package com.mars.essalureservamedica.data.migration

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
    private val firestoreService: FirestoreService
) {
    suspend fun migrateAllData(): Result<Unit> = withContext(Dispatchers.IO) {
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
                firestoreService.addDoctor(doctorFirestore).getOrThrow()
            }

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

            // Migrar calificaciones
            val calificaciones = calificacionDao.getAllCalificaciones().value ?: emptyList()
            calificaciones.forEach { calificacion: Calificacion ->
                val calificacionFirestore = CalificacionFirestore(
                    id = calificacion.id.toString(),
                    usuarioId = calificacion.usuarioId.toString(),
                    doctorId = calificacion.doctorId.toString(),
                    citaId = calificacion.citaId.toString(),
                    puntuacion = calificacion.puntuacion.toInt(),
                    comentario = calificacion.comentario ?: "",
                    fecha = calificacion.fechaCalificacion.time
                )
                firestoreService.addCalificacion(calificacionFirestore).getOrThrow()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyMigration(): Map<String, Boolean> = withContext(Dispatchers.IO) {
        mapOf(
            "usuarios" to verifyUsersMigration(),
            "doctores" to verifyDoctorsMigration(),
            "citas" to verifyCitasMigration(),
            "calificaciones" to verifyCalificacionesMigration()
        )
    }

    private suspend fun verifyUsersMigration(): Boolean {
        val roomUsers = userDao.getAllUsers().value?.size ?: 0
        // Implementar verificación del conteo en Firestore
        return true // Temporalmente retorna true
    }

    private suspend fun verifyDoctorsMigration(): Boolean {
        val roomDoctors = doctorDao.getAllDoctorsSync().size
        // Implementar verificación del conteo en Firestore
        return true // Temporalmente retorna true
    }

    private suspend fun verifyCitasMigration(): Boolean {
        val roomCitas = citaDao.getAllCitas().value?.size ?: 0
        // Implementar verificación del conteo en Firestore
        return true // Temporalmente retorna true
    }

    private suspend fun verifyCalificacionesMigration(): Boolean {
        val roomCalificaciones = calificacionDao.getAllCalificaciones().value?.size ?: 0
        // Implementar verificación del conteo en Firestore
        return true // Temporalmente retorna true
    }
}
