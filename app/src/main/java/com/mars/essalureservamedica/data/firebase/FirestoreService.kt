package com.mars.essalureservamedica.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mars.essalureservamedica.data.firebase.models.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * Un servicio contenedor para Cloud Firestore que gestiona todas las interacciones con la base de datos.
 * Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en las diferentes colecciones
 * de la base de datos de la aplicación, como usuarios, doctores, citas, etc.
 * Utiliza corutinas de Kotlin y Flow para operaciones asíncronas y en tiempo real.
 */
class FirestoreService {
    // Instancia de FirebaseFirestore para interactuar con la base de datos.
    private val db = FirebaseFirestore.getInstance()

    // Referencias a las colecciones en Firestore para un acceso más fácil.
    private val usersCollection = db.collection("users")
    private val doctorsCollection = db.collection("doctors")
    private val citasCollection = db.collection("appointments")
    private val calificacionesCollection = db.collection("ratings")
    private val notificacionesCollection = db.collection("notifications")

    // --- SECCIÓN DE USUARIOS ---

    /**
     * Crea un nuevo documento de usuario en la colección 'users'.
     * @param user El objeto [UserFirestore] que se va a crear.
     * @return Un [Result] que contiene el ID del usuario creado en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun createUser(user: UserFirestore): Result<String> = try {
        val docRef = usersCollection.document(user.id)
        docRef.set(user.toMap()).await() // Establece los datos del usuario en el documento.
        Result.success(user.id) // Devuelve el ID del usuario en caso de éxito.
    } catch (e: Exception) {
        Result.failure(e) // Devuelve la excepción en caso de fallo.
    }

    /**
     * Recupera un único usuario de Firestore por su ID.
     * @param userId El ID del usuario a recuperar.
     * @return Un [Result] que contiene el objeto [UserFirestore] si se encuentra, nulo si no, o una excepción en caso de fallo.
     */
    suspend fun getUser(userId: String): Result<UserFirestore?> = try {
        val document = usersCollection.document(userId).get().await()
        if (document.exists()) {
            // Mapea los datos del documento a un objeto UserFirestore.
            val user = UserFirestore(
                id = document.id,
                nombreCompleto = document.getString("nombreCompleto") ?: "",
                email = document.getString("email") ?: "",
                createdAt = document.getLong("createdAt") ?: 0L
            )
            Result.success(user)
        } else {
            Result.success(null) // El usuario no fue encontrado.
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtiene una lista de todos los usuarios de la colección.
     * @return Un [Result] que contiene una lista de objetos [UserFirestore] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun getAllUsers(): Result<List<UserFirestore>> = try {
        val snapshot = usersCollection.get().await()
        val users = snapshot.documents.mapNotNull { doc ->
            // Mapea cada documento a un objeto UserFirestore.
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

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email El correo electrónico del usuario a buscar.
     * @return Un [Result] que contiene el objeto [UserFirestore] si se encuentra, nulo si no, o una excepción en caso de fallo.
     */
    suspend fun getUserByEmail(email: String): Result<UserFirestore?> = try {
        val snapshot = usersCollection
            .whereEqualTo("email", email) // Filtra por el campo de correo electrónico.
            .get()
            .await()
        
        // Obtiene el primer usuario que coincida o nulo.
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

    /**
     * Actualiza campos específicos de un documento de usuario.
     * @param userId El ID del usuario a actualizar.
     * @param updates Un mapa que contiene los campos y los nuevos valores a actualizar.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersCollection.document(userId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- SECCIÓN DE DOCTORES ---

    /**
     * Proporciona un flujo (Flow) de la lista de doctores, que se actualiza en tiempo real.
     * Los doctores se ordenan alfabéticamente por nombre.
     * @return Un [Flow] que emite la lista de [DoctorFirestore] cada vez que hay cambios en la colección.
     */
    fun getDoctorsFlow(): Flow<List<DoctorFirestore>> = callbackFlow {
        val subscription = doctorsCollection
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Cierra el flujo con un error si ocurre uno.
                    return@addSnapshotListener
                }

                // Convierte los documentos del snapshot en una lista de objetos DoctorFirestore.
                val doctors = snapshot?.documents?.mapNotNull {
                    it.toObject(DoctorFirestore::class.java)
                } ?: emptyList()

                trySend(doctors) // Emite la lista actualizada de doctores.
            }

        // Se llama cuando el flujo se cierra, eliminando el listener para evitar fugas de memoria.
        awaitClose { subscription.remove() }
    }

    /**
     * Añade un nuevo doctor a la colección 'doctors'.
     * @param doctor El objeto [DoctorFirestore] a añadir.
     * @return Un [Result] que contiene el ID del documento del doctor creado en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun addDoctor(doctor: DoctorFirestore): Result<String> = try {
        val docRef = doctorsCollection.document() // Crea una referencia a un nuevo documento con un ID autogenerado.
        val doctorWithId = doctor.copy(id = docRef.id)
        docRef.set(doctorWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera todos los doctores de la colección.
     * @return Un [Result] que contiene una lista de todos los [DoctorFirestore] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun getAllDoctors(): Result<List<DoctorFirestore>> = try {
        val snapshot = doctorsCollection.get().await()
        val doctors = snapshot.documents.mapNotNull { doc ->
            doc.toObject(DoctorFirestore::class.java)
        }
        Result.success(doctors)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera un único doctor por su ID.
     * @param doctorId El ID del doctor a recuperar.
     * @return Un [Result] que contiene el objeto [DoctorFirestore] si se encuentra, nulo si no, o una excepción en caso de fallo.
     */
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

    // --- SECCIÓN DE CITAS ---

    /**
     * Proporciona un flujo (Flow) de las citas de un usuario específico, que se actualiza en tiempo real.
     * @param userId El ID del usuario para el que se recuperan las citas.
     * @return Un [Flow] que emite la lista de [CitaFirestore] del usuario, ordenada por fecha descendente.
     */
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

    /**
     * Crea una nueva cita en la colección 'appointments'.
     * @param cita El objeto [CitaFirestore] a crear.
     * @return Un [Result] que contiene el ID de la cita creada en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun createCita(cita: CitaFirestore): Result<String> = try {
        val docRef = citasCollection.document()
        val citaWithId = cita.copy(id = docRef.id)
        docRef.set(citaWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Actualiza el estado de una cita específica.
     * @param citaId El ID de la cita a actualizar.
     * @param newStatus El nuevo estado de la cita (p. ej., "Confirmada", "Cancelada").
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun updateCitaStatus(citaId: String, newStatus: String): Result<Unit> = try {
        citasCollection.document(citaId)
            .update("estado", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Actualiza el estado de una cita. (Función duplicada, considera eliminar una de ellas)
     * @param citaId El ID de la cita a actualizar.
     * @param newStatus El nuevo estado de la cita.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun updateCitaEstado(citaId: String, newStatus: String): Result<Unit> = try {
        citasCollection.document(citaId)
            .update("estado", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Actualiza campos específicos de un documento de cita.
     * @param citaId El ID de la cita a actualizar.
     * @param updates Un mapa que contiene los campos y los nuevos valores a actualizar.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun updateCita(citaId: String, updates: Map<String, Any>): Result<Unit> = try {
        citasCollection.document(citaId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera todas las citas de la colección.
     * @return Un [Result] que contiene una lista de todos los [CitaFirestore] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun getAllCitas(): Result<List<CitaFirestore>> = try {
        val snapshot = citasCollection.get().await()
        val citas = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CitaFirestore::class.java)
        }
        Result.success(citas)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtiene las citas para un doctor específico en una fecha determinada.
     * @param doctorId El ID del doctor.
     * @param fechaTimestamp La fecha como un timestamp de Unix.
     * @return Un [Result] que contiene una lista de [CitaFirestore] que coinciden, o una excepción en caso de fallo.
     */
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

    // --- SECCIÓN DE CALIFICACIONES ---

    /**
     * Añade una nueva calificación y actualiza la calificación promedio del doctor correspondiente.
     * @param calificacion El objeto [CalificacionFirestore] a añadir.
     * @return Un [Result] que contiene el ID de la calificación creada en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun addCalificacion(calificacion: CalificacionFirestore): Result<String> = try {
        val docRef = calificacionesCollection.document()
        val calificacionWithId = calificacion.copy(id = docRef.id)
        docRef.set(calificacionWithId.toMap()).await()

        // Llama a la función para recalcular y actualizar la calificación del doctor.
        updateDoctorRating(calificacion.doctorId)

        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Actualiza la calificación promedio y el número total de calificaciones de un doctor.
     * Esta es una función privada de ayuda llamada después de que se añade una nueva calificación.
     * @param doctorId El ID del doctor cuya calificación se va a actualizar.
     */
    private suspend fun updateDoctorRating(doctorId: String) {
        try {
            // Obtiene todas las calificaciones para el doctor específico.
            val calificaciones = calificacionesCollection
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()

            val ratings = calificaciones.documents.mapNotNull { it.getLong("puntuacion")?.toInt() }
            if (ratings.isNotEmpty()) {
                val avgRating = ratings.average()
                // Actualiza el documento del doctor con la nueva calificación promedio y el recuento total.
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
            // Manejar el error de forma silenciosa para no interrumpir el flujo principal si la actualización de la calificación falla.
        }
    }

    /**
     * Recupera todas las calificaciones de la colección.
     * @return Un [Result] que contiene una lista de todos los [CalificacionFirestore] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun getAllCalificaciones(): Result<List<CalificacionFirestore>> = try {
        val snapshot = calificacionesCollection.get().await()
        val calificaciones = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CalificacionFirestore::class.java)
        }
        Result.success(calificaciones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtiene la calificación asociada a un ID de cita específico.
     * @param citaId El ID de la cita para la que se busca una calificación.
     * @return Un [Result] que contiene el objeto [CalificacionFirestore] si se encuentra, nulo si no, o una excepción en caso de fallo.
     */
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

    // --- SECCIÓN DE NOTIFICACIONES ---

    /**
     * Crea una nueva notificación en la colección 'notifications'.
     * @param notificacion El objeto [NotificacionFirestore] a crear.
     * @return Un [Result] que contiene el ID de la notificación creada en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun createNotificacion(notificacion: NotificacionFirestore): Result<String> = try {
        val docRef = notificacionesCollection.document()
        val notificacionWithId = notificacion.copy(id = docRef.id)
        docRef.set(notificacionWithId.toMap()).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera todas las notificaciones para un usuario específico, ordenadas por fecha de creación descendente.
     * @param usuarioId El ID del usuario.
     * @return Un [Result] que contiene una lista de [NotificacionFirestore], o una excepción en caso de fallo.
     */
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

    /**
     * Recupera solo las notificaciones no leídas para un usuario específico.
     * @param usuarioId El ID del usuario.
     * @return Un [Result] que contiene una lista de [NotificacionFirestore] no leídas, o una excepción en caso de fallo.
     */
    suspend fun getNotificacionesNoLeidasByUserId(usuarioId: String): Result<List<NotificacionFirestore>> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false) // Filtra solo las no leídas.
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

    /**
     * Cuenta el número de notificaciones no leídas para un usuario.
     * @param usuarioId El ID del usuario.
     * @return Un [Result] que contiene el recuento de notificaciones no leídas en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun getCountNotificacionesNoLeidas(usuarioId: String): Result<Int> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false)
            .get()
            .await()
        Result.success(snapshot.size()) // size() es más eficiente que obtener todos los documentos.
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Marca una notificación específica como leída.
     * @param notificacionId El ID de la notificación a marcar como leída.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun marcarNotificacionComoLeida(notificacionId: String): Result<Unit> = try {
        notificacionesCollection.document(notificacionId)
            .update("leida", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Marca todas las notificaciones no leídas de un usuario como leídas usando una operación por lotes (batch).
     * @param usuarioId El ID del usuario.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun marcarTodasNotificacionesComoLeidas(usuarioId: String): Result<Unit> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", false)
            .get()
            .await()

        val batch = db.batch() // Inicia una escritura por lotes para atomicidad y eficiencia.
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "leida", true)
        }
        batch.commit().await() // Ejecuta todas las operaciones de actualización en el lote.
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Elimina todas las notificaciones leídas de un usuario usando una operación por lotes (batch).
     * @param usuarioId El ID del usuario.
     * @return Un [Result] con [Unit] en caso de éxito, o una excepción en caso de fallo.
     */
    suspend fun deleteNotificacionesLeidas(usuarioId: String): Result<Unit> = try {
        val snapshot = notificacionesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("leida", true) // Filtra solo las leídas.
            .get()
            .await()

        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference) // Añade una operación de eliminación al lote.
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera todas las notificaciones de la colección (principalmente para fines de administración o depuración).
     * @return Un [Result] que contiene una lista de todos los [NotificacionFirestore] en caso de éxito, o una excepción en caso de fallo.
     */
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
