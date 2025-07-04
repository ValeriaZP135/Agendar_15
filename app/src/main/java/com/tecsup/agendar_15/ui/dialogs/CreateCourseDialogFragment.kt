package com.tecsup.agendar_15.ui.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateCourseBinding
import com.tecsup.agendar_15.data.database.AgendarDatabase
import com.tecsup.agendar_15.data.repository.AgendarRepository
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.PreferencesManager
import com.tecsup.agendar_15.viewmodel.CoursesViewModel
import com.tecsup.agendar_15.viewmodel.CoursesViewModelFactory

class CreateCourseDialogFragment : DialogFragment() {

    private var _binding: DialogCreateCourseBinding? = null
    private val binding get() = _binding!!

    private lateinit var coursesViewModel: CoursesViewModel
    private lateinit var prefsManager: PreferencesManager

    private var selectedColor = "#6200EE" // Color por defecto

    private val courseColors = arrayOf(
        "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03DAC5", "#4CAF50", "#8BC34A",
        "#CDDC39", "#FFEB3B", "#FF9800", "#FF5722"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCreateCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        setupListeners()
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
        coursesViewModel = ViewModelProvider(this, factory)[CoursesViewModel::class.java]
    }

    private fun setupUI() {
        setupColorPicker()
    }

    private fun setupColorPicker() {
        courseColors.forEachIndexed { index, colorHex ->
            val colorView = View(requireContext())
            val size = 120
            colorView.layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                setMargins(16, 16, 16, 16)
            }
            colorView.setBackgroundColor(Color.parseColor(colorHex))
            colorView.isClickable = true

            // Efecto ripple
            AnimationUtils.addRippleEffect(colorView)

            colorView.setOnClickListener {
                selectedColor = colorHex
                updateSelectedColor()
                AnimationUtils.pulse(colorView, 1.2f, 200)
            }

            binding.layoutColors.addView(colorView)
        }

        // Seleccionar primer color por defecto
        updateSelectedColor()
    }

    private fun updateSelectedColor() {
        binding.viewSelectedColor.setBackgroundColor(Color.parseColor(selectedColor))
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            AnimationUtils.pulse(it, 1.1f, 150)
            saveCourse()
        }
    }

    private fun saveCourse() {
        val name = binding.etCourseName.text.toString().trim()
        val description = binding.etCourseDescription.text.toString().trim()
        val professor = binding.etProfessor.text.toString().trim()
        val classroom = binding.etClassroom.text.toString().trim()
        val creditsText = binding.etCredits.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre del curso es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val credits = creditsText.toIntOrNull() ?: 0

        coursesViewModel.createCourse(
            nombre = name,
            descripcion = description.ifEmpty { null },
            color = selectedColor,
            profesor = professor.ifEmpty { null },
            salon = classroom.ifEmpty { null },
            creditos = credits
        )

        Toast.makeText(requireContext(), "Curso creado exitosamente", Toast.LENGTH_SHORT).show()
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