package com.mars.essalureservamedica.data.firebase.models

data class UserFirestore(
    val id: String = "",
    val nombreCompleto: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "nombreCompleto" to nombreCompleto,
        "email" to email,
        "createdAt" to createdAt
    )
}

data class DoctorFirestore(
    val id: String = "",
    val nombre: String = "",
    val especialidad: String = "",
    val experiencia: String = "",
    val disponibilidad: String = "",
    val foto: String? = null,
    val rating: Double = 0.0,
    val totalRatings: Int = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "nombre" to nombre,
        "especialidad" to especialidad,
        "experiencia" to experiencia,
        "disponibilidad" to disponibilidad,
        "foto" to (foto ?: ""),
        "rating" to rating,
        "totalRatings" to totalRatings
    )
}

data class CitaFirestore(
    val id: String = "",
    val usuarioId: String = "",
    val doctorId: String = "",
    val fecha: Long = 0,
    val hora: String = "",
    val estado: String = "PENDIENTE",
    val motivo: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "usuarioId" to usuarioId,
        "doctorId" to doctorId,
        "fecha" to fecha,
        "hora" to hora,
        "estado" to estado,
        "motivo" to motivo,
        "createdAt" to createdAt
    )
}

data class CalificacionFirestore(
    val id: String = "",
    val usuarioId: String = "",
    val doctorId: String = "",
    val citaId: String = "",
    val puntuacion: Int = 0,
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "usuarioId" to usuarioId,
        "doctorId" to doctorId,
        "citaId" to citaId,
        "puntuacion" to puntuacion,
        "comentario" to comentario,
        "fecha" to fecha
    )
}

data class NotificacionFirestore(
    val id: String = "",
    val usuarioId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "", // "CITA_CONFIRMADA", "CITA_CANCELADA", "RECORDATORIO", "GENERAL"
    val leida: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val citaId: String? = null // Opcional, para notificaciones relacionadas con citas
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "usuarioId" to usuarioId,
        "titulo" to titulo,
        "mensaje" to mensaje,
        "tipo" to tipo,
        "leida" to leida,
        "fechaCreacion" to fechaCreacion,
        "citaId" to (citaId ?: "")
    )
}
