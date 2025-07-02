package com.tecsup.agendar_15.ui.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.tecsup.agendar_15.databinding.FragmentCoursesBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.ui.adapters.CourseAdapter
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.CoursesViewModel
import com.tecsup.agendar_15.viewmodel.CoursesViewModelFactory

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CoursesViewModel
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
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

        val factory = CoursesViewModelFactory(repository, userId)
        viewModel = ViewModelProvider(this, factory)[CoursesViewModel::class.java]
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter { curso ->
            // Navegar a detalle del curso
            AnimationUtils.pulse(view, 1.05f, 200)
            // TODO: Implementar navegación
        }

        binding.recyclerViewCourses.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = courseAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }

    private fun observeData() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)

            if (courses.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
            }
        }
    }

    private fun showEmptyState() {
        AnimationUtils.fadeOut(binding.recyclerViewCourses, 300)
        AnimationUtils.fadeIn(binding.layoutEmptyState, 400)
    }

    private fun hideEmptyState() {
        AnimationUtils.fadeOut(binding.layoutEmptyState, 300)
        AnimationUtils.fadeIn(binding.recyclerViewCourses, 400)
    }

    private fun startAnimations() {
        // Animación del RecyclerView
        binding.recyclerViewCourses.alpha = 0f
        binding.recyclerViewCourses.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}