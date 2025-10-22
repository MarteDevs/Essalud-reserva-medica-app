package com.mars.essalureservamedica.ui.notifications

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.firebase.models.NotificacionFirestore
import com.mars.essalureservamedica.databinding.ItemNotificationBinding // Importante: usar ViewBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsAdapter(
    private val onNotificationClick: (NotificacionFirestore) -> Unit
) : ListAdapter<NotificacionFirestore, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        // Usar ViewBinding para más seguridad y limpieza
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        // Usar el binding en lugar de 'itemView'
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacion: NotificacionFirestore) {
            binding.apply { // 'apply' nos permite acceder a las vistas del binding directamente
                tvNotificationTitle.text = notificacion.titulo
                tvNotificationMessage.text = notificacion.mensaje

                // Formatear la fecha para que sea relativa (ej: "hace 5m", "hace 2h")
                tvNotificationDate.text = getRelativeTime(Date(notificacion.fechaCreacion))

                // --- SE ELIMINÓ TODA LA LÓGICA DE 'tvNotificationType' ---

                // Mostrar/Ocultar el indicador de no leída
                viewUnreadIndicator.visibility = if (notificacion.leida) View.GONE else View.VISIBLE

                // Cambiar estilo del texto si está leída o no
                val titleStyle = if (notificacion.leida) Typeface.NORMAL else Typeface.BOLD
                val messageColor = if (notificacion.leida) {
                    ContextCompat.getColor(root.context, R.color.futuristic_hint)
                } else {
                    ContextCompat.getColor(root.context, R.color.futuristic_text)
                }

                tvNotificationTitle.setTypeface(null, titleStyle)
                tvNotificationMessage.setTextColor(messageColor)

                // Configurar click listener
                root.setOnClickListener {
                    onNotificationClick(notificacion)
                }
            }
        }

        private fun getRelativeTime(date: Date): String {
            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            if (seconds < 60) return "ahora"

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            if (minutes < 60) return "hace ${minutes}m"

            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            if (hours < 24) return "hace ${hours}h"

            val days = TimeUnit.MILLISECONDS.toDays(diff)
            return "hace ${days}d"
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificacionFirestore>() {
        override fun areItemsTheSame(oldItem: NotificacionFirestore, newItem: NotificacionFirestore): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificacionFirestore, newItem: NotificacionFirestore): Boolean {
            return oldItem == newItem
        }
    }
}
