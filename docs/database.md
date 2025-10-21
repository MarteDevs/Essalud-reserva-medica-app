# Modelo de Base de Datos - Kardia

## Descripción General

La aplicación Kardia utiliza **Room Database** como sistema de persistencia local. La base de datos está diseñada para manejar usuarios, doctores, citas médicas, calificaciones y notificaciones de manera eficiente y relacional.

## Configuración de la Base de Datos

```kotlin
@Database(
    entities = [User::class, Doctor::class, Cita::class, Calificacion::class, Notificacion::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase()
```

**Características:**
- **Nombre**: `essalud_database`
- **Versión**: 3
- **Migración**: Destructiva (para desarrollo)
- **Type Converters**: Manejo de fechas (`Date` ↔ `Long`)

## Entidades de la Base de Datos

### 1. User (Usuarios)

```kotlin
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreCompleto: String,
    val email: String,
    val password: String
)
```

**Características:**
- **Tabla**: `users`
- **Clave primaria**: `id` (auto-generada)
- **Índice único**: `email`
- **Campos obligatorios**: Todos los campos son requeridos

**Propósito**: Almacenar información de usuarios registrados en la aplicación.

### 2. Doctor (Doctores)

```kotlin
@Entity(tableName = "doctores")
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val especialidad: String,
    val experiencia: String,
    val disponibilidad: String,
    val foto: String? = null
)
```

**Características:**
- **Tabla**: `doctores`
- **Clave primaria**: `id` (auto-generada)
- **Campo opcional**: `foto`
- **Especialidades**: Cardiología, Neurología, Pediatría, etc.

**Propósito**: Catálogo de doctores disponibles para citas médicas.

### 3. Cita (Citas Médicas)

```kotlin
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["doctorId"])
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val doctorId: Int,
    val fechaHora: Date,
    val estado: String,
    val notas: String? = null
)
```

**Características:**
- **Tabla**: `citas`
- **Relaciones**: Usuario (CASCADE), Doctor (CASCADE)
- **Índices**: `usuarioId`, `doctorId`
- **Estados posibles**: "Confirmada", "Cancelada", "Completada", "Pendiente", "Reprogramada"

**Propósito**: Gestionar las citas médicas entre usuarios y doctores.

### 4. Calificacion (Calificaciones)

```kotlin
@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(entity = User::class, ...),
        ForeignKey(entity = Doctor::class, ...),
        ForeignKey(entity = Cita::class, ...)
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["doctorId"]),
        Index(value = ["citaId"], unique = true)
    ]
)
data class Calificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val doctorId: Int,
    val citaId: Int,
    val puntuacion: Float, // 1.0 a 5.0
    val comentario: String? = null,
    val fechaCalificacion: Date = Date()
)
```

**Características:**
- **Tabla**: `calificaciones`
- **Relaciones**: Usuario, Doctor, Cita (todas CASCADE)
- **Restricción**: Una calificación por cita (índice único en `citaId`)
- **Puntuación**: Rango de 1.0 a 5.0 estrellas

**Propósito**: Sistema de calificaciones y comentarios sobre la atención médica.

### 5. Notificacion (Notificaciones)

```kotlin
@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["fechaCreacion"])
    ]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val leida: Boolean = false,
    val fechaCreacion: Date = Date(),
    val citaId: Int? = null
)
```

**Características:**
- **Tabla**: `notificaciones`
- **Relación**: Usuario (CASCADE)
- **Índices**: `usuarioId`, `fechaCreacion`
- **Tipos**: "CITA_CONFIRMADA", "CITA_CANCELADA", "RECORDATORIO", "GENERAL"

**Propósito**: Sistema de notificaciones para usuarios.

## Enumeraciones

### EstadoCita

```kotlin
enum class EstadoCita(val displayName: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    REPROGRAMADA("Reprogramada")
}
```

## Data Access Objects (DAO)

### UserDao
```kotlin
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
}
```

### DoctorDao
```kotlin
interface DoctorDao {
    @Query("SELECT * FROM doctores ORDER BY nombre ASC")
    fun getAllDoctors(): LiveData<List<Doctor>>
    
    @Query("SELECT * FROM doctores WHERE id = :id")
    suspend fun getDoctorById(id: Int): Doctor?
    
    @Query("SELECT COUNT(*) FROM doctores")
    suspend fun getDoctorCount(): Int
    
    @Insert
    suspend fun insertDoctor(doctor: Doctor): Long
}
```

### CitaDao
```kotlin
interface CitaDao {
    @Query("""
        SELECT c.*, d.nombre as doctorNombre, d.especialidad as doctorEspecialidad 
        FROM citas c 
        INNER JOIN doctores d ON c.doctorId = d.id 
        WHERE c.usuarioId = :userId 
        ORDER BY c.fechaHora DESC
    """)
    fun getCitasWithDoctorByUserId(userId: Int): LiveData<List<CitaWithDoctorInfo>>
    
    @Insert
    suspend fun insertCita(cita: Cita): Long
    
    @Update
    suspend fun updateCita(cita: Cita)
    
    @Query("UPDATE citas SET estado = :estado WHERE id = :citaId")
    suspend fun updateCitaEstado(citaId: Int, estado: String)
}
```

## Relaciones y Consultas Complejas

### CitaWithDoctorInfo
```kotlin
data class CitaWithDoctorInfo(
    val id: Int,
    val usuarioId: Int,
    val doctorId: Int,
    val fechaHora: Date,
    val estado: String,
    val notas: String?,
    val doctorNombre: String,
    val doctorEspecialidad: String
)
```

### CalificacionConDetalles
```kotlin
data class CalificacionConDetalles(
    val id: Int,
    val puntuacion: Float,
    val comentario: String?,
    val fechaCalificacion: Date,
    val usuarioNombre: String,
    val doctorNombre: String
)
```

## Inicialización de Datos

La base de datos se inicializa con datos de ejemplo:

```kotlin
private suspend fun populateDatabase(doctorDao: DoctorDao) {
    val doctores = listOf(
        Doctor(nombre = "Dr. Carlos Mendoza", especialidad = "Cardiología", ...),
        Doctor(nombre = "Dra. Ana García", especialidad = "Neurología", ...),
        Doctor(nombre = "Dr. Luis Rodríguez", especialidad = "Pediatría", ...),
        // ... más doctores
    )
    
    doctores.forEach { doctor ->
        doctorDao.insertDoctor(doctor)
    }
}
```

## Optimizaciones

### Índices
- **Usuarios**: Índice único en `email`
- **Citas**: Índices en `usuarioId` y `doctorId`
- **Calificaciones**: Índices en `usuarioId`, `doctorId`, y único en `citaId`
- **Notificaciones**: Índices en `usuarioId` y `fechaCreacion`

### Foreign Keys con CASCADE
- Eliminación automática de datos relacionados
- Integridad referencial garantizada

### Type Converters
```kotlin
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

## Consultas Principales

1. **Obtener citas con información del doctor**
2. **Buscar doctores por especialidad**
3. **Calcular promedio de calificaciones por doctor**
4. **Obtener notificaciones no leídas**
5. **Historial de citas por usuario**
6. **Estadísticas de citas por estado**

## Consideraciones de Rendimiento

- **Consultas asíncronas**: Todas las operaciones de escritura son `suspend`
- **LiveData**: Para observación reactiva sin bloqueo
- **Índices estratégicos**: En campos de búsqueda frecuente
- **Paginación**: Para listas grandes (implementable con Paging 3)

## Migración de Base de Datos

Actualmente configurada con `fallbackToDestructiveMigration()` para desarrollo. En producción se implementarían migraciones específicas:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
```