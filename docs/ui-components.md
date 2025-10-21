# Componentes de Interfaz de Usuario - Kardia

## Diseño General

La aplicación Kardia sigue los principios de **Material Design** con una paleta de colores centrada en tonos verdes que evocan salud y confianza. La interfaz está optimizada para una experiencia de usuario intuitiva y accesible.

## Paleta de Colores

### Colores Primarios
```xml
<color name="primary_color">#2E7D32</color>        <!-- Verde principal -->
<color name="primary_dark">#1B5E20</color>         <!-- Verde oscuro -->
<color name="primary_light">#4CAF50</color>        <!-- Verde claro -->
<color name="primary_green">#57C973</color>        <!-- Verde personalizado -->
```

### Colores Secundarios
```xml
<color name="secondary_color">#1976D2</color>      <!-- Azul secundario -->
<color name="secondary_dark">#0D47A1</color>       <!-- Azul oscuro -->
<color name="secondary_light">#42A5F5</color>      <!-- Azul claro -->
```

### Colores de Estado
```xml
<color name="success">#4CAF50</color>              <!-- Éxito -->
<color name="warning">#FF9800</color>              <!-- Advertencia -->
<color name="error">#F44336</color>                <!-- Error -->
<color name="info">#2196F3</color>                 <!-- Información -->
```

## Estructura de Navegación

### Navegación Principal (Bottom Navigation)

La aplicación utiliza un **BottomNavigationView** con 5 secciones principales:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_home"
          android:icon="@drawable/ic_home"
          android:title="Inicio" />
    
    <item android:id="@+id/nav_doctors"
          android:icon="@drawable/ic_doctors"
          android:title="Doctores" />
    
    <item android:id="@+id/nav_appointments"
          android:icon="@drawable/ic_appointments"
          android:title="Citas" />
    
    <item android:id="@+id/nav_notifications"
          android:icon="@drawable/ic_notifications"
          android:title="Notificaciones" />
    
    <item android:id="@+id/nav_profile"
          android:icon="@drawable/ic_person"
          android:title="Perfil" />
</menu>
```

**Características:**
- **Animaciones**: Efectos de escala al seleccionar elementos
- **Estados visuales**: Colores diferentes para elementos activos/inactivos
- **Iconografía**: Iconos vectoriales personalizados

## Pantallas Principales

### 1. Pantalla de Inicio (HomeFragment)

**Componentes principales:**
- **Saludo personalizado**: Muestra el nombre del usuario
- **Tarjetas de estadísticas**: Resumen de citas y actividad
- **Accesos rápidos**: Botones para acciones frecuentes
- **Información del sistema**: Estado de la aplicación

**Layout característico:**
```xml
<ScrollView>
    <LinearLayout orientation="vertical">
        <!-- Header con saludo -->
        <TextView android:id="@+id/tvUserName" />
        
        <!-- Tarjetas de estadísticas -->
        <CardView>
            <LinearLayout>
                <!-- Estadísticas de citas -->
            </LinearLayout>
        </CardView>
        
        <!-- Accesos rápidos -->
        <LinearLayout orientation="horizontal">
            <Button text="Ver Doctores" />
            <Button text="Mis Citas" />
        </LinearLayout>
    </LinearLayout>
</ScrollView>
```

### 2. Lista de Doctores (DoctorsFragment)

**Componentes principales:**
- **Barra de búsqueda**: SearchView personalizado
- **Filtro por especialidad**: Spinner con especialidades médicas
- **Lista de doctores**: RecyclerView con adaptador personalizado
- **Pull-to-refresh**: SwipeRefreshLayout

**Características del adaptador:**
```kotlin
class DoctorsAdapter(private val onDoctorClick: (Doctor) -> Unit) : 
    RecyclerView.Adapter<DoctorsAdapter.DoctorViewHolder>() {
    
    class DoctorViewHolder(val binding: ItemDoctorBinding) : 
        RecyclerView.ViewHolder(binding.root)
}
```

**Item de doctor incluye:**
- Foto del doctor (placeholder si no disponible)
- Nombre y especialidad
- Experiencia
- Estado de disponibilidad
- Botón de acción

### 3. Gestión de Citas (AppointmentsFragment)

**Componentes principales:**
- **Pestañas**: TabLayout para "Próximas" y "Historial"
- **Lista de citas**: RecyclerView con información detallada
- **Acciones contextuales**: Reprogramar, cancelar, calificar

**Estados de cita visuales:**
- **Confirmada**: Verde con icono de check
- **Pendiente**: Amarillo con icono de reloj
- **Cancelada**: Rojo con icono de X
- **Completada**: Azul con icono de check doble

### 4. Perfil de Usuario (ProfileFragment)

**Componentes principales:**
- **Información personal**: Nombre, email
- **Estadísticas**: Número de citas, calificaciones dadas
- **Configuraciones**: Opciones de la aplicación
- **Cerrar sesión**: Botón de logout

### 5. Notificaciones (NotificationsFragment)

**Componentes principales:**
- **Lista de notificaciones**: RecyclerView con diferentes tipos
- **Indicadores visuales**: Notificaciones leídas/no leídas
- **Acciones**: Marcar como leída, eliminar

## Componentes Personalizados

### 1. Diálogos Personalizados

#### RatingDialogFragment
```kotlin
class RatingDialogFragment : DialogFragment() {
    // Sistema de calificación con estrellas
    // Campo de comentarios
    // Botones de acción
}
```

#### RescheduleDialogFragment
```kotlin
class RescheduleDialogFragment : DialogFragment() {
    // Selector de fecha
    // Selector de hora
    // Confirmación de cambios
}
```

### 2. Adaptadores Especializados

#### AppointmentsAdapter
- **ViewTypes múltiples**: Diferentes layouts según estado
- **Animaciones**: Transiciones suaves
- **Acciones contextuales**: Menús deslizables

#### NotificationsAdapter
- **Indicadores visuales**: Estados de lectura
- **Tipos de notificación**: Iconos específicos
- **Timestamps**: Formato de fecha amigable

## Animaciones y Transiciones

### Animaciones de Navegación
```xml
<!-- slide_in_right.xml -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:fromXDelta="100%p"
        android:toXDelta="0"
        android:duration="300" />
</set>
```

### Animaciones de Elementos
- **Bottom Navigation**: Escala al seleccionar
- **Botones**: Efecto ripple personalizado
- **Tarjetas**: Elevación dinámica
- **Listas**: Animaciones de entrada/salida

## Recursos Gráficos

### Iconos Vectoriales
- **ic_home**: Icono de inicio
- **ic_doctors**: Icono de doctores
- **ic_appointments**: Icono de citas
- **ic_notifications**: Icono de notificaciones
- **ic_person**: Icono de perfil
- **ic_medical_logo**: Logo médico personalizado

### Backgrounds Personalizados
```xml
<!-- rounded_background.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/background_card" />
    <corners android:radius="12dp" />
    <stroke android:width="1dp" android:color="@color/divider" />
</shape>
```

### Gradientes
```xml
<!-- background_futuristic_gradient.xml -->
<gradient
    android:startColor="@color/primary_light"
    android:endColor="@color/primary_color"
    android:angle="45" />
```

## Responsive Design

### Adaptabilidad de Pantalla
- **Layouts flexibles**: ConstraintLayout para adaptabilidad
- **Dimensiones relativas**: dp y sp para escalabilidad
- **Orientación**: Soporte para portrait y landscape

### Accesibilidad
- **Content descriptions**: Para lectores de pantalla
- **Tamaños de toque**: Mínimo 48dp para elementos interactivos
- **Contraste**: Cumple con estándares WCAG
- **Navegación por teclado**: Soporte completo

## Temas y Estilos

### Tema Principal
```xml
<style name="Theme.EssaluReservaMedica" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/primary_color</item>
    <item name="colorPrimaryVariant">@color/primary_dark</item>
    <item name="colorSecondary">@color/secondary_color</item>
    <item name="android:statusBarColor">@color/primary_dark</item>
</style>
```

### Estilos de Botones
```xml
<style name="RoundedButton" parent="Widget.Material3.Button">
    <item name="android:background">@drawable/rounded_button</item>
    <item name="android:textColor">@color/white</item>
    <item name="android:elevation">4dp</item>
</style>
```

### Estilos de Texto
```xml
<style name="HeaderText">
    <item name="android:textSize">24sp</item>
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>
```

## ViewBinding

Todas las vistas utilizan **ViewBinding** para acceso seguro:

```kotlin
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(...): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

## Mejores Prácticas Implementadas

1. **Material Design**: Siguiendo guías oficiales de Google
2. **Consistencia visual**: Elementos uniformes en toda la app
3. **Feedback visual**: Respuesta inmediata a acciones del usuario
4. **Navegación intuitiva**: Flujos lógicos y predecibles
5. **Performance**: Layouts optimizados y reciclaje de vistas
6. **Accesibilidad**: Soporte para usuarios con discapacidades
7. **Responsive**: Adaptación a diferentes tamaños de pantalla
8. **Temas**: Soporte para modo claro/oscuro (preparado)