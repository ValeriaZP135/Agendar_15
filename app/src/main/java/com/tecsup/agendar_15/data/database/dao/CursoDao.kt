package com.tecsup.agendar_15.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tecsup.agendar_15.data.database.entities.Curso

@Dao
interface CursoDao {
    @Query("SELECT * FROM cursos WHERE usuarioId = :usuarioId ORDER BY nombre ASC")
    fun getCursosPorUsuario(usuarioId: String): LiveData<List<Curso>>

    @Query("SELECT * FROM cursos WHERE id = :id LIMIT 1")
    suspend fun getCursoPorId(id: String): Curso?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCurso(curso: Curso)

    @Update
    suspend fun actualizarCurso(curso: Curso)

    @Delete
    suspend fun eliminarCurso(curso: Curso)
}