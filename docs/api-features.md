# API y Funcionalidades - Kardia

## Funcionalidades Principales

### 1. Sistema de Autenticación

#### Registro de Usuarios
```kotlin
// AuthViewModel
suspend fun registerUser(nombreCompleto: String, email: String, password: String): Result<User> {
    return try {
        // Validar datos de entrada
        if (!isValidEmail(email)) {
            return Result.failure(Exception("Email inválido"))
        }
        
        // Verificar si el usuario ya existe
        val existingUser = repository.getUserByEmail(email)
        if (existingUser != null) {
            return Result.failure(Exception("El usuario ya existe"))
        }
        
        // Crear nuevo usuario
        val user = User(
            nombreCompleto = nombreCompleto,
            email = email,
            password = hashPassword(password)
        )
        
        val userId = repository.insertUser(user)
        Result.success(user.copy(id = userId.toInt()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### Inicio de Sesión
```kotlin
suspend fun loginUser(email: String, password: String): Result<User> {
    return try {
        val hashedPassword = hashPassword(password)
        val user = repository.getUserByEmailAndPassword(email, hashedPassword)
        
        if (user != null) {
            // Guardar sesión
            sessionManager.saveUserSession(user.id, user.nombreCompleto, user.email)
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales inválidas"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Características:**
- Validación de email
- Encriptación de contraseñas
- Gestión de sesiones persistentes
- Manejo de errores

### 2. Gestión de Doctores

#### Obtener Lista de Doctores
```kotlin
// DoctorsViewModel
class DoctorsViewModel : ViewModel() {
    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> = _doctors
    
    private val _filteredDoctors = MutableLiveData<List<Doctor>>()
    val filteredDoctors: LiveData<List<Doctor>> = _filteredDoctors
    
    fun loadDoctors() {
        repository.getAllDoctors().observeForever { doctorList ->
            _doctors.value = doctorList
            _filteredDoctors.value = doctorList
        }
    }
    
    fun searchDoctors(query: String) {
        val currentDoctors = _doctors.value ?: return
        val filtered = currentDoctors.filter { doctor ->
            doctor.nombre.contains(query, ignoreCase = true) ||
            doctor.especialidad.contains(query, ignoreCase = true)
        }
        _filteredDoctors.value = filtered
    }
    
    fun filterBySpecialty(specialty: String) {
        val currentDoctors = _doctors.value ?: return
        val filtered = if (specialty == "Todas") {
            currentDoctors
        } else {
            currentDoctors.filter { it.especialidad == specialty }
        }
        _filteredDoctors.value = filtered
    }
}
```

#### Detalles de Doctor
```kotlin
// DoctorDetailViewModel
suspend fun loadDoctorDetails(doctorId: Int) {
    try {
        val doctor = repository.getDoctorById(doctorId)
        val calificaciones = repository.getCalificacionesByDoctorId(doctorId)
        val promedio = repository.getPromedioPuntuacionDoctor(doctorId)
        
        _doctorDetails.value = DoctorDetails(
            doctor = doctor,
            calificaciones = calificaciones,
            promedioCalificacion = promedio ?: 0f
        )
    } catch (e: Exception) {
        _error.value = e.message
    }
}
```

**Funcionalidades:**
- Lista completa de doctores
- Búsqueda por nombre y especialidad
- Filtrado por especialidad médica
- Detalles completos del doctor
- Sistema de calificaciones

### 3. Sistema de Citas Médicas

#### Agendar Cita
```kotlin
// ScheduleViewModel
suspend fun scheduleAppointment(
    doctorId: Int,
    fechaHora: Date,
    notas: String?
): Result<Long> {
    return try {
        val userId = sessionManager.getUserId()
        
        // Verificar disponibilidad
        val existingCitas = repository.getCitasPorDoctorYFecha(doctorId, fechaHora.time)
        if (existingCitas.isNotEmpty()) {
            return Result.failure(Exception("Horario no disponible"))
        }
        
        // Crear nueva cita
        val cita = Cita(
            usuarioId = userId,
            doctorId = doctorId,
            fechaHora = fechaHora,
            estado = EstadoCita.PENDIENTE.name,
            notas = notas
        )
        
        val citaId = repository.insertCita(cita)
        
        // Crear notificación
        repository.crearNotificacionCita(
            usuarioId = userId,
            citaId = citaId.toInt(),
            tipo = "CITA_CONFIRMADA",
            titulo = "Cita Agendada",
            mensaje = "Tu cita ha sido agendada exitosamente"
        )
        
        Result.success(citaId)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### Gestión de Citas Existentes
```kotlin
// AppointmentsViewModel
suspend fun cancelAppointment(citaId: Int) {
    try {
        repository.updateCitaEstado(citaId, EstadoCita.CANCELADA.name)
        
        // Notificar cancelación
        val userId = sessionManager.getUserId()
        repository.crearNotificacionCita(
            usuarioId = userId,
            citaId = citaId,
            tipo = "CITA_CANCELADA",
            titulo = "Cita Cancelada",
            mensaje = "Tu cita ha sido cancelada"
        )
        
        _operationResult.value = "Cita cancelada exitosamente"
    } catch (e: Exception) {
        _error.value = e.message
    }
}

suspend fun rescheduleAppointment(citaId: Int, nuevaFechaHora: Date) {
    try {
        repository.reprogramarCita(citaId, nuevaFechaHora)
        repository.updateCitaEstado(citaId, EstadoCita.REPROGRAMADA.name)
        
        _operationResult.value = "Cita reprogramada exitosamente"
    } catch (e: Exception) {
        _error.value = e.message
    }
}
```

**Funcionalidades:**
- Agendar nuevas citas
- Verificación de disponibilidad
- Cancelación de citas
- Reprogramación de citas
- Historial de citas
- Estados de cita (Pendiente, Confirmada, Completada, Cancelada)

### 4. Sistema de Calificaciones

#### Calificar Atención Médica
```kotlin
// RatingDialogFragment
suspend fun submitRating(
    citaId: Int,
    doctorId: Int,
    puntuacion: Float,
    comentario: String?
): Result<Long> {
    return try {
        val userId = sessionManager.getUserId()
        
        // Verificar si ya existe calificación
        val existingRating = repository.getCalificacionByCitaId(citaId)
        if (existingRating != null) {
            return Result.failure(Exception("Ya has calificado esta cita"))
        }
        
        val calificacion = Calificacion(
            usuarioId = userId,
            doctorId = doctorId,
            citaId = citaId,
            puntuacion = puntuacion,
            comentario = comentario,
            fechaCalificacion = Date()
        )
        
        val ratingId = repository.insertCalificacion(calificacion)
        
        // Actualizar estado de la cita
        repository.updateCitaEstado(citaId, EstadoCita.COMPLETADA.name)
        
        Result.success(ratingId)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### Obtener Calificaciones de Doctor
```kotlin
fun loadDoctorRatings(doctorId: Int) {
    repository.getCalificacionesConDetallesByDoctorId(doctorId).observeForever { ratings ->
        _doctorRatings.value = ratings
        
        // Calcular estadísticas
        val promedio = ratings.map { it.puntuacion }.average().toFloat()
        val total = ratings.size
        
        _ratingStats.value = RatingStats(
            promedio = promedio,
            totalCalificaciones = total,
            distribucion = calculateRatingDistribution(ratings)
        )
    }
}
```

**Funcionalidades:**
- Calificación con estrellas (1-5)
- Comentarios opcionales
- Prevención de calificaciones duplicadas
- Estadísticas de calificaciones por doctor
- Historial de calificaciones del usuario

### 5. Sistema de Notificaciones

#### Crear Notificaciones
```kotlin
// AppRepository
suspend fun crearNotificacionCita(
    usuarioId: Int,
    citaId: Int,
    tipo: String,
    titulo: String,
    mensaje: String
) {
    val notificacion = Notificacion(
        usuarioId = usuarioId,
        titulo = titulo,
        mensaje = mensaje,
        tipo = tipo,
        citaId = citaId,
        fechaCreacion = Date()
    )
    
    insertNotificacion(notificacion)
}
```

#### Gestión de Notificaciones
```kotlin
// NotificationsViewModel
fun loadNotifications() {
    val userId = sessionManager.getUserId()
    repository.getNotificacionesByUserId(userId).observeForever { notifications ->
        _notifications.value = notifications
    }
}

suspend fun markAsRead(notificationId: Int) {
    repository.marcarNotificacionComoLeida(notificationId)
}

suspend fun markAllAsRead() {
    val userId = sessionManager.getUserId()
    repository.marcarTodasNotificacionesComoLeidas(userId)
}

fun getUnreadCount(): LiveData<Int> {
    val userId = sessionManager.getUserId()
    return repository.getCountNotificacionesNoLeidas(userId)
}
```

**Tipos de Notificaciones:**
- `CITA_CONFIRMADA`: Confirmación de nueva cita
- `CITA_CANCELADA`: Notificación de cancelación
- `RECORDATORIO`: Recordatorios de citas próximas
- `GENERAL`: Notificaciones generales del sistema

### 6. Gestión de Perfil

#### Estadísticas de Usuario
```kotlin
// ProfileViewModel
suspend fun loadUserStats() {
    try {
        val userId = sessionManager.getUserId()
        
        val totalCitas = repository.getCitaCountByUserId(userId)
        val citasCompletadas = repository.getCitaCountByUserIdAndEstado(userId, "COMPLETADA")
        val citasPendientes = repository.getCitaCountByUserIdAndEstado(userId, "PENDIENTE")
        
        _userStats.value = UserStats(
            totalCitas = totalCitas,
            citasCompletadas = citasCompletadas,
            citasPendientes = citasPendientes
        )
    } catch (e: Exception) {
        _error.value = e.message
    }
}
```

#### Actualizar Perfil
```kotlin
suspend fun updateProfile(nombreCompleto: String, email: String): Result<Unit> {
    return try {
        val userId = sessionManager.getUserId()
        val currentUser = repository.getUserById(userId)
        
        if (currentUser != null) {
            val updatedUser = currentUser.copy(
                nombreCompleto = nombreCompleto,
                email = email
            )
            
            repository.updateUser(updatedUser)
            sessionManager.saveUserSession(userId, nombreCompleto, email)
            
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Manejo de Errores

### Estrategia Global
```kotlin
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    data class Loading<T>(val data: T? = null) : Resource<T>()
}
```

### Validaciones
```kotlin
object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
}
```

## Seguridad

### Encriptación de Contraseñas
```kotlin
object SecurityUtils {
    fun hashPassword(password: String): String {
        return password.hashCode().toString() // Simplificado para demo
        // En producción usar BCrypt o similar
    }
}
```

### Gestión de Sesiones
```kotlin
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    
    fun saveUserSession(userId: Int, userName: String, userEmail: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("user_name", userName)
            putString("user_email", userEmail)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
```

## Performance y Optimización

### Consultas Optimizadas
- Uso de índices en campos de búsqueda frecuente
- Consultas con JOIN para reducir llamadas a BD
- LiveData para actualizaciones reactivas

### Gestión de Memoria
- ViewBinding para evitar memory leaks
- Coroutines para operaciones asíncronas
- RecyclerView con ViewHolder pattern

### Caching
- Room como cache local
- SharedPreferences para configuraciones
- Singleton pattern para Repository