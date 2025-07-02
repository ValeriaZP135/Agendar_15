package com.tecsup.agendar_15.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendar_15.databinding.ActivityRegisterBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.ui.main.MainActivity
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.AuthViewModel
import com.tecsup.agendar_15.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupListeners()
        startAnimations()
    }

    private fun setupViewModel() {
        val database = AgendarDatabase.getDatabase(this)
        val repository = AgendarRepository(
            database.usuarioDao(),
            database.cursoDao(),
            database.eventoDao(),
            database.tareaDao()
        )
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        prefsManager = PreferencesManager(this)
    }

    private fun setupUI() {
        // Configurar efectos ripple
        AnimationUtils.addRippleEffect(binding.btnRegister)
        AnimationUtils.addRippleEffect(binding.tvLogin)
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            AnimationUtils.pulse(binding.tvLogin)
            finish()
            AnimationUtils.overrideActivityTransition(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun startAnimations() {
        // Animación del logo
        AnimationUtils.scaleIn(binding.logo, 600)

        // Animación del título
        binding.titleRegister.alpha = 0f
        binding.titleRegister.translationY = 50f
        binding.titleRegister.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(300)
            .start()

        // Animación de los campos (entrada escalonada)
        val campos = listOf(
            binding.tilName,
            binding.tilEmail,
            binding.tilPassword,
            binding.tilConfirmPassword
        )

        campos.forEachIndexed { index, campo ->
            campo.alpha = 0f
            campo.translationY = 100f
            campo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((index * 100 + 400).toLong())
                .start()
        }

        // Animación del botón
        AnimationUtils.scaleIn(binding.btnRegister, 500)

        // Animación del texto de login
        AnimationUtils.fadeIn(binding.tvLogin, 400)
    }

    private fun performRegister() {
        val nombre = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            AnimationUtils.pulse(binding.tilConfirmPassword, 1.1f, 300)
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            AnimationUtils.pulse(binding.tilPassword, 1.1f, 300)
            return
        }

        // Animación del botón durante carga
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registrando..."

        viewModel.register(nombre, email, password) { success, error ->
            runOnUiThread {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Registrarse"

                if (success) {
                    Toast.makeText(this, "¡Registro exitoso! Iniciando sesión...", Toast.LENGTH_SHORT).show()

                    // Auto-login después del registro
                    viewModel.login(email, password) { usuario ->
                        runOnUiThread {
                            if (usuario != null) {
                                prefsManager.isLoggedIn = true
                                prefsManager.userId = usuario.id

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                AnimationUtils.overrideActivityTransition(this, android.R.anim.fade_in, android.R.anim.fade_out)
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, error ?: "Error al registrar", Toast.LENGTH_SHORT).show()
                    // Animación de error
                    AnimationUtils.pulse(binding.tilEmail, 1.1f, 300)
                }
            }
        }
    }
}