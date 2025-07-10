package com.tecsup.agendar_15.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateCourseBinding
import com.tecsup.agendar_15.ui.adapters.ColorPickerAdapter
import com.tecsup.agendar_15.utils.AnimationUtils

class CreateCourseDialog : DialogFragment() {

    private var _binding: DialogCreateCourseBinding? = null
    private val binding get() = _binding!!

    private lateinit var colorAdapter: ColorPickerAdapter
    private var selectedColor: String = "#6200EE" // Color por defecto

    private val courseColors = listOf(
        "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03DAC6", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107",
        "#FF9800", "#FF5722", "#795548", "#607D8B"
    )

    companion object {
        fun newInstance(): CreateCourseDialog {
            return CreateCourseDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        setupColorPicker()
        startAnimations()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupUI() {
        // Configurar créditos por defecto
        binding.etCredits.setText("3")
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
            saveCourse()
        }

        // Validación en tiempo real del nombre
        binding.etCourseName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCourseName()
            }
        }

        // Validación de créditos
        binding.etCredits.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCredits()
            }
        }
    }

    private fun setupColorPicker() {
        colorAdapter = ColorPickerAdapter(courseColors) { color ->
            selectedColor = color
            updateColorPreview()

            // Animación de selección
            AnimationUtils.pulse(binding.colorPreview, 1.2f, 300)
        }

        binding.recyclerViewColors.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = colorAdapter
        }

        // Seleccionar primer color por defecto
        colorAdapter.selectColor(selectedColor)
        updateColorPreview()
    }

    private fun updateColorPreview() {
        binding.colorPreview.setCardBackgroundColor(android.graphics.Color.parseColor(selectedColor))
        binding.tvSelectedColor.text = "Color seleccionado"
    }

    private fun validateCourseName(): Boolean {
        val nombre = binding.etCourseName.text.toString().trim()

        return when {
            nombre.isEmpty() -> {
                binding.tilCourseName.error = "El nombre del curso es obligatorio"
                false
            }
            nombre.length < 3 -> {
                binding.tilCourseName.error = "El nombre debe tener al menos 3 caracteres"
                false
            }
            else -> {
                binding.tilCourseName.error = null
                true
            }
        }
    }

    private fun validateCredits(): Boolean {
        val creditsText = binding.etCredits.text.toString().trim()

        return when {
            creditsText.isEmpty() -> {
                binding.tilCredits.error = "Los créditos son obligatorios"
                false
            }
            else -> {
                try {
                    val credits = creditsText.toInt()
                    when {
                        credits < 1 -> {
                            binding.tilCredits.error = "Los créditos deben ser mayor a 0"
                            false
                        }
                        credits > 10 -> {
                            binding.tilCredits.error = "Los créditos no pueden ser mayor a 10"
                            false
                        }
                        else -> {
                            binding.tilCredits.error = null
                            true
                        }
                    }
                } catch (e: NumberFormatException) {
                    binding.tilCredits.error = "Ingresa un número válido"
                    false
                }
            }
        }
    }

    private fun saveCourse() {
        // Validar todos los campos
        val isNameValid = validateCourseName()
        val isCreditsValid = validateCredits()

        if (!isNameValid || !isCreditsValid) {
            // Animar campos con error
            if (!isNameValid) AnimationUtils.pulse(binding.tilCourseName, 1.1f, 300)
            if (!isCreditsValid) AnimationUtils.pulse(binding.tilCredits, 1.1f, 300)
            return
        }

        // Obtener datos del formulario
        val nombre = binding.etCourseName.text.toString().trim()
        val descripcion = binding.etCourseDescription.text.toString().trim()
        val profesor = binding.etProfessor.text.toString().trim()
        val salon = binding.etClassroom.text.toString().trim()
        val creditos = binding.etCredits.text.toString().toInt()

        // TODO: Integrar con ViewModel cuando esté Room funcionando
        // Por ahora mostrar mensaje de éxito

        // Animación de éxito
        AnimationUtils.checkmarkAnimation(binding.btnSave)

        Snackbar.make(
            requireView(),
            "✅ Curso '$nombre' creado exitosamente",
            Snackbar.LENGTH_SHORT
        ).show()

        // Cerrar diálogo con animación
        AnimationUtils.fadeOut(binding.root) {
            dismiss()
        }
    }

    private fun startAnimations() {
        // Animación de entrada del diálogo
        AnimationUtils.slideInFromBottom(binding.root, 400)

        // Animación escalonada de los campos
        val campos = listOf(
            binding.tilCourseName,
            binding.tilCourseDescription,
            binding.tilProfessor,
            binding.tilClassroom,
            binding.tilCredits,
            binding.colorPickerSection,
            binding.recyclerViewColors
        )

        campos.forEachIndexed { index, campo ->
            campo.alpha = 0f
            campo.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((index * 60).toLong())
                .start()
        }

        // Animación especial para la vista previa del color
        binding.colorPreview.scaleX = 0f
        binding.colorPreview.scaleY = 0f
        binding.colorPreview.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(300)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}