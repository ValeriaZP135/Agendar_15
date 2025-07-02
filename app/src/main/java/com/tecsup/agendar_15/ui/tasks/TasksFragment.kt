package com.tecsup.agendar_15.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.FragmentTasksBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.ui.adapters.TaskAdapter
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.TasksViewModel
import com.tecsup.agendar_15.viewmodel.TasksViewModelFactory

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TasksViewModel
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupFilters()
        observeData()
        setupSwipeToDelete()
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

        val factory = TasksViewModelFactory(repository, userId)
        viewModel = ViewModelProvider(this, factory)[TasksViewModel::class.java]
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { tarea ->
                // Navegar a detalle de tarea
                // TODO: Implementar navegación
            },
            onTaskCompleted = { tarea ->
                viewModel.markTaskCompleted(tarea)
                showCompletionFeedback()
            },
            onTaskLongClick = { tarea ->
                // Mostrar menú contextual
                showTaskContextMenu(tarea)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }
    }

    private fun setupFilters() {
        // CORRECCIÓN: Usar setOnCheckedStateChangeListener
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = if (checkedIds.isNotEmpty()) checkedIds.first() else View.NO_ID

            val filter = when (checkedId) {
                R.id.chipAll -> TasksViewModel.TaskFilter.ALL
                R.id.chipPending -> TasksViewModel.TaskFilter.PENDING
                R.id.chipCompleted -> TasksViewModel.TaskFilter.COMPLETED
                R.id.chipOverdue -> TasksViewModel.TaskFilter.OVERDUE
                else -> TasksViewModel.TaskFilter.ALL
            }

            // Animación en el chip seleccionado
            if (checkedId != View.NO_ID) {
                val selectedChip = binding.chipGroupFilters.findViewById<View>(checkedId)
                selectedChip?.let { AnimationUtils.pulse(it, 1.1f, 200) }
            }

            viewModel.filterTasks(filter)
        }
    }

    private fun observeData() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)

            // Mostrar/ocultar estado vacío
            if (tasks.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // CORRECCIÓN: Usar bindingAdapterPosition
                val position = viewHolder.bindingAdapterPosition
                val tarea = taskAdapter.currentList[position]

                // Animación de deslizamiento
                AnimationUtils.slideOutToBottom(viewHolder.itemView) {
                    viewModel.deleteTask(tarea)

                    // Snackbar con opción de deshacer
                    Snackbar.make(binding.root, "Tarea eliminada", Snackbar.LENGTH_LONG)
                        .setAction("Deshacer") {
                            viewModel.createTask(
                                tarea.titulo,
                                tarea.descripcion,
                                tarea.fechaVencimiento,
                                tarea.prioridad,
                                tarea.cursoId
                            )
                        }
                        .show()
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)
    }

    private fun showCompletionFeedback() {
        val snackbar = Snackbar.make(
            binding.root,
            "¡Tarea completada! 🎉",
            Snackbar.LENGTH_SHORT
        )
        snackbar.show()
    }

    private fun showTaskContextMenu(tarea: com.tecsup.agendar_15.data.database.entities.Tarea) {
        // TODO: Implementar menú contextual con opciones: Editar, Eliminar, Duplicar
    }

    private fun showEmptyState() {
        AnimationUtils.fadeOut(binding.recyclerViewTasks, 300)
        AnimationUtils.fadeIn(binding.layoutEmptyState, 400)
    }

    private fun hideEmptyState() {
        AnimationUtils.fadeOut(binding.layoutEmptyState, 300)
        AnimationUtils.fadeIn(binding.recyclerViewTasks, 400)
    }

    private fun startAnimations() {
        // Animación de filtros
        AnimationUtils.slideInFromBottom(binding.chipGroupFilters.parent as View, 400)

        // Animación del RecyclerView
        binding.recyclerViewTasks.alpha = 0f
        binding.recyclerViewTasks.animate()
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