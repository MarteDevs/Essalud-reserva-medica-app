package com.mars.essalureservamedica.ui.migration

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mars.essalureservamedica.MainActivity
import com.mars.essalureservamedica.data.database.AppDatabase
import com.mars.essalureservamedica.data.firebase.FirestoreService
import com.mars.essalureservamedica.data.migration.MigrationExecutor
import com.mars.essalureservamedica.databinding.ActivityMigrationBinding
import com.mars.essalureservamedica.utils.SessionManager

class MigrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMigrationBinding
    private lateinit var migrationExecutor: MigrationExecutor
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMigrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        // Inicializar MigrationExecutor
        val database = AppDatabase.getDatabase(this)
        val firestoreService = FirestoreService()
        migrationExecutor = MigrationExecutor(
            context = this,
            database = database,
            firestoreService = firestoreService,
            sessionManager = sessionManager
        )

        setupUI()
        checkMigrationStatus()
    }

    private fun setupUI() {
        binding.btnStartMigration.setOnClickListener {
            startMigration()
        }

        binding.btnSkipMigration.setOnClickListener {
            skipMigration()
        }

        binding.btnContinueToApp.setOnClickListener {
            navigateToMainApp()
        }
    }

    private fun checkMigrationStatus() {
        if (migrationExecutor.checkMigrationStatus()) {
            // Migración ya completada
            showMigrationCompleted()
        } else {
            // Mostrar opciones de migración
            showMigrationOptions()
        }
    }

    private fun showMigrationOptions() {
        binding.tvTitle.text = "Migración de Datos"
        binding.tvDescription.text = "Se han detectado datos locales. ¿Deseas migrarlos a Firebase para sincronización en la nube?"
        binding.btnStartMigration.visibility = View.VISIBLE
        binding.btnSkipMigration.visibility = View.VISIBLE
        binding.btnContinueToApp.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tvProgress.visibility = View.GONE
    }

    private fun showMigrationCompleted() {
        binding.tvTitle.text = "Migración Completada"
        binding.tvDescription.text = "Tus datos han sido migrados exitosamente a Firebase."
        binding.btnStartMigration.visibility = View.GONE
        binding.btnSkipMigration.visibility = View.GONE
        binding.btnContinueToApp.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.tvProgress.text = "¡Listo para continuar!"
        binding.tvProgress.visibility = View.VISIBLE
    }

    private fun startMigration() {
        binding.btnStartMigration.isEnabled = false
        binding.btnSkipMigration.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.tvProgress.visibility = View.VISIBLE

        migrationExecutor.executeMigration(
            onProgress = { message ->
                binding.tvProgress.text = message
            },
            onSuccess = {
                binding.tvTitle.text = "¡Migración Exitosa!"
                binding.tvDescription.text = "Todos tus datos han sido migrados correctamente a Firebase."
                binding.progressBar.visibility = View.GONE
                binding.btnContinueToApp.visibility = View.VISIBLE
                binding.tvProgress.text = "¡Migración completada exitosamente!"
            },
            onError = { error ->
                binding.tvTitle.text = "Error en la Migración"
                binding.tvDescription.text = "Ocurrió un error durante la migración: $error"
                binding.progressBar.visibility = View.GONE
                binding.btnStartMigration.isEnabled = true
                binding.btnSkipMigration.isEnabled = true
                binding.tvProgress.text = "Error: $error"
            }
        )
    }

    private fun skipMigration() {
        // Marcar migración como completada (saltada)
        sessionManager.setMigrationCompleted(true)
        navigateToMainApp()
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}