package com.tecsup.agendar_15.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey
    val id: String,
    val usuarioId: String,
    val cursoId: String?,
    val titulo: String,
    val descripcion: String?,
    val fechaInicio: Long,
    val fechaFin: Long,
    val esRecurrente: Boolean = false,
    val tipoRecurrencia: String? = null, // DIARIO, SEMANAL, MENSUAL
    val ubicacion: String?,
    val notificacionMinutos: Int = 15,
    val completado: Boolean = false,
    val color: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)