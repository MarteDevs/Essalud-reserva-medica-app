package com.mars.essalureservamedica.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["fechaCreacion"])
    ]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // "CITA_CONFIRMADA", "CITA_CANCELADA", "RECORDATORIO", "GENERAL"
    val leida: Boolean = false,
    val fechaCreacion: Date = Date(),
    val citaId: Int? = null // Opcional, para notificaciones relacionadas con citas
)