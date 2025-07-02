package com.tecsup.agendar_15.data.repository

import androidx.lifecycle.LiveData
import com.tecsup.agendar_15.data.database.dao.*
import com.tecsup.agendar_15.data.database.entities.*

class AgendarRepository(
    private val usuarioDao: UsuarioDao,
    private val cursoDao: CursoDao,
    private val eventoDao: EventoDao,
    private val tareaDao: TareaDao
) {

    // Usuarios
    suspend fun login(email: String, password: String): Usuario? = usuarioDao.login(email, password)
    suspend fun registrarUsuario(usuario: Usuario): Long = usuarioDao.insertarUsuario(usuario)
    suspend fun getUsuarioPorEmail(email: String): Usuario? = usuarioDao.getUsuarioPorEmail(email)
    suspend fun actualizarUsuario(usuario: Usuario) = usuarioDao.actualizarUsuario(usuario)

    // Cursos
    fun getCursosPorUsuario(usuarioId: String): LiveData<List<Curso>> = cursoDao.getCursosPorUsuario(usuarioId)
    suspend fun insertarCurso(curso: Curso) = cursoDao.insertarCurso(curso)
    suspend fun actualizarCurso(curso: Curso) = cursoDao.actualizarCurso(curso)
    suspend fun eliminarCurso(curso: Curso) = cursoDao.eliminarCurso(curso)

    // Eventos
    fun getEventosPorUsuario(usuarioId: String): LiveData<List<Evento>> = eventoDao.getEventosPorUsuario(usuarioId)
    fun getEventosPorFecha(usuarioId: String, fechaInicio: Long, fechaFin: Long): LiveData<List<Evento>> =
        eventoDao.getEventosPorFecha(usuarioId, fechaInicio, fechaFin)
    suspend fun insertarEvento(evento: Evento) = eventoDao.insertarEvento(evento)
    suspend fun actualizarEvento(evento: Evento) = eventoDao.actualizarEvento(evento)
    suspend fun eliminarEvento(evento: Evento) = eventoDao.eliminarEvento(evento)

    // Tareas
    fun getTareasPorUsuario(usuarioId: String): LiveData<List<Tarea>> = tareaDao.getTareasPorUsuario(usuarioId)
    fun getTareasPendientes(usuarioId: String): LiveData<List<Tarea>> = tareaDao.getTareasPendientes(usuarioId)
    suspend fun insertarTarea(tarea: Tarea) = tareaDao.insertarTarea(tarea)
    suspend fun actualizarTarea(tarea: Tarea) = tareaDao.actualizarTarea(tarea)
    suspend fun eliminarTarea(tarea: Tarea) = tareaDao.eliminarTarea(tarea)
}