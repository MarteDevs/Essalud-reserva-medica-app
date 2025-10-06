package com.mars.essalureservamedica.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.entity.Notificacion
import java.text.SimpleDateFormat
import java.util.*

class NotificationsAdapter(
    private val onNotificationClick: (Notificacion) -> Unit
) : ListAdapter<Notificacion, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMensaje: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvNotificationDate)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvNotificationType)
        private val indicadorNoLeida: View = itemView.findViewById(R.id.viewUnreadIndicator)

        fun bind(notificacion: Notificacion) {
            tvTitulo.text = notificacion.titulo
            tvMensaje.text = notificacion.mensaje
            
            // Formatear fecha
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = dateFormat.format(notificacion.fechaCreacion)
            
            // Mostrar tipo de notificación
            tvTipo.text = when (notificacion.tipo) {
                "CITA_CONFIRMADA" -> "Cita Confirmada"
                "CITA_CANCELADA" -> "Cita Cancelada"
                "CITA_REPROGRAMADA" -> "Cita Reprogramada"
                "RECORDATORIO" -> "Recordatorio"
                "SISTEMA" -> "Sistema"
                else -> notificacion.tipo
            }
            
            // Configurar color del tipo según el tipo de notificación
            val colorRes = when (notificacion.tipo) {
                "CITA_CONFIRMADA" -> R.color.success_color
                "CITA_CANCELADA" -> R.color.error_color
                "CITA_REPROGRAMADA" -> R.color.warning_color
                "RECORDATORIO" -> R.color.info_color
                else -> R.color.primary_color
            }
            tvTipo.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            
            // Mostrar indicador de no leída
            indicadorNoLeida.visibility = if (notificacion.leida) View.GONE else View.VISIBLE
            
            // Cambiar estilo según si está leída o no
            val textColor = if (notificacion.leida) {
                ContextCompat.getColor(itemView.context, R.color.text_secondary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.text_primary)
            }
            tvTitulo.setTextColor(textColor)
            tvMensaje.setTextColor(textColor)
            
            // Configurar click listener
            itemView.setOnClickListener {
                onNotificationClick(notificacion)
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notificacion>() {
        override fun areItemsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
            return oldItem == newItem
        }
    }
}