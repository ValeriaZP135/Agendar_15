package com.tecsup.agendar_15.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tecsup.agendar_15.R

class ColorPickerAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_picker, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount() = colors.size

    fun selectColor(color: String) {
        val index = colors.indexOf(color)
        if (index != -1) {
            val oldPosition = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorCard: MaterialCardView = itemView.findViewById(R.id.colorCard)
        private val selectionIndicator: View = itemView.findViewById(R.id.selectionIndicator)

        fun bind(color: String, isSelected: Boolean) {
            colorCard.setCardBackgroundColor(Color.parseColor(color))

            // Mostrar/ocultar indicador de selección
            selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Configurar click
            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition

                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)

                onColorSelected(color)
            }
        }
    }
}