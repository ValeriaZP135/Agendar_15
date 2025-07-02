package com.tecsup.agendar_15.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.ActivityMainBinding
import com.tecsup.agendar_15.ui.auth.LoginActivity
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_calendar, R.id.nav_courses, R.id.nav_tasks, R.id.nav_profile
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        binding.bottomNavigation.setupWithNavController(navController)

        setupFab()
        setupAnimations()
        setupNavigationListener(navView)
    }

    private fun setupNavigationListener(navView: NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> {
                    // Manejo normal de navegación
                    val navController = findNavController(R.id.nav_host_fragment)
                    navController.navigate(menuItem.itemId)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun logout() {
        prefsManager.clearUserData()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        AnimationUtils.overrideActivityTransition(this, android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Animación del FAB
            AnimationUtils.pulse(binding.fabAdd, 1.2f, 200)

            // Determinar qué diálogo mostrar basado en el fragment actual
            val navController = findNavController(R.id.nav_host_fragment)
            when (navController.currentDestination?.id) {
                R.id.nav_calendar -> {
                    showCreateEventDialog()
                }
                R.id.nav_courses -> {
                    showCreateCourseDialog()
                }
                R.id.nav_tasks -> {
                    showCreateTaskDialog()
                }
                else -> {
                    Snackbar.make(binding.root, "Función no disponible en esta sección", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupAnimations() {
        // Animación de entrada del FAB
        binding.fabAdd.alpha = 0f
        binding.fabAdd.scaleX = 0f
        binding.fabAdd.scaleY = 0f

        binding.fabAdd.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(500)
            .start()
    }

    private fun showCreateEventDialog() {
        // TODO: Implementar diálogo de crear evento
        Snackbar.make(binding.root, "Crear evento - Por implementar", Snackbar.LENGTH_SHORT).show()
    }

    private fun showCreateCourseDialog() {
        // TODO: Implementar diálogo de crear curso
        Snackbar.make(binding.root, "Crear curso - Por implementar", Snackbar.LENGTH_SHORT).show()
    }

    private fun showCreateTaskDialog() {
        // TODO: Implementar diálogo de crear tarea
        Snackbar.make(binding.root, "Crear tarea - Por implementar", Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // TODO: Abrir configuración
                Snackbar.make(binding.root, "Configuración - Por implementar", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}