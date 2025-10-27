package com.mars.essalureservamedica.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa la entidad de un doctor en la base de datos local (Room).
 * Esta clase define la estructura de la tabla "doctores".
 *
 * @property id El identificador único para el doctor, generado automáticamente.
 * @property nombre El nombre completo del doctor.
 * @property especialidad La especialidad médica del doctor (p. ej., "Cardiología", "Pediatría").
 * @property experiencia Una breve descripción de la experiencia del doctor.
 * @property disponibilidad Información sobre los horarios de disponibilidad del doctor.
 * @property foto La URL o ruta a la foto de perfil del doctor (opcional).
 */
@Entity(tableName = "doctores")
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val especialidad: String,
    val experiencia: String,
    val disponibilidad: String,
    val foto: String? = null
)
