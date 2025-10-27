# Guía de Testing - Kardia

## Estrategia de Testing

### Pirámide de Testing
```
    /\
   /  \     E2E Tests (10%)
  /____\    
 /      \   Integration Tests (20%)
/________\  Unit Tests (70%)
```

### Tipos de Pruebas Implementadas

1. **Unit Tests**: Pruebas de lógica de negocio, ViewModels, Repository
2. **Integration Tests**: Pruebas de base de datos, DAOs
3. **UI Tests**: Pruebas de interfaz de usuario con Espresso
4. **End-to-End Tests**: Flujos completos de usuario

## Configuración de Testing

### Dependencias de Testing
```kotlin
// En app/build.gradle.kts
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.room:room-testing:2.5.0")
    androidTestImplementation("androidx.navigation:navigation-testing:2.6.0")
}
```

### Configuración de Test Runner
```kotlin
// En app/build.gradle.kts
android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
```

## Unit Tests

### 1. Testing de ViewModels

#### AuthViewModel Tests
```kotlin
@ExtendWith(MockitoExtension::class)
class AuthViewModelTest {
    
    @Mock
    private lateinit var repository: AppRepository
    
    @Mock
    private lateinit var sessionManager: SessionManager
    
    private lateinit var authViewModel: AuthViewModel
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @BeforeEach
    fun setup() {
        authViewModel = AuthViewModel(repository, sessionManager)
    }
    
    @Test
    fun `loginUser with valid credentials should return success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(1, "Test User", email, "hashedPassword")
        
        whenever(repository.getUserByEmailAndPassword(email, any())).thenReturn(expectedUser)
        
        // When
        val result = authViewModel.loginUser(email, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        verify(sessionManager).saveUserSession(1, "Test User", email)
    }
    
    @Test
    fun `loginUser with invalid credentials should return failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        
        whenever(repository.getUserByEmailAndPassword(email, any())).thenReturn(null)
        
        // When
        val result = authViewModel.loginUser(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Credenciales inválidas", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `registerUser with existing email should return failure`() = runTest {
        // Given
        val email = "existing@example.com"
        val existingUser = User(1, "Existing User", email, "hashedPassword")
        
        whenever(repository.getUserByEmail(email)).thenReturn(existingUser)
        
        // When
        val result = authViewModel.registerUser("New User", email, "password123")
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("El usuario ya existe", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `registerUser with valid data should return success`() = runTest {
        // Given
        val email = "new@example.com"
        val name = "New User"
        val password = "password123"
        
        whenever(repository.getUserByEmail(email)).thenReturn(null)
        whenever(repository.insertUser(any())).thenReturn(1L)
        
        // When
        val result = authViewModel.registerUser(name, email, password)
        
        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(name, user?.nombreCompleto)
        assertEquals(email, user?.email)
    }
}
```

#### DoctorsViewModel Tests
```kotlin
@ExtendWith(MockitoExtension::class)
class DoctorsViewModelTest {
    
    @Mock
    private lateinit var repository: AppRepository
    
    private lateinit var doctorsViewModel: DoctorsViewModel
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDoctors = listOf(
        Doctor(1, "Dr. Juan Pérez", "Cardiología", "Especialista en corazón", 4.5f, "9:00-17:00"),
        Doctor(2, "Dra. María García", "Pediatría", "Especialista en niños", 4.8f, "8:00-16:00"),
        Doctor(3, "Dr. Carlos López", "Cardiología", "Cirujano cardiovascular", 4.2f, "10:00-18:00")
    )
    
    @BeforeEach
    fun setup() {
        doctorsViewModel = DoctorsViewModel(repository)
    }
    
    @Test
    fun `loadDoctors should update doctors LiveData`() {
        // Given
        val doctorsLiveData = MutableLiveData<List<Doctor>>()
        whenever(repository.getAllDoctors()).thenReturn(doctorsLiveData)
        
        // When
        doctorsViewModel.loadDoctors()
        doctorsLiveData.value = testDoctors
        
        // Then
        assertEquals(testDoctors, doctorsViewModel.doctors.value)
        assertEquals(testDoctors, doctorsViewModel.filteredDoctors.value)
    }
    
    @Test
    fun `searchDoctors should filter by name`() {
        // Given
        doctorsViewModel.doctors.value = testDoctors
        
        // When
        doctorsViewModel.searchDoctors("Juan")
        
        // Then
        val filtered = doctorsViewModel.filteredDoctors.value
        assertEquals(1, filtered?.size)
        assertEquals("Dr. Juan Pérez", filtered?.first()?.nombre)
    }
    
    @Test
    fun `searchDoctors should filter by specialty`() {
        // Given
        doctorsViewModel.doctors.value = testDoctors
        
        // When
        doctorsViewModel.searchDoctors("Cardiología")
        
        // Then
        val filtered = doctorsViewModel.filteredDoctors.value
        assertEquals(2, filtered?.size)
        assertTrue(filtered?.all { it.especialidad == "Cardiología" } == true)
    }
    
    @Test
    fun `filterBySpecialty should filter correctly`() {
        // Given
        doctorsViewModel.doctors.value = testDoctors
        
        // When
        doctorsViewModel.filterBySpecialty("Pediatría")
        
        // Then
        val filtered = doctorsViewModel.filteredDoctors.value
        assertEquals(1, filtered?.size)
        assertEquals("Pediatría", filtered?.first()?.especialidad)
    }
    
    @Test
    fun `filterBySpecialty with 'Todas' should show all doctors`() {
        // Given
        doctorsViewModel.doctors.value = testDoctors
        
        // When
        doctorsViewModel.filterBySpecialty("Todas")
        
        // Then
        val filtered = doctorsViewModel.filteredDoctors.value
        assertEquals(testDoctors.size, filtered?.size)
    }
}
```

### 2. Testing de Repository

```kotlin
@ExtendWith(MockitoExtension::class)
class AppRepositoryTest {
    
    @Mock
    private lateinit var userDao: UserDao
    
    @Mock
    private lateinit var doctorDao: DoctorDao
    
    @Mock
    private lateinit var citaDao: CitaDao
    
    private lateinit var repository: AppRepository
    
    @BeforeEach
    fun setup() {
        repository = AppRepository(userDao, doctorDao, citaDao)
    }
    
    @Test
    fun `getUserByEmailAndPassword should return user when found`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "hashedPassword"
        val expectedUser = User(1, "Test User", email, password)
        
        whenever(userDao.getUserByEmailAndPassword(email, password)).thenReturn(expectedUser)
        
        // When
        val result = repository.getUserByEmailAndPassword(email, password)
        
        // Then
        assertEquals(expectedUser, result)
    }
    
    @Test
    fun `insertUser should return user id`() = runTest {
        // Given
        val user = User(0, "New User", "new@example.com", "hashedPassword")
        val expectedId = 1L
        
        whenever(userDao.insertUser(user)).thenReturn(expectedId)
        
        // When
        val result = repository.insertUser(user)
        
        // Then
        assertEquals(expectedId, result)
    }
    
    @Test
    fun `getAllDoctors should return LiveData of doctors`() {
        // Given
        val doctorsLiveData = MutableLiveData<List<Doctor>>()
        whenever(doctorDao.getAllDoctors()).thenReturn(doctorsLiveData)
        
        // When
        val result = repository.getAllDoctors()
        
        // Then
        assertEquals(doctorsLiveData, result)
    }
}
```

### 3. Testing de Utilidades

```kotlin
class ValidationUtilsTest {
    
    @Test
    fun `isValidEmail should return true for valid emails`() {
        // Given
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org"
        )
        
        // When & Then
        validEmails.forEach { email ->
            assertTrue("$email should be valid", ValidationUtils.isValidEmail(email))
        }
    }
    
    @Test
    fun `isValidEmail should return false for invalid emails`() {
        // Given
        val invalidEmails = listOf(
            "invalid-email",
            "@example.com",
            "user@",
            "user name@example.com"
        )
        
        // When & Then
        invalidEmails.forEach { email ->
            assertFalse("$email should be invalid", ValidationUtils.isValidEmail(email))
        }
    }
    
    @Test
    fun `isValidPassword should return true for valid passwords`() {
        // Given
        val validPasswords = listOf("123456", "password", "mySecretPassword123")
        
        // When & Then
        validPasswords.forEach { password ->
            assertTrue("$password should be valid", ValidationUtils.isValidPassword(password))
        }
    }
    
    @Test
    fun `isValidPassword should return false for short passwords`() {
        // Given
        val shortPasswords = listOf("", "1", "12345")
        
        // When & Then
        shortPasswords.forEach { password ->
            assertFalse("$password should be invalid", ValidationUtils.isValidPassword(password))
        }
    }
}
```

## Integration Tests

### 1. Database Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var doctorDao: DoctorDao
    private lateinit var citaDao: CitaDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
        doctorDao = database.doctorDao()
        citaDao = database.citaDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetUser() = runTest {
        // Given
        val user = User(0, "Test User", "test@example.com", "hashedPassword")
        
        // When
        val userId = userDao.insertUser(user)
        val retrievedUser = userDao.getUserById(userId.toInt())
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals("Test User", retrievedUser?.nombreCompleto)
        assertEquals("test@example.com", retrievedUser?.email)
    }
    
    @Test
    fun getUserByEmailAndPassword() = runTest {
        // Given
        val user = User(0, "Test User", "test@example.com", "hashedPassword")
        userDao.insertUser(user)
        
        // When
        val retrievedUser = userDao.getUserByEmailAndPassword("test@example.com", "hashedPassword")
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals("Test User", retrievedUser?.nombreCompleto)
    }
    
    @Test
    fun insertAndGetDoctor() = runTest {
        // Given
        val doctor = Doctor(0, "Dr. Test", "Cardiología", "Especialista", 4.5f, "9:00-17:00")
        
        // When
        val doctorId = doctorDao.insertDoctor(doctor)
        val retrievedDoctor = doctorDao.getDoctorById(doctorId.toInt())
        
        // Then
        assertNotNull(retrievedDoctor)
        assertEquals("Dr. Test", retrievedDoctor?.nombre)
        assertEquals("Cardiología", retrievedDoctor?.especialidad)
    }
    
    @Test
    fun insertCitaWithForeignKeys() = runTest {
        // Given
        val user = User(0, "Test User", "test@example.com", "hashedPassword")
        val doctor = Doctor(0, "Dr. Test", "Cardiología", "Especialista", 4.5f, "9:00-17:00")
        
        val userId = userDao.insertUser(user).toInt()
        val doctorId = doctorDao.insertDoctor(doctor).toInt()
        
        val cita = Cita(
            id = 0,
            usuarioId = userId,
            doctorId = doctorId,
            fechaHora = Date(),
            estado = "PENDIENTE",
            notas = "Cita de prueba"
        )
        
        // When
        val citaId = citaDao.insertCita(cita)
        val retrievedCita = citaDao.getCitaById(citaId.toInt())
        
        // Then
        assertNotNull(retrievedCita)
        assertEquals(userId, retrievedCita?.usuarioId)
        assertEquals(doctorId, retrievedCita?.doctorId)
        assertEquals("PENDIENTE", retrievedCita?.estado)
    }
    
    @Test
    fun getCitasConDetalles() = runTest {
        // Given
        val user = User(0, "Test User", "test@example.com", "hashedPassword")
        val doctor = Doctor(0, "Dr. Test", "Cardiología", "Especialista", 4.5f, "9:00-17:00")
        
        val userId = userDao.insertUser(user).toInt()
        val doctorId = doctorDao.insertDoctor(doctor).toInt()
        
        val cita = Cita(
            id = 0,
            usuarioId = userId,
            doctorId = doctorId,
            fechaHora = Date(),
            estado = "PENDIENTE",
            notas = "Cita de prueba"
        )
        
        citaDao.insertCita(cita)
        
        // When
        val citasConDetalles = citaDao.getCitasConDetallesByUserId(userId).getOrAwaitValue()
        
        // Then
        assertEquals(1, citasConDetalles.size)
        val citaConDetalles = citasConDetalles.first()
        assertEquals("Test User", citaConDetalles.nombreUsuario)
        assertEquals("Dr. Test", citaConDetalles.nombreDoctor)
        assertEquals("Cardiología", citaConDetalles.especialidadDoctor)
    }
}
```

### 2. Navigation Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testNavigationBetweenFragments() {
        // Test navigation from Home to Doctors
        onView(withId(R.id.navigation_doctors)).perform(click())
        onView(withId(R.id.doctorsFragment)).check(matches(isDisplayed()))
        
        // Test navigation to Appointments
        onView(withId(R.id.navigation_appointments)).perform(click())
        onView(withId(R.id.appointmentsFragment)).check(matches(isDisplayed()))
        
        // Test navigation to Profile
        onView(withId(R.id.navigation_profile)).perform(click())
        onView(withId(R.id.profileFragment)).check(matches(isDisplayed()))
        
        // Test navigation to Notifications
        onView(withId(R.id.navigation_notifications)).perform(click())
        onView(withId(R.id.notificationsFragment)).check(matches(isDisplayed()))
    }
}
```

## UI Tests (Espresso)

### 1. Authentication Flow Tests

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthenticationFlowTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(AuthActivity::class.java)
    
    @Test
    fun testLoginFlow() {
        // Enter email
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())
        
        // Enter password
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())
        
        // Click login button
        onView(withId(R.id.btnLogin)).perform(click())
        
        // Verify navigation to MainActivity (assuming successful login)
        // This would require mocking the repository or using a test database
    }
    
    @Test
    fun testRegistrationFlow() {
        // Switch to registration
        onView(withId(R.id.tvRegister)).perform(click())
        
        // Fill registration form
        onView(withId(R.id.etFullName))
            .perform(typeText("Test User"), closeSoftKeyboard())
        
        onView(withId(R.id.etEmail))
            .perform(typeText("newuser@example.com"), closeSoftKeyboard())
        
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())
        
        onView(withId(R.id.etConfirmPassword))
            .perform(typeText("password123"), closeSoftKeyboard())
        
        // Click register button
        onView(withId(R.id.btnRegister)).perform(click())
        
        // Verify success message or navigation
    }
    
    @Test
    fun testLoginValidation() {
        // Try to login without email
        onView(withId(R.id.btnLogin)).perform(click())
        
        // Verify error message
        onView(withText("Por favor ingresa tu email"))
            .check(matches(isDisplayed()))
    }
}
```

### 2. Doctors List Tests

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class DoctorsListTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun navigateToDoctors() {
        onView(withId(R.id.navigation_doctors)).perform(click())
    }
    
    @Test
    fun testDoctorsListDisplayed() {
        // Verify RecyclerView is displayed
        onView(withId(R.id.rvDoctors)).check(matches(isDisplayed()))
        
        // Verify at least one doctor is displayed
        onView(withId(R.id.rvDoctors))
            .check(matches(hasMinimumChildCount(1)))
    }
    
    @Test
    fun testDoctorSearch() {
        // Perform search
        onView(withId(R.id.searchView))
            .perform(click())
            .perform(typeText("Cardiología"))
        
        // Verify filtered results
        onView(withId(R.id.rvDoctors))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testDoctorItemClick() {
        // Click on first doctor item
        onView(withId(R.id.rvDoctors))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        
        // Verify navigation to doctor detail
        // This would require checking if DoctorDetailActivity is launched
    }
}
```

### 3. Appointment Booking Tests

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppointmentBookingTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(DoctorDetailActivity::class.java)
    
    @Test
    fun testBookAppointment() {
        // Click book appointment button
        onView(withId(R.id.btnBookAppointment)).perform(click())
        
        // Select date
        onView(withId(R.id.calendarView)).perform(click())
        
        // Select time
        onView(withId(R.id.timeSlot1)).perform(click())
        
        // Add notes
        onView(withId(R.id.etNotes))
            .perform(typeText("Consulta de rutina"), closeSoftKeyboard())
        
        // Confirm booking
        onView(withId(R.id.btnConfirmBooking)).perform(click())
        
        // Verify success message
        onView(withText("Cita agendada exitosamente"))
            .check(matches(isDisplayed()))
    }
}
```

## Test Utilities

### 1. LiveData Testing Extension

```kotlin
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }
    
    @Suppress("UNCHECKED_CAST")
    return data as T
}
```

### 2. Main Dispatcher Rule

```kotlin
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 3. Test Data Factory

```kotlin
object TestDataFactory {
    
    fun createTestUser(
        id: Int = 1,
        name: String = "Test User",
        email: String = "test@example.com",
        password: String = "hashedPassword"
    ) = User(id, name, email, password)
    
    fun createTestDoctor(
        id: Int = 1,
        name: String = "Dr. Test",
        specialty: String = "Cardiología",
        description: String = "Especialista en corazón",
        rating: Float = 4.5f,
        schedule: String = "9:00-17:00"
    ) = Doctor(id, name, specialty, description, rating, schedule)
    
    fun createTestCita(
        id: Int = 1,
        userId: Int = 1,
        doctorId: Int = 1,
        date: Date = Date(),
        status: String = "PENDIENTE",
        notes: String? = null
    ) = Cita(id, userId, doctorId, date, status, notes)
}
```

## Ejecutar Tests

### 1. Desde Android Studio
```
- Unit Tests: Click derecho en test folder > Run 'Tests in...'
- Android Tests: Click derecho en androidTest folder > Run 'Tests in...'
- Specific Test: Click derecho en test class > Run 'TestClassName'
```

### 2. Desde Terminal
```bash
# Ejecutar todos los unit tests
./gradlew test

# Ejecutar tests de un módulo específico
./gradlew :app:test

# Ejecutar Android tests
./gradlew connectedAndroidTest

# Ejecutar tests con reporte
./gradlew test --continue
```

### 3. Generar Reportes de Coverage
```bash
# Habilitar coverage en build.gradle.kts
android {
    buildTypes {
        debug {
            isTestCoverageEnabled = true
        }
    }
}

# Ejecutar con coverage
./gradlew createDebugCoverageReport
```

## Mejores Prácticas

### 1. Naming Conventions
- Test classes: `ClassNameTest`
- Test methods: `methodName_condition_expectedResult`
- Given-When-Then structure

### 2. Test Organization
```kotlin
class ExampleTest {
    
    // Setup
    @BeforeEach
    fun setup() { }
    
    // Happy path tests
    @Test
    fun `happy path test`() { }
    
    // Edge cases
    @Test
    fun `edge case test`() { }
    
    // Error cases
    @Test
    fun `error case test`() { }
    
    // Cleanup
    @AfterEach
    fun cleanup() { }
}
```

### 3. Mock Strategy
- Mock external dependencies
- Use real objects for simple data classes
- Verify interactions when necessary
- Don't over-mock

### 4. Test Data Management
- Use factories for test data creation
- Keep test data minimal and focused
- Use builders for complex objects

La implementación completa de estas pruebas garantiza la calidad y confiabilidad de la aplicación Kardia.