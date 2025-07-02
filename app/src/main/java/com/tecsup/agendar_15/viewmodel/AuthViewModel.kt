package com.tecsup.agendar_15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.data.database.entities.Usuario
import kotlinx.coroutines.launch
import java.util.*

class AuthViewModel(private val repository: AgendarRepository) : ViewModel() {

    fun login(email: String, password: String, callback: (Usuario?) -> Unit) {
        viewModelScope.launch {
            val usuario = repository.login(email, password)
            callback(usuario)
        }
    }

    fun register(nombre: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Verificar si el email ya existe
                val existingUser = repository.getUsuarioPorEmail(email)
                if (existingUser != null) {
                    callback(false, "El email ya está registrado")
                    return@launch
                }

                // Crear nuevo usuario
                val nuevoUsuario = Usuario(
                    id = UUID.randomUUID().toString(),
                    nombre = nombre,
                    email = email,
                    password = password
                )

                repository.registrarUsuario(nuevoUsuario)
                callback(true, null)
            } catch (e: Exception) {
                callback(false, "Error al registrar usuario: ${e.message}")
            }
        }
    }
}