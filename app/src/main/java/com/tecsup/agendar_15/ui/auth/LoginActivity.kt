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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Animación del botón durante carga
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Iniciando..."

        viewModel.login(email, password) { usuario ->
            runOnUiThread {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Iniciar Sesión"

                if (usuario != null) {
                    prefsManager.isLoggedIn = true
                    prefsManager.userId = usuario.id

                    Toast.makeText(this, "¡Bienvenido ${usuario.nombre}!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    AnimationUtils.overrideActivityTransition(this, android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    // Animación de error
                    AnimationUtils.pulse(binding.tilPassword, 1.1f, 300)
                }
            }
        }
    }
}