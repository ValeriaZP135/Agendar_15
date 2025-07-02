package com.tecsup.agendar_15.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tecsup.agendar_15.databinding.ItemCourseBinding
import com.tecsup.agendar_15.data.database.entities.Curso
import com.tecsup.agendar_15.utils.AnimationUtils

class CourseAdapter(
    private val onCourseClick: (Curso) -> Unit
) : ListAdapter<Curso, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))

        // Animación de entrada
        AnimationUtils.scaleIn(holder.itemView, 300)
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(curso: Curso) {
            binding.apply {
                tvCourseName.text = curso.nombre
                tvCourseDescription.text = curso.descripcion ?: ""
                tvProfessor.text = curso.profesor ?: ""
                tvCredits.text = "${curso.creditos} créditos"

                // Aplicar color del curso
                viewCourseColor.setBackgroundColor(android.graphics.Color.parseColor(curso.color))

                // Configurar click
                root.setOnClickListener {
                    AnimationUtils.pulse(root, 1.05f, 150)
                    onCourseClick(curso)
                }

                // Agregar efecto ripple
                AnimationUtils.addRippleEffect(root)
            }
        }
    }

    private class CourseDiffCallback : DiffUtil.ItemCallback<Curso>() {
        override fun areItemsTheSame(oldItem: Curso, newItem: Curso): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Curso, newItem: Curso): Boolean {
            return oldItem == newItem
        }
    }
}