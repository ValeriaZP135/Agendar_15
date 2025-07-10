package com.tecsup.agendar_15.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tecsup.agendar_15.R
import com.tecsup.agendar_15.ui.dialogs.RecurrenceOption
import com.tecsup.agendar_15.utils.AnimationUtils

class RecurrenceAdapter(
    private val options: List<RecurrenceOption>,
    private val onRecurrenceSelected: (RecurrenceOption) -> Unit
) : RecyclerView.Adapter<RecurrenceAdapter.RecurrenceViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecurrenceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recurrence_option, parent, false)
        return RecurrenceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecurrenceViewHolder, position: Int) {
        holder.bind(options[position], position == selectedPosition)
    }

    override fun getItemCount() = options.size

    fun selectRecurrence(type: String) {
        val index = options.indexOfFirst { it.type == type }
        if (index != -1) {
            val oldPosition = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    inner class RecurrenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.recurrenceCard)
        private val emoji: TextView = itemView.findViewById(R.id.tvEmoji)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)
        private val selectionIndicator: View = itemView.findViewById(R.id.selectionIndicator)

        fun bind(option: RecurrenceOption, isSelected: Boolean) {
            emoji.text = option.emoji
            description.text = option.description

            // Cambiar apariencia según selección
            if (isSelected) {
                card.strokeWidth = 4
                card.strokeColor = itemView.context.getColor(R.color.primary)
                selectionIndicator.visibility = View.VISIBLE
                AnimationUtils.scaleIn(selectionIndicator, 200)
            } else {
                card.strokeWidth = 1
                card.strokeColor = itemView.context.getColor(R.color.divider)
                selectionIndicator.visibility = View.GONE
            }

            // Configurar click
            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition

                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)

                AnimationUtils.pulse(card, 1.1f, 200)
                onRecurrenceSelected(option)
            }
        }
    }
}