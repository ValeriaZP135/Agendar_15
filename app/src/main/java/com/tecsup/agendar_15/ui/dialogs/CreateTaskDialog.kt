package com.tecsup.agendar_15.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateTaskBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.TasksViewModel
import com.tecsup.agendar_15.viewmodel.TasksViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskDialog : DialogFragment() {

    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TasksViewModel
    private var selectedDate: Long? = null
    private var selectedPriority: Int = 1

    companion object {
        fun newInstance(): CreateTaskDialog {
            return CreateTaskDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        setupListeners()
        startAnimations()
    }

    override fun onStart() {
        super.onStart()
        // Hacer el diálogo pantalla completa
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupViewModel() {
        val prefsManager = PreferencesManager(requireContext())
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

    private fun setupUI() {
        // Configurar spinner de cursos (temporal con datos estáticos)
        val cursosTemp = listOf("Sin curso", "Matemáticas", "Historia", "Ciencias")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cursosTemp)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCourse.adapter = adapter

        // Configurar chips de prioridad
        binding.chipPriorityLow.isChecked = true
        selectedPriority = 1
    }

    private fun setupListeners() {
        // Botón cancelar
        binding.btnCancel.setOnClickListener {
            AnimationUtils.pulse(binding.btnCancel, 1.1f, 150)
            dismiss()
        }

        // Botón guardar
        binding.btnSave.setOnClickListener {
            AnimationUtils.pulse(binding.btnSave, 1.1f, 150)
            saveTask()
        }

        // Campo de fecha
        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }

        // Campo de hora
        binding.etDueTime.setOnClickListener {
            showTimePicker()
        }

        // Chips de prioridad
        binding.chipGroupPriority.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.chipPriorityHigh -> 3
                R.id.chipPriorityMedium -> 2
                else -> 1
            }

            // Animación en el chip seleccionado
            val selectedChip = binding.chipGroupPriority.findViewById<View>(checkedId)
            selectedChip?.let { AnimationUtils.pulse(it, 1.1f, 200) }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis

                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etDueDate.setText(format.format(calendar.time))

                // Animación de confirmación
                AnimationUtils.pulse(binding.etDueDate, 1.05f, 200)
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
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeCalendar = Calendar.getInstance()
                timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                timeCalendar.set(Calendar.MINUTE, minute)

                binding.etDueTime.setText(timeFormat.format(timeCalendar.time))

                // Si hay fecha seleccionada, agregar la hora
                selectedDate?.let { date ->
                    val dateCalendar = Calendar.getInstance()
                    dateCalendar.timeInMillis = date
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    dateCalendar.set(Calendar.MINUTE, minute)
                    selectedDate = dateCalendar.timeInMillis
                }

                // Animación de confirmación
                AnimationUtils.pulse(binding.etDueTime, 1.05f, 200)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val titulo = binding.etTaskTitle.text.toString().trim()
        val descripcion = binding.etTaskDescription.text.toString().trim()

        // Validaciones
        if (titulo.isEmpty()) {
            binding.tilTaskTitle.error = "El título es obligatorio"
            AnimationUtils.pulse(binding.tilTaskTitle, 1.1f, 300)
            return
        }

        // Limpiar errores
        binding.tilTaskTitle.error = null

        // Crear tarea
        viewModel.createTask(
            titulo = titulo,
            descripcion = descripcion.ifEmpty { null },
            fechaVencimiento = selectedDate,
            prioridad = selectedPriority,
            cursoId = null // TODO: Implementar selección de curso
        )

        // Mostrar mensaje de éxito
        Snackbar.make(requireView(), "✅ Tarea creada exitosamente", Snackbar.LENGTH_SHORT).show()

        // Cerrar diálogo con animación
        AnimationUtils.fadeOut(binding.root) {
            dismiss()
        }
    }

    private fun startAnimations() {
        // Animación de entrada del diálogo
        AnimationUtils.slideInFromBottom(binding.root, 400)

        // Animación de los campos
        val campos = listOf(
            binding.tilTaskTitle,
            binding.tilTaskDescription,
            binding.spinnerCourse,
            binding.etDueDate,
            binding.etDueTime,
            binding.chipGroupPriority
        )

        campos.forEachIndexed { index, campo ->
            campo.alpha = 0f
            campo.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((index * 50).toLong())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}