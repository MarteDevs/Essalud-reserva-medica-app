# Kardia - Aplicación de Reserva Médica EsSalud

## Descripción General

**Kardia** es una aplicación móvil Android desarrollada para facilitar la reserva de citas médicas en el sistema EsSalud. La aplicación permite a los usuarios registrarse, buscar doctores por especialidad, agendar citas médicas, gestionar sus citas existentes y recibir notificaciones importantes.

## Características Principales

### 🏥 Gestión de Citas Médicas
- **Reserva de citas**: Los usuarios pueden agendar citas con doctores disponibles
- **Reprogramación**: Posibilidad de cambiar la fecha y hora de citas existentes
- **Cancelación**: Cancelar citas cuando sea necesario
- **Historial**: Visualizar el historial completo de citas médicas

### 👨‍⚕️ Directorio de Doctores
- **Búsqueda avanzada**: Buscar doctores por nombre o especialidad
- **Filtros**: Filtrar doctores por especialidad médica
- **Información detallada**: Ver perfil completo del doctor, experiencia y disponibilidad
- **Sistema de calificaciones**: Calificar y comentar sobre la atención recibida

### 🔔 Sistema de Notificaciones
- **Notificaciones en tiempo real**: Recordatorios de citas próximas
- **Estados de cita**: Notificaciones sobre confirmaciones, cancelaciones y cambios
- **Gestión de notificaciones**: Marcar como leídas y eliminar notificaciones

### 👤 Gestión de Perfil
- **Registro de usuarios**: Crear cuenta con email y contraseña
- **Autenticación segura**: Sistema de login con validación
- **Perfil personalizado**: Gestionar información personal del usuario

## Tecnologías Utilizadas

### Arquitectura y Patrones
- **MVVM (Model-View-ViewModel)**: Patrón arquitectónico principal
- **Repository Pattern**: Para la gestión de datos
- **LiveData**: Para observación reactiva de datos
- **ViewBinding**: Para acceso seguro a las vistas

### Base de Datos
- **Room Database**: Base de datos local SQLite
- **Firebase Firestore**: Base de datos en la nube con sincronización en tiempo real
- **Coroutines**: Para operaciones asíncronas
- **TypeConverters**: Para manejo de tipos de datos complejos

### Firebase Services
- **Firebase Authentication**: Autenticación de usuarios
- **Firebase Firestore**: Base de datos NoSQL en tiempo real
- **Firebase Analytics**: Análisis de uso de la aplicación

### Interfaz de Usuario
- **Material Design**: Siguiendo las guías de diseño de Google
- **Navigation Component**: Para navegación entre fragmentos
- **RecyclerView**: Para listas eficientes
- **SwipeRefreshLayout**: Para actualización por deslizamiento

### Dependencias Principales
- **Kotlin**: Lenguaje de programación principal
- **AndroidX**: Bibliotecas de soporte modernas
- **Lifecycle Components**: Para manejo del ciclo de vida
- **Navigation**: Para navegación entre pantallas

## Estructura del Proyecto

```
app/
├── src/main/java/com/mars/essalureservamedica/
│   ├── MainActivity.kt                 # Actividad principal
│   ├── data/                          # Capa de datos
│   │   ├── converter/                 # Convertidores de tipos
│   │   ├── dao/                       # Data Access Objects
│   │   ├── database/                  # Configuración de base de datos
│   │   ├── entity/                    # Entidades de base de datos
│   │   └── repository/                # Repositorios
│   ├── ui/                           # Capa de presentación
│   │   ├── auth/                     # Autenticación
│   │   ├── doctor/                   # Detalles de doctores
│   │   ├── main/                     # Fragmentos principales
│   │   ├── notifications/            # Notificaciones
│   │   ├── profile/                  # Perfil de usuario
│   │   ├── rating/                   # Sistema de calificaciones
│   │   └── schedule/                 # Programación de citas
│   └── utils/                        # Utilidades
└── src/main/res/                     # Recursos
    ├── layout/                       # Layouts XML
    ├── drawable/                     # Recursos gráficos
    ├── values/                       # Valores (colores, strings)
    └── navigation/                   # Grafos de navegación
```

## Requisitos del Sistema

- **Android API Level**: Mínimo 30 (Android 11)
- **Target SDK**: 36
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.0
- **Java**: 11

## Instalación y Configuración

### Requisitos Previos
- **Android Studio**: Última versión estable
- **Firebase CLI**: Para configuración de índices (opcional)
- **Cuenta de Firebase**: Para servicios en la nube

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone [https://github.com/MarteDevs/Essalud-reserva-medica-app.git]
   cd essalud-reserva-medica-app
   ```

2. **Configurar Firebase**
   - Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descargar `google-services.json` y colocarlo en `app/`
   - Habilitar Authentication y Firestore

3. **Configurar Índices de Firestore**
   
   **Opción A: Manual (Recomendado)**
   - Ir a Firebase Console → Firestore Database → Índices
   - Crear índice para colección `appointments`:
     - Campo: `usuarioId` (Ascendente) + `fecha` (Descendente)
   
   **Opción B: Automática**
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init firestore
   firebase deploy --only firestore:indexes
   ```

4. **Abrir en Android Studio**
   - Abrir el proyecto en Android Studio
   - Sincronizar dependencias de Gradle
   - Esperar a que termine la indexación

5. **Ejecutar la aplicación**
   - Conectar dispositivo Android o iniciar emulador
   - Ejecutar la aplicación desde Android Studio

### Configuración Adicional

- **Reglas de Firestore**: Las reglas de seguridad se configuran automáticamente
- **Migración de datos**: La primera ejecución migrará datos de colecciones españolas a inglesas
- **Índices**: Los índices compuestos son necesarios para evitar errores `FAILED_PRECONDITION`

## Documentación Adicional

- [Arquitectura de la Aplicación](./architecture.md)
- [Modelo de Base de Datos](./database.md)
- [Migración a Firebase](./firebase-migration.md)
- [Interfaz de Usuario](./ui-components.md)
- [API y Funcionalidades](./api-features.md)
- [Guía de Desarrollo](./development-guide.md)
- [Guía de Despliegue](./deployment-guide.md)

## Versión

**Versión actual**: 1.0
**Código de versión**: 1

## Licencia

Este proyecto está desarrollado para EsSalud como parte del sistema de reservas médicas.
