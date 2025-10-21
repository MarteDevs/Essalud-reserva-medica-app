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
- **Coroutines**: Para operaciones asíncronas
- **TypeConverters**: Para manejo de tipos de datos complejos

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

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Sincronizar dependencias de Gradle**
4. **Ejecutar en dispositivo o emulador**

## Documentación Adicional

- [Arquitectura de la Aplicación](./architecture.md)
- [Modelo de Base de Datos](./database.md)
- [Interfaz de Usuario](./ui-components.md)
- [API y Funcionalidades](./api-features.md)
- [Guía de Desarrollo](./development-guide.md)

## Versión

**Versión actual**: 1.0
**Código de versión**: 1

## Licencia

Este proyecto está desarrollado para EsSalud como parte del sistema de reservas médicas.