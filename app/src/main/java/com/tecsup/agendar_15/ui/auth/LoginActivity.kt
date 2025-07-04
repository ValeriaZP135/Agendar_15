package com.tecsup.agendar_15.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendar_15.databinding.ActivityLoginBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.ui.main.MainActivity
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.AuthViewModel
import com.tecsup.agendar_15.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        AnimationUtils.addRippleEffect(binding.btnLogin)
        AnimationUtils.addRippleEffect(binding.tvRegister)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            AnimationUtils.pulse(binding.tvRegister)
            startActivity(Intent(this, RegisterActivity::class.java))
            AnimationUtils.overrideActivityTransition(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun startAnimations() {
        // Animación del logo
        AnimationUtils.scaleIn(binding.logo, 600)

        // Animación del título
        binding.titleLogin.alpha = 0f
        binding.titleLogin.translationY = 50f
        binding.titleLogin.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(300)
            .start()

        // Animación de los campos
        AnimationUtils.slideInFromBottom(binding.tilEmail, 400)

        binding.tilPassword.alpha = 0f
        binding.tilPassword.translationY = 100f
        binding.tilPassword.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(200)
            .start()

        // Animación del botón
        AnimationUtils.scaleIn(binding.btnLogin, 500)

        // Animación del texto de registro
        AnimationUtils.fadeIn(binding.tvRegister, 400)
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // 1. Validaciones de entrada
        var hasError = false

        if (email.isEmpty()) {
            binding.tilEmail.error = "El correo no puede estar vacío"
            hasError = true
        } else if (!isValidEmail(email)) { // Función auxiliar para validar formato de email
            binding.tilEmail.error = "Formato de correo inválido"
            hasError = true
        } else {
            binding.tilEmail.error = null // Limpia el error si todo está bien
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña no puede estar vacía"
            hasError = true
        } else {
            binding.tilPassword.error = null // Limpia el error si todo está bien
        }

        if (hasError) {
            // Muestra un Toast genérico solo si hay errores que no se muestran en los campos
            // O simplemente retorna, ya que los errores están en los campos
            Toast.makeText(this, "Por favor corrige los errores", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Animación del botón durante carga
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Iniciando..."

        // 3. Llamada al ViewModel para iniciar sesión
        viewModel.login(email, password) { usuario ->
            runOnUiThread {
                binding.btnLogin.isEnabled = true // Siempre re-habilitar el botón
                binding.btnLogin.text = "Iniciar Sesión" // Restaurar texto

                if (usuario != null) {
                    // Inicio de sesión exitoso
                    prefsManager.isLoggedIn = true
                    prefsManager.userId = usuario.id

                    Toast.makeText(this, "¡Bienvenido ${usuario.nombre}!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    AnimationUtils.overrideActivityTransition(this, android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Cierra la actividad de login
                } else {
                    // Credenciales incorrectas
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    // Animación de error en el campo de contraseña
                    AnimationUtils.pulse(binding.tilPassword, 1.1f, 300)
                }
            }
        }
    }

    // Función auxiliar para validar el formato de correo electrónico
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}