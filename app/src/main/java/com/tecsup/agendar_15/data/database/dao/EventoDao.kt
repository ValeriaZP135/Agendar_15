package com.tecsup.agendar_15.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tecsup.agendar_15.data.database.entities.Evento

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE usuarioId = :usuarioId ORDER BY fechaInicio ASC")
    fun getEventosPorUsuario(usuarioId: String): LiveData<List<Evento>>

    @Query("SELECT * FROM eventos WHERE usuarioId = :usuarioId AND fechaInicio >= :fechaInicio AND fechaInicio <= :fechaFin ORDER BY fechaInicio ASC")
    fun getEventosPorFecha(usuarioId: String, fechaInicio: Long, fechaFin: Long): LiveData<List<Evento>>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun getEventoPorId(id: String): Evento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvento(evento: Evento)

    @Update
    suspend fun actualizarEvento(evento: Evento)

    @Delete
    suspend fun eliminarEvento(evento: Evento)
}