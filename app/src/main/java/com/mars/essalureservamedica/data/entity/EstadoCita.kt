package com.mars.essalureservamedica.data.entity

enum class EstadoCita(val displayName: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    REPROGRAMADA("Reprogramada");

    companion object {
        fun fromString(value: String): EstadoCita {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: PENDIENTE
        }
    }
}