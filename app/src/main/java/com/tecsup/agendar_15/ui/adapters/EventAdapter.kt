package com.tecsup.agendar_15.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tecsup.agendar_15.databinding.ItemEventBinding
import com.tecsup.agendar_15.data.database.entities.Evento
import com.tecsup.agendar_15.utils.AnimationUtils
import com.tecsup.agendar_15.utils.DateUtils

class EventAdapter(
    private val onEventClick: (Evento) -> Unit
) : ListAdapter<Evento, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))

        // Animación de entrada
        AnimationUtils.scaleIn(holder.itemView, 300)
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: Evento) {
            binding.apply {
                tvEventTitle.text = evento.titulo
                tvEventTime.text = "${DateUtils.formatTime(evento.fechaInicio)} - ${DateUtils.formatTime(evento.fechaFin)}"
                tvEventDescription.text = evento.descripcion ?: ""

                // Aplicar color del evento
                viewEventColor.setBackgroundColor(android.graphics.Color.parseColor(evento.color))

                // Configurar click
                root.setOnClickListener {
                    AnimationUtils.pulse(root, 1.05f, 150)
                    onEventClick(evento)
                }

                // Agregar efecto ripple
                AnimationUtils.addRippleEffect(root)
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem == newItem
        }
    }
}