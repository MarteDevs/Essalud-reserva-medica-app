# Guía de Instalación y Configuración - Kardia

## Requisitos del Sistema

### Requisitos Mínimos
- **Android Studio**: Arctic Fox (2020.3.1) o superior
- **JDK**: Java 11 o superior
- **Android SDK**: API Level 24 (Android 7.0) o superior
- **Gradle**: 7.0 o superior
- **Kotlin**: 1.8.0 o superior
- **RAM**: 8 GB mínimo, 16 GB recomendado
- **Espacio en disco**: 4 GB libres

### Requisitos del Dispositivo
- **Android**: 7.0 (API 24) o superior
- **RAM**: 2 GB mínimo
- **Almacenamiento**: 100 MB libres

## Instalación del Entorno de Desarrollo

### 1. Instalar Android Studio

1. Descargar Android Studio desde [developer.android.com](https://developer.android.com/studio)
2. Ejecutar el instalador y seguir las instrucciones
3. Configurar el SDK de Android:
   ```
   Tools > SDK Manager
   - Android SDK Platform 24 o superior
   - Android SDK Build-Tools 30.0.3 o superior
   - Android SDK Platform-Tools
   - Android SDK Tools
   ```

### 2. Configurar Variables de Entorno

#### Windows
```batch
set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
set PATH=%PATH%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools
```

#### macOS/Linux
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 3. Instalar Git
```bash
# Windows (usando Chocolatey)
choco install git

# macOS (usando Homebrew)
brew install git

# Ubuntu/Debian
sudo apt-get install git
```

## Configuración del Proyecto

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/kardia-app.git
cd kardia-app
```

### 2. Abrir en Android Studio
1. Abrir Android Studio
2. Seleccionar "Open an existing Android Studio project"
3. Navegar a la carpeta del proyecto y seleccionarla
4. Esperar a que Gradle sincronice el proyecto

### 3. Configurar Gradle

#### Verificar `gradle/wrapper/gradle-wrapper.properties`
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

#### Verificar `build.gradle.kts` (Project level)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}
```

### 4. Sincronizar Dependencias
```bash
# En Android Studio
File > Sync Project with Gradle Files

# O desde terminal
./gradlew build
```

## Configuración de la Base de Datos

### 1. Configuración Automática
La aplicación utiliza Room Database que se configura automáticamente:

```kotlin
// AppDatabase.kt ya está configurado
@Database(
    entities = [User::class, Doctor::class, Cita::class, Calificacion::class, Notificacion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Configuración automática
}
```

### 2. Datos Iniciales
La base de datos se puebla automáticamente con doctores de ejemplo:

```kotlin
private class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Se insertan doctores de ejemplo automáticamente
    }
}
```

## Configuración de Dependencias

### 1. Verificar `libs.versions.toml`
```toml
[versions]
agp = "8.5.1"
kotlin = "1.9.0"
coreKtx = "1.13.1"
# ... otras versiones
```

### 2. Dependencias Principales
Las siguientes dependencias se instalan automáticamente:

#### Core Android
- `androidx.core:core-ktx`
- `androidx.appcompat:appcompat`
- `com.google.android.material:material`

#### Architecture Components
- `androidx.lifecycle:lifecycle-viewmodel-ktx`
- `androidx.lifecycle:lifecycle-livedata-ktx`
- `androidx.navigation:navigation-fragment-ktx`

#### Database
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`

#### UI Components
- `androidx.constraintlayout:constraintlayout`
- `androidx.swiperefreshlayout:swiperefreshlayout`

## Configuración del Emulador

### 1. Crear AVD (Android Virtual Device)
```
Tools > AVD Manager > Create Virtual Device
- Seleccionar dispositivo (ej: Pixel 4)
- Seleccionar API Level 24 o superior
- Configurar RAM: 2048 MB mínimo
- Habilitar Hardware Acceleration
```

### 2. Configuración Recomendada del Emulador
```
- RAM: 4 GB
- VM Heap: 512 MB
- Internal Storage: 2 GB
- SD Card: 1 GB
- Graphics: Hardware - GLES 2.0
```

## Configuración de Dispositivo Físico

### 1. Habilitar Opciones de Desarrollador
1. Ir a `Configuración > Acerca del teléfono`
2. Tocar "Número de compilación" 7 veces
3. Volver a `Configuración > Opciones de desarrollador`
4. Habilitar "Depuración USB"

### 2. Conectar Dispositivo
```bash
# Verificar conexión
adb devices

# Debería mostrar tu dispositivo
List of devices attached
DEVICE_ID    device
```

## Compilación y Ejecución

### 1. Compilar el Proyecto
```bash
# Compilación debug
./gradlew assembleDebug

# Compilación release
./gradlew assembleRelease

# Ejecutar tests
./gradlew test
```

### 2. Instalar en Dispositivo
```bash
# Instalar APK debug
./gradlew installDebug

# O desde Android Studio
Run > Run 'app'
```

### 3. Generar APK Firmado

#### Crear Keystore
```bash
keytool -genkey -v -keystore kardia-release-key.keystore -alias kardia -keyalg RSA -keysize 2048 -validity 10000
```

#### Configurar `app/build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../kardia-release-key.keystore")
            storePassword = "tu_password"
            keyAlias = "kardia"
            keyPassword = "tu_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... otras configuraciones
        }
    }
}
```

## Solución de Problemas Comunes

### 1. Error de Sincronización de Gradle
```bash
# Limpiar proyecto
./gradlew clean

# Invalidar caché de Android Studio
File > Invalidate Caches and Restart
```

### 2. Error de Dependencias
```bash
# Actualizar dependencias
./gradlew --refresh-dependencies

# Verificar versiones en libs.versions.toml
```

### 3. Error de Room Database
```kotlin
// Verificar anotaciones en entidades
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // ... otros campos
)
```

### 4. Error de Permisos
```xml
<!-- Verificar AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

### 5. Error de Memoria
```
# En gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m
```

## Configuración de Desarrollo

### 1. Configurar Git Hooks
```bash
# Pre-commit hook para ejecutar tests
#!/bin/sh
./gradlew test
if [ $? -ne 0 ]; then
    echo "Tests failed"
    exit 1
fi
```

### 2. Configurar Lint
```kotlin
// En app/build.gradle.kts
android {
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}
```

### 3. Configurar ProGuard (Release)
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Scripts Útiles

### 1. Script de Compilación Completa
```bash
#!/bin/bash
echo "Limpiando proyecto..."
./gradlew clean

echo "Ejecutando tests..."
./gradlew test

echo "Compilando debug..."
./gradlew assembleDebug

echo "Instalando en dispositivo..."
./gradlew installDebug

echo "¡Compilación completada!"
```

### 2. Script de Release
```bash
#!/bin/bash
echo "Generando APK de release..."
./gradlew assembleRelease

echo "APK generado en: app/build/outputs/apk/release/"
```

## Configuración de CI/CD (Opcional)

### GitHub Actions
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Run tests
      run: ./gradlew test
```

## Verificación de Instalación

### 1. Checklist de Verificación
- [ ] Android Studio instalado y configurado
- [ ] SDK de Android instalado
- [ ] Proyecto sincronizado sin errores
- [ ] Emulador o dispositivo configurado
- [ ] Aplicación compila correctamente
- [ ] Aplicación se ejecuta sin errores
- [ ] Base de datos se inicializa correctamente
- [ ] Navegación funciona entre pantallas

### 2. Test de Funcionalidades Básicas
1. Abrir la aplicación
2. Registrar un nuevo usuario
3. Iniciar sesión
4. Navegar entre las diferentes secciones
5. Agendar una cita de prueba
6. Verificar notificaciones

¡La instalación está completa cuando todas las funcionalidades básicas funcionan correctamente!