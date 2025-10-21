# Arquitectura de la Aplicación Kardia

## Patrón Arquitectónico MVVM

La aplicación Kardia está construida siguiendo el patrón **MVVM (Model-View-ViewModel)** recomendado por Google para aplicaciones Android modernas.

### Componentes Principales

#### 1. Model (Modelo)
Representa la capa de datos y lógica de negocio:

**Entidades de Base de Datos:**
- `User`: Información de usuarios registrados
- `Doctor`: Datos de doctores disponibles
- `Cita`: Información de citas médicas
- `Calificacion`: Sistema de calificaciones y comentarios
- `Notificacion`: Notificaciones del sistema

**Data Access Objects (DAO):**
- `UserDao`: Operaciones CRUD para usuarios
- `DoctorDao`: Gestión de datos de doctores
- `CitaDao`: Manejo de citas médicas
- `CalificacionDao`: Operaciones de calificaciones
- `NotificacionDao`: Gestión de notificaciones

#### 2. View (Vista)
Componentes de interfaz de usuario:

**Activities:**
- `MainActivity`: Actividad principal con navegación
- `AuthActivity`: Gestión de autenticación
- `DoctorDetailActivity`: Detalles de doctores
- `ScheduleActivity`: Programación de citas

**Fragments:**
- `HomeFragment`: Pantalla principal con estadísticas
- `DoctorsFragment`: Lista y búsqueda de doctores
- `AppointmentsFragment`: Gestión de citas
- `ProfileFragment`: Perfil de usuario
- `NotificationsFragment`: Centro de notificaciones
- `LoginFragment` / `RegisterFragment`: Autenticación

#### 3. ViewModel
Intermediario entre View y Model:

- `HomeViewModel`: Lógica de la pantalla principal
- `DoctorsViewModel`: Gestión de lista de doctores
- `AppointmentsViewModel`: Manejo de citas
- `AuthViewModel`: Lógica de autenticación
- `ProfileViewModel`: Gestión de perfil
- `NotificationsViewModel`: Manejo de notificaciones

## Capa de Datos

### Repository Pattern

La aplicación implementa el patrón Repository para abstraer el acceso a datos:

```kotlin
class AppRepository(private val database: AppDatabase) {
    // Operaciones de usuarios
    suspend fun insertUser(user: User): Long
    suspend fun getUserByEmail(email: String): User?
    
    // Operaciones de doctores
    fun getAllDoctors(): LiveData<List<Doctor>>
    suspend fun getDoctorById(id: Int): Doctor?
    
    // Operaciones de citas
    fun getCitasWithDoctorByUserId(userId: Int): LiveData<List<CitaWithDoctorInfo>>
    suspend fun insertCita(cita: Cita): Long
    
    // Operaciones de calificaciones
    suspend fun insertCalificacion(calificacion: Calificacion): Long
    fun getCalificacionesByDoctorId(doctorId: Int): LiveData<List<Calificacion>>
    
    // Operaciones de notificaciones
    fun getNotificacionesByUserId(userId: Int): LiveData<List<Notificacion>>
    suspend fun insertNotificacion(notificacion: Notificacion): Long
}
```

### Base de Datos Room

**Configuración de la Base de Datos:**
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
- **Singleton Pattern**: Una sola instancia de la base de datos
- **Type Converters**: Para manejo de fechas y tipos complejos
- **Foreign Keys**: Relaciones entre entidades
- **Indices**: Para optimización de consultas
- **Callback de inicialización**: Población de datos de ejemplo

## Gestión de Estado

### LiveData y Observadores

La aplicación utiliza **LiveData** para la observación reactiva de datos:

```kotlin
class HomeViewModel : ViewModel() {
    private val _stats = MutableLiveData<HomeStats>()
    val stats: LiveData<HomeStats> = _stats
    
    fun loadStats() {
        viewModelScope.launch {
            // Cargar estadísticas
        }
    }
}
```

### Coroutines para Operaciones Asíncronas

Todas las operaciones de base de datos utilizan **Kotlin Coroutines**:

```kotlin
viewModelScope.launch {
    try {
        val result = repository.insertCita(cita)
        // Manejar resultado
    } catch (e: Exception) {
        // Manejar error
    }
}
```

## Navegación

### Navigation Component

La aplicación utiliza el **Navigation Component** de Android:

**Grafos de Navegación:**
- `auth_nav_graph.xml`: Flujo de autenticación
- `main_nav_graph.xml`: Navegación principal

**Características:**
- **Safe Args**: Paso seguro de argumentos
- **Deep Links**: Enlaces profundos
- **Animaciones**: Transiciones entre pantallas
- **Back Stack**: Gestión automática de la pila de navegación

## Inyección de Dependencias

### ViewModelFactory

Para la creación de ViewModels con dependencias:

```kotlin
class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            // Otros ViewModels...
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
```

## Gestión de Sesiones

### SessionManager

Utilidad para manejo de sesiones de usuario:

```kotlin
class SessionManager(context: Context) {
    fun saveUserSession(userId: Int, userName: String, userEmail: String)
    fun isLoggedIn(): Boolean
    fun getUserId(): Int
    fun getUserName(): String
    fun getUserEmail(): String
    fun clearSession()
}
```

## Principios de Diseño Aplicados

### 1. Separación de Responsabilidades
- **UI**: Solo manejo de interfaz
- **ViewModel**: Lógica de presentación
- **Repository**: Acceso a datos
- **Database**: Persistencia

### 2. Inversión de Dependencias
- ViewModels dependen de abstracciones (Repository)
- Repository abstrae el acceso a datos

### 3. Single Responsibility
- Cada clase tiene una responsabilidad específica
- Fragmentos enfocados en una funcionalidad

### 4. Observador (Observer Pattern)
- LiveData para comunicación reactiva
- Observación de cambios en datos

## Flujo de Datos

```
View (Fragment/Activity)
    ↓ (User Action)
ViewModel
    ↓ (Business Logic)
Repository
    ↓ (Data Access)
Database/DAO
    ↓ (Data Response)
Repository
    ↓ (LiveData)
ViewModel
    ↓ (UI State)
View (UI Update)
```

## Ventajas de esta Arquitectura

1. **Testabilidad**: Fácil testing de componentes individuales
2. **Mantenibilidad**: Código organizado y fácil de mantener
3. **Escalabilidad**: Fácil agregar nuevas funcionalidades
4. **Reutilización**: Componentes reutilizables
5. **Separación de responsabilidades**: Cada capa tiene su función específica
6. **Gestión de configuración**: Supervivencia a cambios de configuración
7. **Programación reactiva**: Actualizaciones automáticas de UI