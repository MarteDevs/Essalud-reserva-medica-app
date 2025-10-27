package com.mars.essalureservamedica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mars.essalureservamedica.databinding.ActivityMainBinding
import com.mars.essalureservamedica.ui.auth.AuthActivity
import com.mars.essalureservamedica.ui.migration.MigrationActivity
import com.mars.essalureservamedica.utils.SessionManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * Actividad principal que aloja la navegación principal de la aplicación después de que el usuario ha iniciado sesión.
 * Gestiona la comprobación de la sesión, la migración de datos y la configuración de la navegación inferior.
 */
class MainActivity : AppCompatActivity() {

    // Enlace de vista para acceder a los componentes de la interfaz de usuario definidos en el diseño XML.
    private lateinit var binding: ActivityMainBinding
    // Controlador de navegación para gestionar la navegación entre fragmentos.
    private lateinit var navController: NavController
    // Gestor de sesión para manejar los datos y el estado de la sesión del usuario.
    private lateinit var sessionManager: SessionManager

    /**
     * Se llama cuando se crea la actividad.
     * Realiza la inicialización, incluyendo la pantalla de bienvenida, la comprobación de la sesión y la configuración de la interfaz de usuario.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala la pantalla de bienvenida, que se muestra mientras la aplicación se carga.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Inicializa el SessionManager para gestionar la sesión del usuario.
        sessionManager = SessionManager(this)
        
        // Comprueba si el usuario ha iniciado sesión.
        if (!sessionManager.isLoggedIn()) {
            // Si no ha iniciado sesión, redirige a AuthActivity y finaliza la actividad actual.
            navigateToAuthActivity()
            return
        }
        
        // Comprueba si la migración de la cuenta está pendiente.
        if (!sessionManager.isMigrationCompleted()) {
            // Si la migración es necesaria, redirige a MigrationActivity y finaliza la actividad actual.
            navigateToMigrationActivity()
            return
        }
        
        // Infla el diseño de la actividad usando el enlace de vista.
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Establece la vista de contenido de la actividad en la raíz del diseño inflado.
        setContentView(binding.root)
        
        // Configura la navegación principal de la aplicación.
        setupNavigation()
    }

    /**
     * Configura el NavController y el BottomNavigationView para la navegación entre fragmentos.
     */
    private fun setupNavigation() {
        // Encuentra el NavHostFragment que gestiona los fragmentos de navegación.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        // Obtiene el NavController del NavHostFragment.
        navController = navHostFragment.navController

        // Conecta el BottomNavigationView con el NavController.
        binding.bottomNavigation.setupWithNavController(navController)

        // Carga una animación de escalado para los elementos de la navegación inferior.
        val scaleUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bottom_nav_item_animator)

        // Establece un oyente para los elementos seleccionados en la navegación inferior.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Aplica la animación de escalado al elemento de menú seleccionado.
            binding.bottomNavigation.findViewById<android.view.View>(item.itemId)?.startAnimation(scaleUp)
            // Navega al destino correspondiente si no es el destino actual.
            if (navController.currentDestination?.id != item.itemId) {
                navController.navigate(item.itemId)
            }
            // Devuelve verdadero para indicar que el evento ha sido manejado.
            true
        }
    }

    /**
     * Navega a AuthActivity, borrando la pila de actividades anterior.
     * Se utiliza cuando el usuario necesita iniciar sesión.
     */
    private fun navigateToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        // Estas banderas aseguran que el usuario no pueda volver a esta actividad presionando el botón de retroceso.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza la actividad actual.
    }
    
    /**
     * Navega a MigrationActivity, borrando la pila de actividades anterior.
     * Se utiliza cuando el usuario necesita migrar su cuenta.
     */
    private fun navigateToMigrationActivity() {
        val intent = Intent(this, MigrationActivity::class.java)
        // Estas banderas aseguran que el usuario no pueda volver a esta actividad presionando el botón de retroceso.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza la actividad actual.
    }
}
