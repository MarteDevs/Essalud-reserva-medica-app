package com.mars.essalureservamedica.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mars.essalureservamedica.data.firebase.models.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    // Colecciones
    private val usersCollection = db.collection("users")
    private val doctorsCollection = db.collection("doctors")
    private val citasCollection = db.collection("citas")
    private val calificacionesCollection = db.collection("calificaciones")
    private val notificacionesCollection = db.collection("notificaciones")

    // Usuarios
    suspend fun createUser(user: UserFirestore): Result<String> = try {
        val docRef = usersCollection.document(user.id)
        docRef.set(user.toMap()).await()
        Result.success(user.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(userId: String): Result<UserFirestore?> = try {
        val document = usersCollection.document(userId).get().await()
        if (document.exists()) {
            val user = UserFirestore(
                id = document.id,
                nombreCompleto = document.getString("nombreCompleto") ?: "",
                email = document.getString("email") ?: "",
                createdAt = document.getLong("createdAt") ?: 0L
            )
            Result.success(user)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllUsers(): Result<List<UserFirestore>> = try {
        val snapshot = usersCollection.get().await()
        val users = snapshot.documents.mapNotNull { doc ->
            UserFirestore(
                id = doc.id,
                nombreCompleto = doc.getString("nombreCompleto") ?: "",
                email = doc.getString("email") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserByEmail(email: String): Result<UserFirestore?> = try {
        val snapshot = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()
        
        val user = snapshot.documents.firstOrNull()?.let { doc ->
            UserFirestore(
                id = doc.id,
                nombreCompleto = doc.getString("nombreCompleto") ?: "",
                email = doc.getString("email") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersCollection.document(userId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Doctores
    fun getDoctorsFlow(): Flow<List<DoctorFirestore>> = callbackFlow {
        val subscription = doctorsCollection
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val doctors = snapshot?.documents?.mapNotNull {
                    it.toObject(DoctorFirestore::class.java)
                } ?: emptyList()

                trySend(doctors)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addDoctor(doctor: DoctorFirestore): Result<String> = try {
        val docRef = doctorsCollection.document()
        val doctorWithId = doctor.copy(id = docRef.id)
        docRef.set(doctorWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllDoctors(): Result<List<DoctorFirestore>> = try {
        val snapshot = doctorsCollection.get().await()
        val doctors = snapshot.documents.mapNotNull { doc ->
            doc.toObject(DoctorFirestore::class.java)
        }
        Result.success(doctors)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDoctor(doctorId: String): Result<DoctorFirestore?> = try {
        val document = doctorsCollection.document(doctorId).get().await()
        if (document.exists()) {
            val doctor = document.toObject(DoctorFirestore::class.java)
            Result.success(doctor)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Citas
    fun getUserCitasFlow(userId: String): Flow<List<CitaFirestore>> = callbackFlow {
        val subscription = citasCollection
            .whereEqualTo("usuarioId", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val citas = snapshot?.documents?.mapNotNull {
                    it.toObject(CitaFirestore::class.java)
                } ?: emptyList()

                trySend(citas)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createCita(cita: CitaFirestore): Result<String> = try {
        val docRef = citasCollection.document()
        val citaWithId = cita.copy(id = docRef.id)
        docRef.set(citaWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCitaStatus(citaId: String, newStatus: String): Result<Unit> = try {
        citasCollection.document(citaId)
            .update("estado", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCitaEstado(citaId: String, newStatus: String): Result<Unit> = try {
        citasCollection.document(citaId)
            .update("estado", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCita(citaId: String, updates: Map<String, Any>): Result<Unit> = try {
        citasCollection.document(citaId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllCitas(): Result<List<CitaFirestore>> = try {
        val snapshot = citasCollection.get().await()
        val citas = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CitaFirestore::class.java)
        }
        Result.success(citas)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCitasByDoctorAndDate(doctorId: String, fechaTimestamp: Long): Result<List<CitaFirestore>> = try {
        val snapshot = citasCollection
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("fecha", fechaTimestamp)
            .get()
            .await()
        
        val citas = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CitaFirestore::class.java)
        }
        Result.success(citas)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Calificaciones
    suspend fun addCalificacion(calificacion: CalificacionFirestore): Result<String> = try {
        val docRef = calificacionesCollection.document()
        val calificacionWithId = calificacion.copy(id = docRef.id)
        docRef.set(calificacionWithId.toMap()).await()

        // Actualizar rating del doctor
        updateDoctorRating(calificacion.doctorId)

        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun updateDoctorRating(doctorId: String) {
        try {
            val calificaciones = calificacionesCollection
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()

            val ratings = calificaciones.documents.mapNotNull { it.getLong("puntuacion")?.toInt() }
            if (ratings.isNotEmpty()) {
                val avgRating = ratings.average()
                doctorsCollection.document(doctorId)
                    .update(
                        mapOf(
                            "rating" to avgRating,
                            "totalRatings" to ratings.size
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            // Manejar error de actualización de rating
        }
    }

    suspend fun getAllCalificaciones(): Result<List<CalificacionFirestore>> = try {
        val snapshot = calificacionesCollection.get().await()
        val calificaciones = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CalificacionFirestore::class.java)
        }
        Result.success(calificaciones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCalificacionByCitaId(citaId: String): Result<CalificacionFirestore?> = try {
        val snapshot = calificacionesCollection
            .whereEqualTo("citaId", citaId)
            .get()
            .await()
        
        val calificacion = snapshot.documents.firstOrNull()?.toObject(CalificacionFirestore::class.java)
        Result.success(calificacion)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Notificaciones
    suspend fun createNotificacion(notificacion: NotificacionFirestore): Result<String> = try {
        val docRef = notificacionesCollection.document()
        val notificacionWithId = notificacion.copy(id = docRef.id)
        docRef.set(notificacionWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getNotificacionesByUserId(usuarioId: String): Result<List<NotificacionFirestore>> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .await()

        val notificaciones = snapshot.documents.mapNotNull { doc ->
            NotificacionFirestore(
                id = doc.id,
                usuarioId = doc.getString("usuarioId") ?: "",
                titulo = doc.getString("titulo") ?: "",
                mensaje = doc.getString("mensaje") ?: "",
                tipo = doc.getString("tipo") ?: "",
                leida = doc.getBoolean("leida") ?: false,
                fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                citaId = doc.getString("citaId")?.takeIf { it.isNotEmpty() }
            )
        }
        Result.success(notificaciones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getNotificacionesNoLeidasByUserId(usuarioId: String): Result<List<NotificacionFirestore>> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .await()

        val notificaciones = snapshot.documents.mapNotNull { doc ->
            NotificacionFirestore(
                id = doc.id,
                usuarioId = doc.getString("usuarioId") ?: "",
                titulo = doc.getString("titulo") ?: "",
                mensaje = doc.getString("mensaje") ?: "",
                tipo = doc.getString("tipo") ?: "",
                leida = doc.getBoolean("leida") ?: false,
                fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                citaId = doc.getString("citaId")?.takeIf { it.isNotEmpty() }
            )
        }
        Result.success(notificaciones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCountNotificacionesNoLeidas(usuarioId: String): Result<Int> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false)
            .get()
            .await()
        Result.success(snapshot.size())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun marcarNotificacionComoLeida(notificacionId: String): Result<Unit> = try {
        notificacionesCollection.document(notificacionId)
            .update("leida", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun marcarTodasNotificacionesComoLeidas(usuarioId: String): Result<Unit> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false)
            .get()
            .await()

        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "leida", true)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteNotificacionesLeidas(usuarioId: String): Result<Unit> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", true)
            .get()
            .await()

        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllNotificaciones(): Result<List<NotificacionFirestore>> = try {
        val snapshot = notificacionesCollection.get().await()
        val notificaciones = snapshot.documents.mapNotNull { doc ->
            doc.toObject(NotificacionFirestore::class.java)
        }
        Result.success(notificaciones)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
