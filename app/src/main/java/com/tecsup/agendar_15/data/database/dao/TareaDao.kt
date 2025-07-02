package com.tecsup.agendar_15.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tecsup.agendar_15.data.database.entities.Tarea

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE usuarioId = :usuarioId ORDER BY prioridad DESC, fechaVencimiento ASC")
    fun getTareasPorUsuario(usuarioId: String): LiveData<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE usuarioId = :usuarioId AND completada = 0 ORDER BY prioridad DESC, fechaVencimiento ASC")
    fun getTareasPendientes(usuarioId: String): LiveData<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun getTareaPorId(id: String): Tarea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: Tarea)

    @Update
    suspend fun actualizarTarea(tarea: Tarea)

    @Delete
    suspend fun eliminarTarea(tarea: Tarea)
}