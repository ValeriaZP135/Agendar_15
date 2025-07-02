package com.tecsup.agendar_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.data.database.entities.Evento
import com.tecsup.agendar_15.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel(
    private val repository: AgendarRepository,
    private val userId: String
) : ViewModel() {

    private val _events = MutableLiveData<List<Evento>>()
    val events: LiveData<List<Evento>> = _events

    private val _selectedDateEvents = MutableLiveData<List<Evento>>()
    val selectedDateEvents: LiveData<List<Evento>> = _selectedDateEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadEvents()
    }

    fun loadEvents() {
        repository.getEventosPorUsuario(userId).observeForever { eventos ->
            _events.value = eventos
        }
    }

    fun loadEventsForDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val startOfDay = DateUtils.getStartOfDay(calendar.timeInMillis)
        val endOfDay = DateUtils.getEndOfDay(calendar.timeInMillis)

        repository.getEventosPorFecha(userId, startOfDay, endOfDay).observeForever { eventos ->
            _selectedDateEvents.value = eventos
        }
    }

    fun createEvent(
        titulo: String,
        descripcion: String?,
        fechaInicio: Long,
        fechaFin: Long,
        cursoId: String? = null,
        ubicacion: String? = null,
        color: String = "#6200EE",
        esRecurrente: Boolean = false,
        tipoRecurrencia: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val nuevoEvento = Evento(
                    id = UUID.randomUUID().toString(),
                    usuarioId = userId,
                    cursoId = cursoId,
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    esRecurrente = esRecurrente,
                    tipoRecurrencia = tipoRecurrencia,
                    ubicacion = ubicacion,
                    color = color
                )

                repository.insertarEvento(nuevoEvento)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear evento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(evento: Evento) {
        viewModelScope.launch {
            try {
                repository.actualizarEvento(evento)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar evento: ${e.message}"
            }
        }
    }

    fun deleteEvent(evento: Evento) {
        viewModelScope.launch {
            try {
                repository.eliminarEvento(evento)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar evento: ${e.message}"
            }
        }
    }
}