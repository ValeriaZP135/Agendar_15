package com.tecsup.agendar_15.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.ActivitySplashBinding
import com.tecsup.agendar_15.ui.main.MainActivity
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        // Animaciones de entrada
        startAnimations()

        // Navegar después de 2.5 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2500)
    }

    private fun startAnimations() {
        // Animación del logo
        binding.logo.alpha = 0f
        binding.logo.scaleX = 0.5f
        binding.logo.scaleY = 0.5f

        binding.logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(300)
            .start()

        // Animación del nombre de la app
        AnimationUtils.fadeIn(binding.appName, 600)

        // Animación del progress bar
        Handler(Looper.getMainLooper()).postDelayed({
            AnimationUtils.fadeIn(binding.progressBar, 400)
        }, 1000)
    }

    private fun navigateToNextScreen() {
        val intent = if (prefsManager.isLoggedIn) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        AnimationUtils.overrideActivityTransition(this, android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}