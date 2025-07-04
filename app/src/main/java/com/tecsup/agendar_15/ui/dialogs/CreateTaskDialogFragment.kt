package com.tecsup.agendar_15.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateTaskBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.TasksViewModel
import com.tecsup.agendar_15.viewmodel.TasksViewModelFactory
import com.tecsup.agendar_15.viewmodel.CoursesViewModel
import com.tecsup.agendar_15.viewmodel.CoursesViewModelFactory
import java.util.*

class CreateTaskDialogFragment : DialogFragment() {

    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var coursesViewModel: CoursesViewModel
    private lateinit var prefsManager: PreferencesManager

    private var selectedDueDate: Long? = null
    private var selectedCourseId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModels()
        setupUI()
        setupListeners()
        loadCourses()
        startAnimations()
    }

    private fun setupViewModels() {
        prefsManager = PreferencesManager(requireContext())
        val userId = prefsManager.userId ?: return

        val database = AgendarDatabase.getDatabase(requireContext())
        val repository = AgendarRepository(
            database.usuarioDao(),
            database.cursoDao(),
            database.eventoDao(),
            database.tareaDao()
        )

        val tasksFactory = TasksViewModelFactory(repository, userId)
        tasksViewModel = ViewModelProvider(this, tasksFactory)[TasksViewModel::class.java]

        val coursesFactory = CoursesViewModelFactory(repository, userId)
        coursesViewModel = ViewModelProvider(this, coursesFactory)[CoursesViewModel::class.java]
    }

    private fun setupUI() {
        // Configurar prioridad por defecto
        binding.chipPriorityMedium.isChecked = true
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            saveTask()
        }

        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }

        binding.etDueTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun loadCourses() {
        coursesViewModel.courses.observe(viewLifecycleOwner) { courses ->
            val courseNames = mutableListOf("Sin curso")
            val courseIds = mutableListOf<String?>(null)

            courses.forEach { course ->
                courseNames.add(course.nombre)
                courseIds.add(course.id)
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCourse.adapter = adapter

            binding.spinnerCourse.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCourseId = courseIds[position]
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                if (selectedDueDate != null) {
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.timeInMillis = selectedDueDate!!
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    selectedCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                }

                selectedDueDate = selectedCalendar.timeInMillis
                binding.etDueDate.setText("${dayOfMonth}/${month + 1}/${year}")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance()
                if (selectedDueDate != null) {
                    selectedCalendar.timeInMillis = selectedDueDate!!
                }
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)

                selectedDueDate = selectedCalendar.timeInMillis
                binding.etDueTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val priority = when {
            binding.chipPriorityHigh.isChecked -> 3
            binding.chipPriorityMedium.isChecked -> 2
            else -> 1
        }

        tasksViewModel.createTask(
            titulo = title,
            descripcion = description.ifEmpty { null },
            fechaVencimiento = selectedDueDate,
            prioridad = priority,
            cursoId = selectedCourseId
        )

        Toast.makeText(requireContext(), "Tarea creada exitosamente", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun startAnimations() {
        AnimationUtils.slideInFromBottom(binding.root, 400)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}