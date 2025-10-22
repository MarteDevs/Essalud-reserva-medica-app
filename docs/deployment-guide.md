# Guía de Despliegue - Kardia

## Estrategia de Despliegue

### Entornos de Despliegue
1. **Desarrollo (Development)**: Para pruebas internas
2. **Staging**: Para pruebas de aceptación
3. **Producción (Production)**: Para usuarios finales

### Tipos de Distribución
- **Internal Testing**: Distribución interna para el equipo
- **Alpha Testing**: Pruebas cerradas con usuarios seleccionados
- **Beta Testing**: Pruebas abiertas con grupo amplio de usuarios
- **Production Release**: Lanzamiento público en Google Play Store

## Configuración de Build Variants

### 1. Configurar Build Types
```kotlin
// En app/build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false
            manifestPlaceholders["appName"] = "Kardia Debug"
        }
        
        create("staging") {
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            isDebuggable = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appName"] = "Kardia Staging"
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appName"] = "Kardia"
        }
    }
}
```

### 2. Configurar Product Flavors
```kotlin
android {
    flavorDimensions += "environment"
    
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.kardia.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.kardia.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
    }
}
```

## Configuración de Signing

### 1. Crear Keystore
```bash
keytool -genkey -v -keystore kardia-release-key.keystore \
    -alias kardia \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass YOUR_STORE_PASSWORD \
    -keypass YOUR_KEY_PASSWORD \
    -dname "CN=Kardia, OU=Medical, O=YourCompany, L=YourCity, S=YourState, C=YourCountry"
```

### 2. Configurar Signing en Gradle
```kotlin
// En app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/kardia-release-key.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_store_password"
            keyAlias = "kardia"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_key_password"
        }
        
        create("staging") {
            storeFile = file("../keystore/kardia-staging-key.keystore")
            storePassword = System.getenv("STAGING_KEYSTORE_PASSWORD") ?: "staging_password"
            keyAlias = "kardia-staging"
            keyPassword = System.getenv("STAGING_KEY_PASSWORD") ?: "staging_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
        
        getByName("staging") {
            signingConfig = signingConfigs.getByName("staging")
        }
    }
}
```

### 3. Configurar Variables de Entorno
```bash
# En .env o variables del sistema
export KEYSTORE_PASSWORD="your_secure_store_password"
export KEY_PASSWORD="your_secure_key_password"
export STAGING_KEYSTORE_PASSWORD="staging_password"
export STAGING_KEY_PASSWORD="staging_password"
```

## Configuración de ProGuard/R8

### 1. Reglas de ProGuard
```proguard
# En proguard-rules.pro

# Keep Room entities
-keep class com.yourpackage.data.entities.** { *; }

# Keep Retrofit interfaces
-keep interface com.yourpackage.network.** { *; }

# Keep ViewModels
-keep class com.yourpackage.viewmodel.** { *; }

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Room specific rules
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
```

### 2. Configuración de R8
```kotlin
// En app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}
```

## Scripts de Build

### 1. Script de Build Completo
```bash
#!/bin/bash
# build-release.sh

echo "🚀 Iniciando build de release para Kardia..."

# Limpiar proyecto
echo "🧹 Limpiando proyecto..."
./gradlew clean

# Ejecutar tests
echo "🧪 Ejecutando tests..."
./gradlew test
if [ $? -ne 0 ]; then
    echo "❌ Tests fallaron. Abortando build."
    exit 1
fi

# Ejecutar lint
echo "🔍 Ejecutando análisis de código..."
./gradlew lint
if [ $? -ne 0 ]; then
    echo "⚠️ Lint encontró problemas. Revisa el reporte."
fi

# Build release
echo "📦 Generando APK de release..."
./gradlew assembleRelease

# Build AAB para Play Store
echo "📱 Generando AAB para Play Store..."
./gradlew bundleRelease

# Verificar archivos generados
if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "✅ APK generado: app/build/outputs/apk/release/app-release.apk"
fi

if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "✅ AAB generado: app/build/outputs/bundle/release/app-release.aab"
fi

echo "🎉 Build completado exitosamente!"
```

### 2. Script de Build por Entorno
```bash
#!/bin/bash
# build-environment.sh

ENVIRONMENT=$1

if [ -z "$ENVIRONMENT" ]; then
    echo "Uso: ./build-environment.sh [dev|staging|prod]"
    exit 1
fi

case $ENVIRONMENT in
    "dev")
        echo "🔧 Building para desarrollo..."
        ./gradlew assembleDevDebug
        ;;
    "staging")
        echo "🎭 Building para staging..."
        ./gradlew assembleProdStaging
        ;;
    "prod")
        echo "🚀 Building para producción..."
        ./gradlew assembleProdRelease
        ./gradlew bundleProdRelease
        ;;
    *)
        echo "❌ Entorno no válido: $ENVIRONMENT"
        exit 1
        ;;
esac
```

## Configuración de CI/CD

### 1. GitHub Actions
```yaml
# .github/workflows/android.yml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  release:
    types: [ published ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Run lint
      run: ./gradlew lint
      
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: app/build/reports/tests/
        
    - name: Upload lint results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: lint-results
        path: app/build/reports/lint-results.html

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.event_name == 'push' || github.event_name == 'release'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Decode Keystore
      env:
        ENCODED_STRING: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        echo $ENCODED_STRING | base64 -di > keystore/kardia-release-key.keystore
        
    - name: Build Release APK
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew assembleRelease
      
    - name: Build Release AAB
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew bundleRelease
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: release-apk
        path: app/build/outputs/apk/release/app-release.apk
        
    - name: Upload AAB
      uses: actions/upload-artifact@v3
      with:
        name: release-aab
        path: app/build/outputs/bundle/release/app-release.aab

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.event_name == 'release'
    
    steps:
    - name: Download AAB
      uses: actions/download-artifact@v3
      with:
        name: release-aab
        
    - name: Deploy to Play Store
      uses: r0adkll/upload-google-play@v1
      with:
        serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT }}
        packageName: com.yourpackage.kardia
        releaseFiles: app-release.aab
        track: production
        status: completed
```

### 2. GitLab CI/CD
```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - deploy

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
  - chmod +x ./gradlew

cache:
  paths:
    - .gradle/wrapper
    - .gradle/caches

test:
  stage: test
  script:
    - ./gradlew test
    - ./gradlew lint
  artifacts:
    reports:
      junit: app/build/test-results/test*/TEST-*.xml
    paths:
      - app/build/reports/
    expire_in: 1 week

build_staging:
  stage: build
  script:
    - echo $STAGING_KEYSTORE_BASE64 | base64 -d > keystore/kardia-staging-key.keystore
    - ./gradlew assembleProdStaging
  artifacts:
    paths:
      - app/build/outputs/apk/prodStaging/
    expire_in: 1 week
  only:
    - develop

build_production:
  stage: build
  script:
    - echo $KEYSTORE_BASE64 | base64 -d > keystore/kardia-release-key.keystore
    - ./gradlew assembleRelease
    - ./gradlew bundleRelease
  artifacts:
    paths:
      - app/build/outputs/apk/release/
      - app/build/outputs/bundle/release/
    expire_in: 1 month
  only:
    - main
    - tags

deploy_play_store:
  stage: deploy
  script:
    - echo "Deploying to Google Play Store..."
    # Aquí iría el script de despliegue a Play Store
  only:
    - tags
```

## Configuración de Firebase

### 1. Configuración Inicial de Firebase
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Inicializar sesión
firebase login

# Inicializar proyecto Firebase
firebase init
```

### 2. Configuración de Firestore Indexes

**IMPORTANTE**: Los índices de Firestore deben configurarse antes del primer despliegue para evitar errores `FAILED_PRECONDITION`.

#### Opción A: Configuración Manual (Recomendado para producción)
1. **Acceder a Firebase Console**
   - Ir a [Firebase Console](https://console.firebase.google.com/)
   - Seleccionar el proyecto
   - Navegar a Firestore Database → Índices

2. **Crear Índices Compuestos**
   
   **Para colección `appointments`:**
   - Campo: `usuarioId` (Ascendente)
   - Campo: `fecha` (Descendente)
   
   **Para colección `notifications`:**
   - Campo: `usuarioId` (Ascendente) + `fechaCreacion` (Descendente)
   - Campo: `usuarioId` (Ascendente) + `leida` (Ascendente) + `fechaCreacion` (Descendente)

#### Opción B: Configuración Automática
```bash
# Inicializar Firestore
firebase init firestore

# Desplegar índices desde firestore.indexes.json
firebase deploy --only firestore:indexes
```

**Archivo firestore.indexes.json:**
```json
{
  "indexes": [
    {
      "collectionGroup": "appointments",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "usuarioId", "order": "ASCENDING"},
        {"fieldPath": "fecha", "order": "DESCENDING"}
      ]
    },
    {
      "collectionGroup": "notifications", 
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "usuarioId", "order": "ASCENDING"},
        {"fieldPath": "fechaCreacion", "order": "DESCENDING"}
      ]
    }
  ]
}
```

### 3. Reglas de Seguridad de Firestore
```bash
# Desplegar reglas de seguridad
firebase deploy --only firestore:rules
```

**Archivo firestore.rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Colecciones principales
    match /appointments/{appointmentId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.usuarioId;
    }
    
    match /ratings/{ratingId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    match /notifications/{notificationId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.usuarioId;
    }
  }
}
```

### 4. Verificación de Configuración
```bash
# Verificar configuración de Firebase
firebase projects:list

# Verificar estado de Firestore
firebase firestore:indexes

# Probar reglas de seguridad
firebase emulators:start --only firestore
```

## Distribución Interna

### 1. Firebase App Distribution
```kotlin
// En app/build.gradle.kts
plugins {
    id("com.google.firebase.appdistribution")
}

firebaseAppDistribution {
    artifactType = "APK"
    groups = "internal-testers"
    releaseNotes = "Nueva versión con mejoras en la interfaz de usuario"
}
```

```bash
# Distribuir a Firebase
./gradlew assembleDebug appDistributionUploadDebug
```

### 2. Distribución por Email
```bash
#!/bin/bash
# distribute-internal.sh

VERSION=$(grep "versionName" app/build.gradle.kts | cut -d'"' -f2)
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Build APK
./gradlew assembleDebug

# Crear email con APK adjunto
echo "Nueva versión $VERSION de Kardia disponible para testing interno." | \
mail -s "Kardia v$VERSION - Internal Build" \
     -a "$APK_PATH" \
     team@yourcompany.com
```

## Google Play Store

### 1. Preparar para Play Store
```kotlin
// Configuración final para Play Store
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
        
        // Configurar para múltiples arquitecturas
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

### 2. Metadatos de Play Store
```
# Crear carpeta play-store-assets/
play-store-assets/
├── screenshots/
│   ├── phone/
│   ├── tablet/
│   └── wear/
├── feature-graphic.png
├── icon.png
├── descriptions/
│   ├── es/
│   │   ├── title.txt
│   │   ├── short-description.txt
│   │   └── full-description.txt
│   └── en/
│       ├── title.txt
│       ├── short-description.txt
│       └── full-description.txt
└── release-notes/
    ├── es/
    └── en/
```

### 3. Configurar Play Console
1. **Crear aplicación en Play Console**
2. **Configurar información de la app**:
   - Título: "Kardia - Reserva Médica"
   - Descripción corta: "Agenda citas médicas fácilmente"
   - Descripción completa: [Ver archivo de descripción]
   - Categoría: "Medicina"
   - Clasificación de contenido: "Para toda la familia"

3. **Subir assets gráficos**:
   - Icono de la aplicación (512x512)
   - Gráfico destacado (1024x500)
   - Screenshots (mínimo 2, máximo 8)

4. **Configurar precios y distribución**:
   - Aplicación gratuita
   - Países disponibles
   - Clasificación de contenido

## Versionado

### 1. Estrategia de Versionado (Semantic Versioning)
```
MAJOR.MINOR.PATCH
1.0.0 - Primera versión estable
1.1.0 - Nuevas funcionalidades
1.1.1 - Corrección de bugs
```

### 2. Automatizar Versionado
```bash
#!/bin/bash
# bump-version.sh

TYPE=$1 # major, minor, patch

if [ -z "$TYPE" ]; then
    echo "Uso: ./bump-version.sh [major|minor|patch]"
    exit 1
fi

# Leer versión actual
CURRENT_VERSION=$(grep "versionName" app/build.gradle.kts | cut -d'"' -f2)
CURRENT_CODE=$(grep "versionCode" app/build.gradle.kts | grep -o '[0-9]*')

# Calcular nueva versión
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]}

case $TYPE in
    "major")
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        ;;
    "minor")
        MINOR=$((MINOR + 1))
        PATCH=0
        ;;
    "patch")
        PATCH=$((PATCH + 1))
        ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_CODE=$((CURRENT_CODE + 1))

# Actualizar build.gradle.kts
sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" app/build.gradle.kts
sed -i "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts

echo "Versión actualizada: $CURRENT_VERSION -> $NEW_VERSION"
echo "Código de versión: $CURRENT_CODE -> $NEW_CODE"

# Crear tag de git
git add app/build.gradle.kts
git commit -m "Bump version to $NEW_VERSION"
git tag -a "v$NEW_VERSION" -m "Version $NEW_VERSION"
```

## Monitoreo Post-Despliegue

### 1. Firebase Crashlytics
```kotlin
// En app/build.gradle.kts
plugins {
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

### 2. Play Console Vitals
- Monitorear ANRs (Application Not Responding)
- Crashes
- Rendimiento de la aplicación
- Estadísticas de uso

### 3. Configurar Alertas
```kotlin
// En Application class
class KardiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configurar Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        
        // Configurar Analytics
        FirebaseAnalytics.getInstance(this)
    }
}
```

## Rollback Strategy

### 1. Plan de Rollback
```bash
#!/bin/bash
# rollback.sh

PREVIOUS_VERSION=$1

if [ -z "$PREVIOUS_VERSION" ]; then
    echo "Uso: ./rollback.sh [version]"
    echo "Ejemplo: ./rollback.sh v1.0.0"
    exit 1
fi

echo "🔄 Iniciando rollback a $PREVIOUS_VERSION..."

# Checkout a la versión anterior
git checkout $PREVIOUS_VERSION

# Build de la versión anterior
./gradlew clean
./gradlew bundleRelease

echo "✅ Rollback completado. Subir manualmente a Play Console."
```

### 2. Hotfix Process
1. Crear branch hotfix desde main
2. Aplicar fix mínimo
3. Test exhaustivo
4. Build y deploy urgente
5. Merge a main y develop

Esta guía de despliegue asegura un proceso robusto y confiable para llevar Kardia desde desarrollo hasta producción.