package com.tecsup.agendar_15.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController // Asegúrate de importar NavController
import androidx.navigation.fragment.NavHostFragment // <--- IMPORTANTE: Importa NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.ActivityMainBinding
import com.tecsup.agendar_15.ui.auth.LoginActivity
import com.tecsup.agendar_15.ui.dialogs.CreateCourseDialog
import com.tecsup.agendar_15.ui.dialogs.CreateEventDialog
import com.tecsup.agendar_15.ui.dialogs.CreateTaskDialog
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var navController: NavController // <--- Decláralo a nivel de clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // --- CAMBIO CLAVE AQUÍ ---
        // Obtener el NavHostFragment a través del FragmentManager
        // y luego su NavController.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // -------------------------

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
                    // Ya tenemos navController disponible como propiedad de la clase
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

            // Ya tenemos navController disponible como propiedad de la clase
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

    private fun showCreateTaskDialog() {
        val dialog = CreateTaskDialog.newInstance()
        dialog.show(supportFragmentManager, "CreateTaskDialog")

        // Animación del FAB
        AnimationUtils.fabMorphToDialog(binding.fabAdd, binding.root, 400)
    }

    private fun showCreateEventDialog() {
        val dialog = CreateEventDialog.newInstance()
        dialog.show(supportFragmentManager, "CreateEventDialog")

        // Animación del FAB
        AnimationUtils.pulse(binding.fabAdd, 1.2f, 200)
    }

    private fun showCreateCourseDialog() {
        val dialog = CreateCourseDialog.newInstance()
        dialog.show(supportFragmentManager, "CreateCourseDialog")

        // Animación del FAB
        AnimationUtils.pulse(binding.fabAdd, 1.2f, 200)
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
        // Ya tenemos navController disponible como propiedad de la clase
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}