package com.mars.essalureservamedica.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mars.essalureservamedica.MainActivity
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.databinding.ActivityAuthBinding
import com.mars.essalureservamedica.utils.SessionManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Verificar si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity()
            return
        }
        
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}