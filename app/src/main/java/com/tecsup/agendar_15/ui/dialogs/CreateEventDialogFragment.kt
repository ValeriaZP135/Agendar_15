package com.tecsup.agendar_15.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateEventBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.CalendarViewModel
import com.tecsup.agendar_15.viewmodel.CalendarViewModelFactory
import com.tecsup.agendar_15.viewmodel.CoursesViewModel
import com.tecsup.agendar_15.viewmodel.CoursesViewModelFactory
import java.util.*

class CreateEventDialogFragment : DialogFragment() {

    private var _binding: DialogCreateEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var coursesViewModel: CoursesViewModel
    private lateinit var prefsManager: PreferencesManager

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null
    private var selectedCourseId: String? = null
    private var selectedColor = "#6200EE"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCreateEventBinding.inflate(inflater, container, false)
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

        val calendarFactory = CalendarViewModelFactory(repository, userId)
        calendarViewModel = ViewModelProvider(this, calendarFactory)[CalendarViewModel::class.java]

        val coursesFactory = CoursesViewModelFactory(repository, userId)
        coursesViewModel = ViewModelProvider(this, coursesFactory)[CoursesViewModel::class.java]
    }

    private fun setupUI() {
        // Configurar fecha y hora por defecto (ahora)
        val now = Calendar.getInstance()
        selectedStartDate = now.timeInMillis

        val endTime = Calendar.getInstance()
        endTime.add(Calendar.HOUR, 1)
        selectedEndDate = endTime.timeInMillis

        updateDateTimeFields()
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            saveEvent()
        }

        binding.etStartDate.setOnClickListener { showStartDatePicker() }
        binding.etStartTime.setOnClickListener { showStartTimePicker() }
        binding.etEndDate.setOnClickListener { showEndDatePicker() }
        binding.etEndTime.setOnClickListener { showEndTimePicker() }
    }

    private fun loadCourses() {
        coursesViewModel.courses.observe(viewLifecycleOwner) { courses ->
            val courseNames = mutableListOf("Sin curso")
            val courseIds = mutableListOf<String?>(null)
            val courseColors = mutableListOf("#6200EE")

            courses.forEach { course ->
                courseNames.add(course.nombre)
                courseIds.add(course.id)
                courseColors.add(course.color)
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCourse.adapter = adapter

            binding.spinnerCourse.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCourseId = courseIds[position]
                    selectedColor = courseColors[position]
                    binding.viewSelectedColor.setBackgroundColor(Color.parseColor(selectedColor))
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedStartDate != null) calendar.timeInMillis = selectedStartDate!!

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedStartDate ?: System.currentTimeMillis()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedStartDate = selectedCalendar.timeInMillis
                updateDateTimeFields()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showStartTimePicker() {
        val calendar = Calendar.getInstance()
        if (selectedStartDate != null) calendar.timeInMillis = selectedStartDate!!

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedStartDate ?: System.currentTimeMillis()
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedStartDate = selectedCalendar.timeInMillis

                // Actualizar hora de fin automáticamente (+1 hora)
                val endCalendar = Calendar.getInstance()
                endCalendar.timeInMillis = selectedStartDate!!
                endCalendar.add(Calendar.HOUR, 1)
                selectedEndDate = endCalendar.timeInMillis

                updateDateTimeFields()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showEndDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedEndDate != null) calendar.timeInMillis = selectedEndDate!!

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedEndDate ?: System.currentTimeMillis()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedEndDate = selectedCalendar.timeInMillis
                updateDateTimeFields()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showEndTimePicker() {
        val calendar = Calendar.getInstance()
        if (selectedEndDate != null) calendar.timeInMillis = selectedEndDate!!

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedEndDate ?: System.currentTimeMillis()
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedEndDate = selectedCalendar.timeInMillis
                updateDateTimeFields()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeFields() {
        selectedStartDate?.let { startTime ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTime

            binding.etStartDate.setText("${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")
            binding.etStartTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
        }

        selectedEndDate?.let { endTime ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = endTime

            binding.etEndDate.setText("${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")
            binding.etEndTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
        }
    }

    private fun saveEvent() {
        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStartDate == null || selectedEndDate == null) {
            Toast.makeText(requireContext(), "Selecciona fecha y hora de inicio y fin", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedEndDate!! <= selectedStartDate!!) {
            Toast.makeText(requireContext(), "La hora de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        calendarViewModel.createEvent(
            titulo = title,
            descripcion = description.ifEmpty { null },
            fechaInicio = selectedStartDate!!,
            fechaFin = selectedEndDate!!,
            cursoId = selectedCourseId,
            ubicacion = location.ifEmpty { null },
            color = selectedColor
        )

        Toast.makeText(requireContext(), "Evento creado exitosamente", Toast.LENGTH_SHORT).show()
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