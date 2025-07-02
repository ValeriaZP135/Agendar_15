package com.tecsup.agendar_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.data.database.entities.Tarea
import kotlinx.coroutines.launch
import java.util.*

class TasksViewModel(
    private val repository: AgendarRepository,
    private val userId: String
) : ViewModel() {

    private val _tasks = MutableLiveData<List<Tarea>>()
    val tasks: LiveData<List<Tarea>> = _tasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadTasks()
    }

    fun loadTasks() {
        repository.getTareasPorUsuario(userId).observeForever { tareas ->
            _tasks.value = tareas
        }
    }

    fun loadPendingTasks() {
        repository.getTareasPendientes(userId).observeForever { tareas ->
            _tasks.value = tareas
        }
    }

    fun createTask(
        titulo: String,
        descripcion: String?,
        fechaVencimiento: Long?,
        prioridad: Int,
        cursoId: String?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val nuevaTarea = Tarea(
                    id = UUID.randomUUID().toString(),
                    usuarioId = userId,
                    cursoId = cursoId,
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaVencimiento = fechaVencimiento,
                    prioridad = prioridad
                )

                repository.insertarTarea(nuevaTarea)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear tarea: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repository.actualizarTarea(tarea)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar tarea: ${e.message}"
            }
        }
    }

    fun markTaskCompleted(tarea: Tarea) {
        viewModelScope.launch {
            try {
                val tareaActualizada = tarea.copy(
                    completada = true,
                    fechaCompletado = System.currentTimeMillis()
                )
                repository.actualizarTarea(tareaActualizada)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al marcar tarea como completada: ${e.message}"
            }
        }
    }

    fun deleteTask(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repository.eliminarTarea(tarea)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar tarea: ${e.message}"
            }
        }
    }

    fun filterTasks(filter: TaskFilter) {
        val currentTasks = _tasks.value ?: return

        val filteredTasks = when (filter) {
            TaskFilter.ALL -> currentTasks
            TaskFilter.PENDING -> currentTasks.filter { !it.completada }
            TaskFilter.COMPLETED -> currentTasks.filter { it.completada }
            TaskFilter.OVERDUE -> currentTasks.filter {
                !it.completada && it.fechaVencimiento != null &&
                        it.fechaVencimiento < System.currentTimeMillis()
            }
            TaskFilter.HIGH_PRIORITY -> currentTasks.filter { it.prioridad == 3 }
        }

        _tasks.value = filteredTasks
    }

    enum class TaskFilter {
        ALL, PENDING, COMPLETED, OVERDUE, HIGH_PRIORITY
    }
}