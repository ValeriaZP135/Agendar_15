package com.tecsup.agendar_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.data.database.entities.Curso
import kotlinx.coroutines.launch
import java.util.*

class CoursesViewModel(
    private val repository: AgendarRepository,
    private val userId: String
) : ViewModel() {

    private val _courses = MutableLiveData<List<Curso>>()
    val courses: LiveData<List<Curso>> = _courses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCourses()
    }

    fun loadCourses() {
        repository.getCursosPorUsuario(userId).observeForever { cursos ->
            _courses.value = cursos
        }
    }

    fun createCourse(
        nombre: String,
        descripcion: String?,
        color: String,
        profesor: String?,
        salon: String?,
        creditos: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val nuevoCurso = Curso(
                    id = UUID.randomUUID().toString(),
                    usuarioId = userId,
                    nombre = nombre,
                    descripcion = descripcion,
                    color = color,
                    profesor = profesor,
                    salon = salon,
                    creditos = creditos
                )

                repository.insertarCurso(nuevoCurso)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear curso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCourse(curso: Curso) {
        viewModelScope.launch {
            try {
                repository.actualizarCurso(curso)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar curso: ${e.message}"
            }
        }
    }

    fun deleteCourse(curso: Curso) {
        viewModelScope.launch {
            try {
                repository.eliminarCurso(curso)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar curso: ${e.message}"
            }
        }
    }
}