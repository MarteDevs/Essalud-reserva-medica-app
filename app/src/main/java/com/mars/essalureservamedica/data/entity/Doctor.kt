package com.mars.essalureservamedica.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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