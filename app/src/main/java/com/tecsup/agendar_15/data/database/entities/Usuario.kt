package com.tecsup.agendar_15.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val email: String,
    val password: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val avatarPath: String? = null
)