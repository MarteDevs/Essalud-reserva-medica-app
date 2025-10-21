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

    // Usuarios
    suspend fun createUser(user: UserFirestore): Result<String> = try {
        val docRef = usersCollection.document(user.id)
        docRef.set(user.toMap()).await()
        Result.success(user.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(userId: String): Result<UserFirestore> = try {
        val doc = usersCollection.document(userId).get().await()
        if (doc.exists()) {
            Result.success(doc.toObject(UserFirestore::class.java)!!)
        } else {
            Result.failure(NoSuchElementException("Usuario no encontrado"))
        }
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
}
