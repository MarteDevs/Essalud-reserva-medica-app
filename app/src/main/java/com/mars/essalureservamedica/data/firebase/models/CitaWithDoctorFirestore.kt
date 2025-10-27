package com.mars.essalureservamedica.data.firebase.models

import java.io.Serializable
import java.util.Date

/**
 * Modelo que combina información de una cita con los datos del doctor
 * Reemplaza a CitaWithDoctorInfo de Room
 */
data class CitaWithDoctorFirestore(
    val id: String,
    val usuarioId: String,
    val doctorId: String,
    val fecha: Long,
    val hora: String,
    val estado: String,
    val motivo: String,
    val createdAt: Long,
    // Información del doctor
    val doctorNombre: String,
    val doctorEspecialidad: String,
    val doctorFoto: String? = null,
    val doctorRating: Double = 0.0
) : Serializable {
    /**
     * Convierte la fecha (Long) a Date
     */
    val fechaHora: Date
        get() = Date(fecha)

    companion object {
        /**
         * Crea una instancia combinando CitaFirestore y DoctorFirestore
         */
        fun from(cita: CitaFirestore, doctor: DoctorFirestore): CitaWithDoctorFirestore {
            return CitaWithDoctorFirestore(
                id = cita.id,
                usuarioId = cita.usuarioId,
                doctorId = cita.doctorId,
                fecha = cita.fecha,
                hora = cita.hora,
                estado = cita.estado,
                motivo = cita.motivo,
                createdAt = cita.createdAt,
                doctorNombre = doctor.nombre,
                doctorEspecialidad = doctor.especialidad,
                doctorFoto = doctor.foto,
                doctorRating = doctor.rating
            )
        }
    }
}