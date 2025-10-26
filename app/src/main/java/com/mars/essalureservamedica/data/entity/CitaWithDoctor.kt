package com.mars.essalureservamedica.data.entity

data class CitaWithDoctor(
    val cita: Cita,
    val doctor: Doctor
)

data class DoctorConFrecuencia(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val experiencia: String,
    val disponibilidad: String,
    val foto: String?,
    val totalCitas: Int
)