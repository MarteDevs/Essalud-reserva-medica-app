package com.mars.essalureservamedica.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cita::class,
            parentColumns = ["id"],
            childColumns = ["citaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["doctorId"]),
        Index(value = ["citaId"], unique = true) // Una calificación por cita
    ]
)
data class Calificacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val doctorId: Int,
    val citaId: Int,
    val puntuacion: Float, // De 1.0 a 5.0
    val comentario: String? = null,
    val fechaCalificacion: Date = Date()
)