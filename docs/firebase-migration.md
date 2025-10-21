# Migración a Firebase - Kardia

## Introducción

Esta guía detalla el proceso completo para migrar la aplicación **Kardia** desde Room Database local a **Firebase Firestore** como base de datos en la nube, incluyendo la implementación de **Firebase Authentication** para el manejo de usuarios.

## Ventajas de Firebase

### Firebase Firestore
- **Sincronización en tiempo real** entre dispositivos
- **Escalabilidad automática** sin configuración
- **Offline support** nativo
- **Consultas complejas** y indexación automática
- **Seguridad robusta** con reglas de seguridad

### Firebase Authentication
- **Múltiples proveedores** (Email, Google, Facebook, etc.)
- **Gestión de sesiones** automática
- **Verificación de email** integrada
- **Recuperación de contraseña** automática
- **Seguridad enterprise-grade**

## Paso 1: Configuración del Proyecto Firebase

### 1.1 Crear Proyecto en Firebase Console

1. **Acceder a Firebase Console**
   - Ir a [https://console.firebase.google.com/](https://console.firebase.google.com/)
   - Iniciar sesión con cuenta de Google

2. **Crear Nuevo Proyecto**
   ```
   - Click en "Crear un proyecto"
   - Nombre del proyecto: "kardia-medical-app"
   - Habilitar Google Analytics (recomendado)
   - Seleccionar cuenta de Analytics
   - Click en "Crear proyecto"
   ```

3. **Configurar Proyecto**
   - Esperar a que se complete la configuración
   - Click en "Continuar"

### 1.2 Agregar App Android al Proyecto

1. **Registrar App Android**
   ```
   - Click en el ícono de Android
   - Package name: com.yourpackage.kardia
   - App nickname: Kardia Medical App
   - SHA-1 certificate fingerprint (opcional para desarrollo)
   ```

2. **Obtener SHA-1 Certificate (Desarrollo)**
   ```bash
   # Para debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # Para release keystore
   keytool -list -v -keystore path/to/your/keystore.jks -alias your-alias
   ```

3. **Descargar google-services.json**
   - Descargar el archivo `google-services.json`
   - Colocar en `app/` directory del proyecto Android

## Paso 2: Configuración de Dependencias

### 2.1 Actualizar build.gradle.kts (Project level)

```kotlin
// build.gradle.kts (Project level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    // Agregar plugin de Google Services
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

### 2.2 Actualizar libs.versions.toml

```toml
[versions]
# Versiones existentes...
firebase-bom = "32.7.0"
firebase-auth = "22.3.0"
firebase-firestore = "24.10.0"
firebase-analytics = "21.5.0"
firebase-crashlytics = "18.6.1"
play-services-auth = "20.7.0"

[libraries]
# Librerías existentes...

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
play-services-auth = { group = "com.google.android.gms", name = "play-services-auth", version.ref = "play-services-auth" }

[plugins]
# Plugins existentes...
google-services = { id = "com.google.gms.google-services", version = "4.4.0" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "2.9.9" }
```

### 2.3 Actualizar app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    // Agregar plugins de Firebase
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

dependencies {
    // Dependencias existentes...
    
    // Firebase BOM - maneja todas las versiones de Firebase
    implementation(platform(libs.firebase.bom))
    
    // Firebase services
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    
    // Google Play Services
    implementation(libs.play.services.auth)
    
    // Mantener Room para cache local (opcional)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
```

## Paso 3: Estructura de Base de Datos Firestore

### 3.1 Diseño de Colecciones

```
kardia-medical-app (Database)
├── users/
│   └── {userId}/
│       ├── email: string
│       ├── nombreCompleto: string
│       ├── fechaRegistro: timestamp
│       ├── telefono: string (opcional)
│       ├── fechaNacimiento: timestamp (opcional)
│       └── profileImageUrl: string (opcional)
│
├── doctors/
│   └── {doctorId}/
│       ├── nombre: string
│       ├── especialidad: string
│       ├── descripcion: string
│       ├── horarioAtencion: string
│       ├── calificacionPromedio: number
│       ├── totalCalificaciones: number
│       ├── telefono: string
│       ├── email: string
│       ├── consultorio: string
│       ├── experiencia: number
│       ├── imageUrl: string
│       └── disponible: boolean
│
├── appointments/
│   └── {appointmentId}/
│       ├── userId: string (referencia)
│       ├── doctorId: string (referencia)
│       ├── fechaHora: timestamp
│       ├── estado: string (PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA, REPROGRAMADA)
│       ├── notas: string
│       ├── fechaCreacion: timestamp
│       ├── fechaActualizacion: timestamp
│       ├── motivoConsulta: string
│       └── duracionEstimada: number
│
├── ratings/
│   └── {ratingId}/
│       ├── userId: string (referencia)
│       ├── doctorId: string (referencia)
│       ├── appointmentId: string (referencia)
│       ├── puntuacion: number (1-5)
│       ├── comentario: string
│       ├── fechaCalificacion: timestamp
│       └── anonimo: boolean
│
├── notifications/
│   └── {notificationId}/
│       ├── userId: string (referencia)
│       ├── titulo: string
│       ├── mensaje: string
│       ├── tipo: string (CITA_CONFIRMADA, RECORDATORIO, CANCELACION, etc.)
│       ├── leida: boolean
│       ├── fechaCreacion: timestamp
│       ├── appointmentId: string (referencia, opcional)
│       └── actionUrl: string (opcional)
│
└── specialties/
    └── {specialtyId}/
        ├── nombre: string
        ├── descripcion: string
        ├── icono: string
        └── activa: boolean
```

### 3.2 Índices Compuestos Requeridos

```javascript
// Índices para optimizar consultas
appointments:
  - userId, fechaHora (ascending)
  - doctorId, fechaHora (ascending)
  - userId, estado (ascending)
  - doctorId, estado (ascending)

ratings:
  - doctorId, fechaCalificacion (descending)
  - userId, fechaCalificacion (descending)

notifications:
  - userId, fechaCreacion (descending)
  - userId, leida, fechaCreacion (descending)
```

## Paso 4: Configuración de Firebase Authentication

### 4.1 Habilitar Métodos de Autenticación

1. **En Firebase Console**
   ```
   - Ir a Authentication > Sign-in method
   - Habilitar "Email/Password"
   - Habilitar "Google" (opcional)
   - Configurar dominio autorizado
   ```

2. **Configurar Google Sign-In (Opcional)**
   ```
   - Descargar google-services.json actualizado
   - Configurar OAuth consent screen
   - Agregar SHA-1 fingerprints
   ```

### 4.2 Implementar FirebaseAuth Manager

```kotlin
// data/auth/FirebaseAuthManager.kt
class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Registro con email y contraseña
    suspend fun registerWithEmail(
        email: String,
        password: String,
        nombreCompleto: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error al crear usuario")
            
            // Crear documento de usuario en Firestore
            val userData = hashMapOf(
                "email" to email,
                "nombreCompleto" to nombreCompleto,
                "fechaRegistro" to FieldValue.serverTimestamp()
            )
            
            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .await()
            
            // Enviar verificación de email
            user.sendEmailVerification().await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Inicio de sesión con email y contraseña
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error al iniciar sesión")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Inicio de sesión con Google
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Error al iniciar sesión con Google")
            
            // Verificar si es la primera vez que inicia sesión
            if (result.additionalUserInfo?.isNewUser == true) {
                val userData = hashMapOf(
                    "email" to user.email,
                    "nombreCompleto" to user.displayName,
                    "fechaRegistro" to FieldValue.serverTimestamp(),
                    "profileImageUrl" to user.photoUrl?.toString()
                )
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cerrar sesión
    fun signOut() {
        auth.signOut()
    }
    
    // Usuario actual
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    // Observar cambios de autenticación
    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    // Recuperar contraseña
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Paso 5: Configuración de Firestore

### 5.1 Habilitar Firestore

1. **En Firebase Console**
   ```
   - Ir a Firestore Database
   - Click en "Crear base de datos"
   - Seleccionar "Comenzar en modo de prueba" (temporal)
   - Elegir ubicación (us-central1 recomendado para Latinoamérica)
   ```

2. **Configurar Reglas de Seguridad Iniciales**
   ```javascript
   // Reglas temporales para desarrollo
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.time < timestamp.date(2024, 12, 31);
       }
     }
   }
   ```

### 5.2 Implementar FirestoreRepository

```kotlin
// data/repository/FirestoreRepository.kt
class FirestoreRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // Usuarios
    suspend fun createUser(user: User): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userData = user.toFirestoreMap()
            firestore.collection("users")
                .document(user.id)
                .set(userData)
                .await()
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val user = document.toUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Doctores
    fun getAllDoctors(): Flow<List<Doctor>> = callbackFlow {
        val listener = firestore.collection("doctors")
            .whereEqualTo("disponible", true)
            .orderBy("calificacionPromedio", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val doctors = snapshot?.documents?.mapNotNull { it.toDoctor() } ?: emptyList()
                trySend(doctors)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun getDoctorById(doctorId: String): Result<Doctor?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection("doctors")
                .document(doctorId)
                .get()
                .await()
            
            val doctor = document.toDoctor()
            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Citas
    suspend fun createAppointment(appointment: Appointment): Result<String> = withContext(Dispatchers.IO) {
        try {
            val appointmentData = appointment.toFirestoreMap()
            val documentRef = firestore.collection("appointments").document()
            
            firestore.runTransaction { transaction ->
                transaction.set(documentRef, appointmentData)
                
                // Crear notificación
                val notificationData = hashMapOf(
                    "userId" to appointment.userId,
                    "titulo" to "Cita Agendada",
                    "mensaje" to "Tu cita ha sido agendada exitosamente",
                    "tipo" to "CITA_CONFIRMADA",
                    "leida" to false,
                    "fechaCreacion" to FieldValue.serverTimestamp(),
                    "appointmentId" to documentRef.id
                )
                
                val notificationRef = firestore.collection("notifications").document()
                transaction.set(notificationRef, notificationData)
            }.await()
            
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getUserAppointments(userId: String): Flow<List<AppointmentWithDetails>> = callbackFlow {
        val listener = firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toAppointmentWithDetails()
                } ?: emptyList()
                
                trySend(appointments)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Calificaciones
    suspend fun createRating(rating: Rating): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ratingData = rating.toFirestoreMap()
            val documentRef = firestore.collection("ratings").document()
            
            firestore.runTransaction { transaction ->
                transaction.set(documentRef, ratingData)
                
                // Actualizar promedio del doctor
                updateDoctorRating(transaction, rating.doctorId, rating.puntuacion)
            }.await()
            
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun updateDoctorRating(
        transaction: Transaction,
        doctorId: String,
        newRating: Float
    ) {
        val doctorRef = firestore.collection("doctors").document(doctorId)
        val doctorDoc = transaction.get(doctorRef)
        
        val currentAverage = doctorDoc.getDouble("calificacionPromedio")?.toFloat() ?: 0f
        val currentCount = doctorDoc.getLong("totalCalificaciones")?.toInt() ?: 0
        
        val newCount = currentCount + 1
        val newAverage = ((currentAverage * currentCount) + newRating) / newCount
        
        transaction.update(doctorRef, mapOf(
            "calificacionPromedio" to newAverage,
            "totalCalificaciones" to newCount
        ))
    }
    
    // Notificaciones
    fun getUserNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull { it.toNotification() } ?: emptyList()
                trySend(notifications)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("leida", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Paso 6: Modelos de Datos para Firebase

### 6.1 Extensiones para Conversión

```kotlin
// data/models/FirebaseExtensions.kt

// User extensions
fun User.toFirestoreMap(): Map<String, Any> = mapOf(
    "email" to email,
    "nombreCompleto" to nombreCompleto,
    "fechaRegistro" to (fechaRegistro?.let { Timestamp(it) } ?: FieldValue.serverTimestamp()),
    "telefono" to (telefono ?: ""),
    "fechaNacimiento" to (fechaNacimiento?.let { Timestamp(it) } ?: null),
    "profileImageUrl" to (profileImageUrl ?: "")
).filterValues { it != null }

fun DocumentSnapshot.toUser(): User? {
    return try {
        User(
            id = id,
            email = getString("email") ?: return null,
            nombreCompleto = getString("nombreCompleto") ?: return null,
            fechaRegistro = getTimestamp("fechaRegistro")?.toDate(),
            telefono = getString("telefono"),
            fechaNacimiento = getTimestamp("fechaNacimiento")?.toDate(),
            profileImageUrl = getString("profileImageUrl")
        )
    } catch (e: Exception) {
        null
    }
}

// Doctor extensions
fun Doctor.toFirestoreMap(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "especialidad" to especialidad,
    "descripcion" to descripcion,
    "horarioAtencion" to horarioAtencion,
    "calificacionPromedio" to calificacionPromedio,
    "totalCalificaciones" to totalCalificaciones,
    "telefono" to telefono,
    "email" to email,
    "consultorio" to consultorio,
    "experiencia" to experiencia,
    "imageUrl" to imageUrl,
    "disponible" to disponible
)

fun DocumentSnapshot.toDoctor(): Doctor? {
    return try {
        Doctor(
            id = id,
            nombre = getString("nombre") ?: return null,
            especialidad = getString("especialidad") ?: return null,
            descripcion = getString("descripcion") ?: "",
            horarioAtencion = getString("horarioAtencion") ?: "",
            calificacionPromedio = getDouble("calificacionPromedio")?.toFloat() ?: 0f,
            totalCalificaciones = getLong("totalCalificaciones")?.toInt() ?: 0,
            telefono = getString("telefono") ?: "",
            email = getString("email") ?: "",
            consultorio = getString("consultorio") ?: "",
            experiencia = getLong("experiencia")?.toInt() ?: 0,
            imageUrl = getString("imageUrl") ?: "",
            disponible = getBoolean("disponible") ?: true
        )
    } catch (e: Exception) {
        null
    }
}

// Appointment extensions
fun Appointment.toFirestoreMap(): Map<String, Any> = mapOf(
    "userId" to userId,
    "doctorId" to doctorId,
    "fechaHora" to Timestamp(fechaHora),
    "estado" to estado,
    "notas" to (notas ?: ""),
    "fechaCreacion" to FieldValue.serverTimestamp(),
    "fechaActualizacion" to FieldValue.serverTimestamp(),
    "motivoConsulta" to (motivoConsulta ?: ""),
    "duracionEstimada" to duracionEstimada
)

fun DocumentSnapshot.toAppointment(): Appointment? {
    return try {
        Appointment(
            id = id,
            userId = getString("userId") ?: return null,
            doctorId = getString("doctorId") ?: return null,
            fechaHora = getTimestamp("fechaHora")?.toDate() ?: return null,
            estado = getString("estado") ?: "PENDIENTE",
            notas = getString("notas"),
            fechaCreacion = getTimestamp("fechaCreacion")?.toDate(),
            fechaActualizacion = getTimestamp("fechaActualizacion")?.toDate(),
            motivoConsulta = getString("motivoConsulta"),
            duracionEstimada = getLong("duracionEstimada")?.toInt() ?: 30
        )
    } catch (e: Exception) {
        null
    }
}
```

## Paso 7: Reglas de Seguridad de Firestore

### 7.1 Reglas de Producción

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Función para verificar autenticación
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Función para verificar si es el propietario
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // Usuarios - solo pueden leer/escribir sus propios datos
    match /users/{userId} {
      allow read, write: if isAuthenticated() && isOwner(userId);
    }
    
    // Doctores - lectura pública, escritura solo para administradores
    match /doctors/{doctorId} {
      allow read: if true; // Lectura pública
      allow write: if false; // Solo administradores (implementar función personalizada)
    }
    
    // Citas - solo el usuario propietario puede leer/escribir
    match /appointments/{appointmentId} {
      allow read, write: if isAuthenticated() && 
        (isOwner(resource.data.userId) || isOwner(request.resource.data.userId));
      allow create: if isAuthenticated() && isOwner(request.resource.data.userId);
    }
    
    // Calificaciones - solo el usuario propietario puede crear/leer
    match /ratings/{ratingId} {
      allow read: if true; // Lectura pública para mostrar calificaciones
      allow create: if isAuthenticated() && isOwner(request.resource.data.userId);
      allow update, delete: if isAuthenticated() && isOwner(resource.data.userId);
    }
    
    // Notificaciones - solo el usuario propietario
    match /notifications/{notificationId} {
      allow read, write: if isAuthenticated() && 
        (isOwner(resource.data.userId) || isOwner(request.resource.data.userId));
    }
    
    // Especialidades - lectura pública
    match /specialties/{specialtyId} {
      allow read: if true;
      allow write: if false; // Solo administradores
    }
  }
}
```

## Paso 8: Migración de Datos Existentes

### 8.1 Script de Migración

```kotlin
// utils/DataMigration.kt
class DataMigration(
    private val roomRepository: AppRepository,
    private val firestoreRepository: FirestoreRepository,
    private val authManager: FirebaseAuthManager
) {
    
    suspend fun migrateAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Migrar doctores
            migrateDoctors()
            
            // 2. Migrar especialidades
            migrateSpecialties()
            
            // 3. Los usuarios se migrarán cuando se registren/inicien sesión
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun migrateDoctors() {
        val doctors = roomRepository.getAllDoctorsSync() // Implementar método síncrono
        
        doctors.forEach { doctor ->
            val firestoreDoctor = doctor.toFirestoreDoctor()
            firestoreRepository.createDoctor(firestoreDoctor)
        }
    }
    
    private suspend fun migrateSpecialties() {
        val specialties = listOf(
            "Cardiología", "Pediatría", "Dermatología", "Neurología",
            "Ginecología", "Traumatología", "Psiquiatría", "Oftalmología"
        )
        
        specialties.forEach { specialty ->
            firestoreRepository.createSpecialty(
                Specialty(
                    id = specialty.lowercase().replace(" ", "_"),
                    nombre = specialty,
                    descripcion = "Especialidad médica en $specialty",
                    icono = "ic_${specialty.lowercase()}",
                    activa = true
                )
            )
        }
    }
    
    suspend fun migrateUserData(userId: String) {
        // Migrar datos específicos del usuario cuando inicie sesión
        val roomUserId = getRoomUserIdByEmail(userId) // Implementar mapeo
        
        if (roomUserId != null) {
            // Migrar citas del usuario
            migrateUserAppointments(roomUserId, userId)
            
            // Migrar calificaciones del usuario
            migrateUserRatings(roomUserId, userId)
            
            // Migrar notificaciones del usuario
            migrateUserNotifications(roomUserId, userId)
        }
    }
}
```

## Paso 9: Actualización de ViewModels

### 9.1 AuthViewModel con Firebase

```kotlin
// viewmodel/AuthViewModel.kt
class AuthViewModel(
    private val authManager: FirebaseAuthManager,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    init {
        // Observar cambios de autenticación
        viewModelScope.launch {
            authManager.getAuthStateFlow().collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            
            val result = authManager.signInWithEmail(email, password)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error de autenticación")
                }
            )
            
            _loading.value = false
        }
    }
    
    fun registerWithEmail(email: String, password: String, nombreCompleto: String) {
        viewModelScope.launch {
            _loading.value = true
            
            val result = authManager.registerWithEmail(email, password, nombreCompleto)
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error de registro")
                }
            )
            
            _loading.value = false
        }
    }
    
    fun signOut() {
        authManager.signOut()
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

## Paso 10: Configuración de Offline Support

### 10.1 Habilitar Persistencia Offline

```kotlin
// En Application class
class KardiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Habilitar persistencia offline de Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        
        FirebaseFirestore.getInstance().firestoreSettings = settings
        
        // Configurar Firebase Auth para persistencia
        FirebaseAuth.getInstance().useAppLanguage()
    }
}
```

## Paso 11: Testing con Firebase

### 11.1 Configurar Firebase Emulator

```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Inicializar proyecto
firebase init

# Configurar emulators
firebase init emulators

# Ejecutar emulators
firebase emulators:start
```

### 11.2 Tests con Emulator

```kotlin
// En androidTest
@RunWith(AndroidJUnit4::class)
class FirestoreRepositoryTest {
    
    @Before
    fun setup() {
        // Configurar emulator
        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        
        val auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)
    }
    
    @Test
    fun testCreateUser() = runTest {
        // Test implementation
    }
}
```

## Paso 12: Monitoreo y Analytics

### 12.1 Configurar Firebase Analytics

```kotlin
// En Activities principales
class MainActivity : AppCompatActivity() {
    private lateinit var analytics: FirebaseAnalytics
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        analytics = FirebaseAnalytics.getInstance(this)
        
        // Log eventos personalizados
        logScreenView("main_screen")
    }
    
    private fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, this@MainActivity.javaClass.simpleName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
```

## Consideraciones Importantes

### Costos de Firebase
- **Firestore**: Facturación por lecturas/escrituras/eliminaciones
- **Authentication**: Gratuito hasta cierto límite
- **Storage**: Si se implementan imágenes de perfil

### Seguridad
- Implementar reglas de seguridad robustas
- Validar datos en el cliente y servidor
- Usar HTTPS siempre

### Performance
- Implementar paginación para listas grandes
- Usar índices compuestos para consultas complejas
- Cachear datos frecuentemente accedidos

### Backup y Recuperación
- Configurar exportaciones automáticas
- Implementar estrategia de backup
- Probar procedimientos de recuperación

Esta migración transformará Kardia en una aplicación moderna, escalable y con sincronización en tiempo real, aprovechando todo el poder de Firebase.