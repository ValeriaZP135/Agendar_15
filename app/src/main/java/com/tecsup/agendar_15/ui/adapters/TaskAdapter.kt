package com.tecsup.agendar_15.ui.adapters

import android.animation.ValueAnimator
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.databinding.ItemTaskBinding
import com.tecsup.agendar_15.data.database.entities.Tarea
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.DateUtils

class TaskAdapter(
    private val onTaskClick: (Tarea) -> Unit,
    private val onTaskCompleted: (Tarea) -> Unit,
    private val onTaskLongClick: (Tarea) -> Unit
) : ListAdapter<Tarea, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))

        // Animación de entrada escalonada
        AnimationUtils.slideInFromBottom(holder.itemView, 300)
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .start()
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tarea: Tarea) {
            binding.apply {
                tvTaskTitle.text = tarea.titulo
                tvTaskDescription.text = tarea.descripcion ?: ""

                // Configurar fecha de vencimiento
                if (tarea.fechaVencimiento != null) {
                    tvTaskDueDate.text = DateUtils.formatDate(tarea.fechaVencimiento)
                    tvTaskDueDate.visibility = ViewGroup.VISIBLE

                    // Verificar si está vencida
                    if (DateUtils.isOverdue(tarea.fechaVencimiento) && !tarea.completada) {
                        tvTaskDueDate.setTextColor(ContextCompat.getColor(root.context, R.color.priority_high))
                        // Animación de pulso para tareas vencidas
                        AnimationUtils.pulse(tvTaskDueDate, 1.1f, 1000)
                    } else {
                        tvTaskDueDate.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                    }
                } else {
                    tvTaskDueDate.visibility = ViewGroup.GONE
                }

                // Configurar prioridad
                val priorityColor = when (tarea.prioridad) {
                    3 -> R.color.priority_high
                    2 -> R.color.priority_medium
                    else -> R.color.priority_low
                }
                viewPriority.setBackgroundColor(
                    ContextCompat.getColor(root.context, priorityColor)
                )

                // Configurar checkbox
                checkboxCompleted.isChecked = tarea.completada

                // Aplicar estilo de tarea completada
                updateCompletedStyle(tarea.completada, false)

                // Listeners
                checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != tarea.completada) {
                        updateCompletedStyle(isChecked, true)
                        onTaskCompleted(tarea.copy(completada = isChecked))

                        if (isChecked) {
                            // Animación de checkmark
                            AnimationUtils.checkmarkAnimation(checkboxCompleted)
                        }
                    }
                }

                root.setOnClickListener {
                    AnimationUtils.pulse(root, 1.02f, 150)
                    onTaskClick(tarea)
                }

                root.setOnLongClickListener {
                    AnimationUtils.pulse(root, 1.05f, 200)
                    onTaskLongClick(tarea)
                    true
                }

                // Agregar efecto ripple
                AnimationUtils.addRippleEffect(root)
            }
        }

        private fun updateCompletedStyle(isCompleted: Boolean, animate: Boolean) {
            binding.apply {
                if (animate) {
                    // Animación de tachado
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 300
                    animator.addUpdateListener { animation ->
                        val progress = animation.animatedValue as Float

                        if (isCompleted) {
                            tvTaskTitle.alpha = 1f - (progress * 0.5f)
                            tvTaskDescription.alpha = 1f - (progress * 0.5f)

                            if (progress > 0.5f) {
                                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            }
                        } else {
                            tvTaskTitle.alpha = 0.5f + (progress * 0.5f)
                            tvTaskDescription.alpha = 0.5f + (progress * 0.5f)
                            tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }
                    animator.start()
                } else {
                    // Aplicar estilo inmediatamente
                    if (isCompleted) {
                        tvTaskTitle.alpha = 0.5f
                        tvTaskDescription.alpha = 0.5f
                        tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        tvTaskTitle.alpha = 1f
                        tvTaskDescription.alpha = 1f
                        tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Tarea>() {
        override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem == newItem
        }
    }
}