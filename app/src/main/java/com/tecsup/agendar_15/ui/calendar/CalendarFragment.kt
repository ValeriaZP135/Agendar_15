package com.tecsup.agendar_15.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tecsup.agendar_15.databinding.FragmentCalendarBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.ui.adapters.EventAdapter
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.CalendarViewModel
import com.tecsup.agendar_15.viewmodel.CalendarViewModelFactory

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CalendarViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupCalendarView()
        observeData()
        startAnimations()
    }

    private fun setupViewModel() {
        prefsManager = PreferencesManager(requireContext())
        val userId = prefsManager.userId ?: return

        val database = AgendarDatabase.getDatabase(requireContext())
        val repository = AgendarRepository(
            database.usuarioDao(),
            database.cursoDao(),
            database.eventoDao(),
            database.tareaDao()
        )

        val factory = CalendarViewModelFactory(repository, userId)
        viewModel = ViewModelProvider(this, factory)[CalendarViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { evento ->
            // Manejar click en evento
            AnimationUtils.pulse(view, 1.1f, 200)
            // TODO: Navegar a detalle del evento
        }

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Manejar cambio de fecha
            loadEventsForDate(year, month, dayOfMonth)
        }
    }

    private fun observeData() {
        viewModel.selectedDateEvents.observe(viewLifecycleOwner) { eventos ->
            eventAdapter.submitList(eventos)

            if (eventos.isEmpty()) {
                binding.tvEventsTitle.text = "No hay eventos para este día"
            } else {
                binding.tvEventsTitle.text = "Eventos del día (${eventos.size})"
            }
        }
    }

    private fun loadEventsForDate(year: Int, month: Int, day: Int) {
        viewModel.loadEventsForDate(year, month, day)
    }

    private fun startAnimations() {
        // Animación del calendario
        AnimationUtils.fadeIn(binding.calendarView, 500)

        // Animación del RecyclerView
        binding.recyclerViewEvents.alpha = 0f
        binding.recyclerViewEvents.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}