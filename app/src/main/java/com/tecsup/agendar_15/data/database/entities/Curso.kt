package com.tecsup.agendar_15.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey
    val id: String,
    val usuarioId: String,
    val nombre: String,
    val descripcion: String?,
    val color: String,
    val profesor: String?,
    val salon: String?,
    val creditos: Int = 0,
    val fechaCreacion: Long = System.currentTimeMillis()
)