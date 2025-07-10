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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.DialogCreateEventBinding
import com.tecsup.agendar_15.ui.adapters.RecurrenceAdapter
import java.text.SimpleDateFormat
import java.util.*

class CreateEventDialog : DialogFragment() {

    private var _binding: DialogCreateEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var recurrenceAdapter: RecurrenceAdapter

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null
    private var selectedColor: String = "#2196F3"
    private var selectedRecurrence: String = "NINGUNA"
    private var notificationMinutes: Int = 15

    private val recurrenceOptions = listOf(
        RecurrenceOption("NINGUNA", "Sin repetir", "📅"),
        RecurrenceOption("DIARIO", "Todos los días", "🔄"),
        RecurrenceOption("SEMANAL", "Cada semana", "📆"),
        RecurrenceOption("MENSUAL", "Cada mes", "🗓️"),
        RecurrenceOption("ANUAL", "Cada año", "🎂")
    )

    private val notificationOptions = listOf(
        "Sin notificación" to 0,
        "5 minutos antes" to 5,
        "15 minutos antes" to 15,
        "30 minutos antes" to 30,
        "1 hora antes" to 60,
        "1 día antes" to 1440
    )

    companion object {
        fun newInstance(): CreateEventDialog {
            return CreateEventDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        setupRecurrencePicker()
        setupNotificationSpinner()

        // OCULTAR SECCIÓN DE COLORES TEMPORALMENTE
        binding.layoutColorSection.visibility = View.GONE

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
        // Configurar fecha por defecto (hoy)
        val today = Calendar.getInstance()
        selectedStartDate = today.timeInMillis
        selectedEndDate = today.apply { add(Calendar.HOUR_OF_DAY, 1) }.timeInMillis

        updateDateTimeFields()
        updateColorPreview()
    }

    private fun setupListeners() {
        // Botón cancelar
        binding.btnCancel.setOnClickListener {
            pulseAnimation(binding.btnCancel)
            dismiss()
        }

        // Botón guardar
        binding.btnSave.setOnClickListener {
            pulseAnimation(binding.btnSave)
            saveEvent()
        }

        // Campos de fecha y hora
        binding.etStartDate.setOnClickListener { showStartDatePicker() }
        binding.etStartTime.setOnClickListener { showStartTimePicker() }
        binding.etEndDate.setOnClickListener { showEndDatePicker() }
        binding.etEndTime.setOnClickListener { showEndTimePicker() }

        // Switch de todo el día
        binding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            toggleAllDayMode(isChecked)
            pulseAnimation(binding.switchAllDay)
        }

        // Validaciones en tiempo real
        binding.etEventTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateTitle()
        }
    }

    private fun setupRecurrencePicker() {
        try {
            recurrenceAdapter = RecurrenceAdapter(recurrenceOptions) { recurrence ->
                selectedRecurrence = recurrence.type
                updateRecurrencePreview(recurrence)
                pulseAnimation(binding.recurrencePreview)
            }

            binding.recyclerViewRecurrence.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = recurrenceAdapter
            }

            // Seleccionar "NINGUNA" por defecto
            recurrenceAdapter.selectRecurrence("NINGUNA")
            updateRecurrencePreview(recurrenceOptions[0])
        } catch (e: Exception) {
            // Si falla, ocultar la sección
            binding.layoutRecurrenceSection.visibility = View.GONE
        }
    }

    private fun setupNotificationSpinner() {
        try {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                notificationOptions.map { it.first }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNotification.adapter = adapter

            // Seleccionar "15 minutos antes" por defecto
            binding.spinnerNotification.setSelection(2)
            notificationMinutes = 15
        } catch (e: Exception) {
            // Si falla, ocultar la sección
            binding.layoutNotificationSection.visibility = View.GONE
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        selectedStartDate?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedStartDate = calendar.timeInMillis

                // Ajustar fecha de fin si es necesario
                selectedEndDate?.let { endDate ->
                    if (endDate < selectedStartDate!!) {
                        selectedEndDate = selectedStartDate!! + (60 * 60 * 1000) // +1 hora
                    }
                }

                updateDateTimeFields()
                pulseAnimation(binding.etStartDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showStartTimePicker() {
        val calendar = Calendar.getInstance()
        selectedStartDate?.let { calendar.timeInMillis = it }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedStartDate = calendar.timeInMillis

                // Ajustar hora de fin automáticamente (+1 hora)
                selectedEndDate = selectedStartDate!! + (60 * 60 * 1000)

                updateDateTimeFields()
                pulseAnimation(binding.etStartTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showEndDatePicker() {
        val calendar = Calendar.getInstance()
        selectedEndDate?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedEndDate = calendar.timeInMillis

                // Validar que sea después del inicio
                if (selectedEndDate!! < selectedStartDate!!) {
                    Toast.makeText(context, "La fecha de fin debe ser posterior al inicio", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                updateDateTimeFields()
                pulseAnimation(binding.etEndDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showEndTimePicker() {
        val calendar = Calendar.getInstance()
        selectedEndDate?.let { calendar.timeInMillis = it }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedEndDate = calendar.timeInMillis

                // Validar que sea después del inicio
                if (selectedEndDate!! <= selectedStartDate!!) {
                    Toast.makeText(context, "La hora de fin debe ser posterior al inicio", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                updateDateTimeFields()
                pulseAnimation(binding.etEndTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun toggleAllDayMode(isAllDay: Boolean) {
        if (isAllDay) {
            // Ocultar campos de hora
            binding.layoutTimeFields.visibility = View.GONE

            // Ajustar fechas para todo el día
            selectedStartDate?.let { startDate ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = startDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                selectedStartDate = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                selectedEndDate = calendar.timeInMillis
            }
        } else {
            // Mostrar campos de hora
            binding.layoutTimeFields.visibility = View.VISIBLE
        }

        updateDateTimeFields()
    }

    private fun updateDateTimeFields() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        selectedStartDate?.let {
            binding.etStartDate.setText(dateFormat.format(Date(it)))
            if (!binding.switchAllDay.isChecked) {
                binding.etStartTime.setText(timeFormat.format(Date(it)))
            }
        }

        selectedEndDate?.let {
            binding.etEndDate.setText(dateFormat.format(Date(it)))
            if (!binding.switchAllDay.isChecked) {
                binding.etEndTime.setText(timeFormat.format(Date(it)))
            }
        }
    }

    private fun updateColorPreview() {
        try {
            binding.colorPreview.setCardBackgroundColor(Color.parseColor(selectedColor))
        } catch (e: Exception) {
            // Usar color por defecto si falla
            binding.colorPreview.setCardBackgroundColor(Color.BLUE)
        }
    }

    private fun updateRecurrencePreview(recurrence: RecurrenceOption) {
        binding.tvRecurrenceSelected.text = "${recurrence.emoji} ${recurrence.description}"
    }

    private fun validateTitle(): Boolean {
        val title = binding.etEventTitle.text.toString().trim()

        return when {
            title.isEmpty() -> {
                binding.tilEventTitle.error = "El título del evento es obligatorio"
                false
            }
            title.length < 3 -> {
                binding.tilEventTitle.error = "El título debe tener al menos 3 caracteres"
                false
            }
            else -> {
                binding.tilEventTitle.error = null
                true
            }
        }
    }

    private fun validateDates(): Boolean {
        return when {
            selectedStartDate == null || selectedEndDate == null -> {
                Toast.makeText(context, "Selecciona fechas válidas", Toast.LENGTH_SHORT).show()
                false
            }
            selectedEndDate!! <= selectedStartDate!! -> {
                Toast.makeText(context, "La fecha de fin debe ser posterior al inicio", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveEvent() {
        // Validar campos
        val isTitleValid = validateTitle()
        val areDatesValid = validateDates()

        if (!isTitleValid || !areDatesValid) {
            if (!isTitleValid) pulseAnimation(binding.tilEventTitle)
            return
        }

        // Obtener datos del formulario
        val titulo = binding.etEventTitle.text.toString().trim()
        val descripcion = binding.etEventDescription.text.toString().trim()
        val ubicacion = binding.etLocation.text.toString().trim()
        val esAllDay = binding.switchAllDay.isChecked

        try {
            val notificationIndex = binding.spinnerNotification.selectedItemPosition
            notificationMinutes = notificationOptions[notificationIndex].second
        } catch (e: Exception) {
            notificationMinutes = 15 // Valor por defecto
        }

        // TODO: Integrar con ViewModel cuando esté Room funcionando

        Toast.makeText(
            requireContext(),
            "📅 Evento '$titulo' creado exitosamente",
            Toast.LENGTH_SHORT
        ).show()

        // Cerrar diálogo
        dismiss()
    }

    private fun startAnimations() {
        try {
            // Animación de entrada del diálogo
            binding.root.alpha = 0f
            binding.root.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

            // Animación escalonada de las secciones
            val secciones = listOf(
                binding.tilEventTitle,
                binding.tilEventDescription,
                binding.layoutDateTimeSection,
                binding.layoutLocationSection,
                binding.layoutRecurrenceSection,
                binding.layoutNotificationSection
            )

            secciones.forEachIndexed { index, seccion ->
                if (seccion.visibility == View.VISIBLE) {
                    seccion.alpha = 0f
                    seccion.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setStartDelay((index * 80).toLong())
                        .start()
                }
            }
        } catch (e: Exception) {
            // Si fallan las animaciones, continuar sin ellas
        }
    }

    private fun pulseAnimation(view: View) {
        try {
            view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } catch (e: Exception) {
            // Si falla la animación, continuar sin ella
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class para opciones de recurrencia
data class RecurrenceOption(
    val type: String,
    val description: String,
    val emoji: String
)

    private fun validateDates(): Boolean {
        return when {
            selectedStartDate == null || selectedEndDate == null -> {
                Snackbar.make(binding.root, "Selecciona fechas válidas", Snackbar.LENGTH_SHORT).show()
                false
            }
            selectedEndDate!! <= selectedStartDate!! -> {
                Snackbar.make(binding.root, "La fecha de fin debe ser posterior al inicio", Snackbar.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveEvent() {
        // Validar campos
        val isTitleValid = validateTitle()
        val areDatesValid = validateDates()

        if (!isTitleValid || !areDatesValid) {
            if (!isTitleValid) pulseAnimation(binding.tilEventTitle)
            return
        }

        // Obtener datos del formulario
        val titulo = binding.etEventTitle.text.toString().trim()
        val descripcion = binding.etEventDescription.text.toString().trim()
        val ubicacion = binding.etLocation.text.toString().trim()
        val esAllDay = binding.switchAllDay.isChecked
        val notificationIndex = binding.spinnerNotification.selectedItemPosition
        notificationMinutes = notificationOptions[notificationIndex].second

        // TODO: Integrar con ViewModel cuando esté Room funcionando

        Toast.makeText(
            requireContext(),
            "📅 Evento '$titulo' creado exitosamente",
            Toast.LENGTH_SHORT
        ).show()

        // Cerrar diálogo
        dismiss()
    }

    private fun startAnimations() {
        // Animación de entrada del diálogo
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Animación escalonada de las secciones
        val secciones = listOf(
            binding.tilEventTitle,
            binding.tilEventDescription,
            binding.layoutDateTimeSection,
            binding.layoutLocationSection,
            binding.layoutColorSection,
            binding.layoutRecurrenceSection,
            binding.layoutNotificationSection
        )

        secciones.forEachIndexed { index, seccion ->
            seccion.alpha = 0f
            seccion.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((index * 80).toLong())
                .start()
        }
    }

    private fun pulseAnimation(view: View) {
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class para opciones de recurrencia
data class RecurrenceOption(
    val type: String,
    val description: String,
    val emoji: String
)