package com.tecsup.agendar_15.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey
    val id: String,
    val usuarioId: String,
    val cursoId: String?,
    val titulo: String,
    val descripcion: String?,
    val fechaVencimiento: Long?,
    val prioridad: Int = 1, // 1: Baja, 2: Media, 3: Alta
    val completada: Boolean = false,
    val fechaCompletado: Long? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)