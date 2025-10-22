package com.mars.essalureservamedica.ui.main.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.R // <-- Asegúrate de que esta importación exista
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.data.entity.EstadoCita
import com.mars.essalureservamedica.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsAdapter(
    private val onAppointmentClick: (CitaWithDoctorFirestore) -> Unit,
    private val onCancelClick: (CitaWithDoctorFirestore) -> Unit,
    private val onRescheduleClick: (CitaWithDoctorFirestore) -> Unit,
    private val onRateClick: (CitaWithDoctorFirestore) -> Unit
) : ListAdapter<CitaWithDoctorFirestore, AppointmentsAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("es", "ES"))

        fun bind(citaWithDoctorInfo: CitaWithDoctorFirestore) {
            binding.apply {
                tvDoctorName.text = "Dr. ${citaWithDoctorInfo.doctorNombre}"
                tvSpecialty.text = citaWithDoctorInfo.doctorEspecialidad
                tvDateTime.text = dateFormat.format(citaWithDoctorInfo.fechaHora)

                // --- INICIO DE CAMBIOS ---

                // 1. Asignar el texto al nuevo Chip
                chipStatus.text = citaWithDoctorInfo.estado

                val estado = EstadoCita.fromString(citaWithDoctorInfo.estado)

                // 2. Configurar colores del Chip (en lugar del TextView)
                val statusColor = when (estado) {
                    EstadoCita.PENDIENTE -> ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                    EstadoCita.CONFIRMADA -> ContextCompat.getColor(root.context, R.color.futuristic_secondary) // Usando nuestro color
                    EstadoCita.CANCELADA -> ContextCompat.getColor(root.context, R.color.error) // Usando nuestro color
                    EstadoCita.COMPLETADA -> ContextCompat.getColor(root.context, R.color.futuristic_primary) // Usando nuestro color
                    EstadoCita.REPROGRAMADA -> ContextCompat.getColor(root.context, android.R.color.holo_purple)
                }

                // Aplicar los colores al Chip
                chipStatus.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.futuristic_background))
                chipStatus.setTextColor(statusColor)

                // --- FIN DE CAMBIOS ---

                // Configurar visibilidad de botones según el estado
                val canModify = estado == EstadoCita.PENDIENTE || estado == EstadoCita.CONFIRMADA
                llActionButtons.visibility = if (canModify) View.VISIBLE else View.GONE

                // Configurar listeners
                root.setOnClickListener {
                    onAppointmentClick(citaWithDoctorInfo)
                }

                btnCancel.setOnClickListener {
                    onCancelClick(citaWithDoctorInfo)
                }

                btnReschedule.setOnClickListener {
                    onRescheduleClick(citaWithDoctorInfo)
                }

                // Mostrar botón de calificar solo para citas completadas
                if (estado == EstadoCita.COMPLETADA) {
                    btnRate.visibility = View.VISIBLE
                    btnRate.setOnClickListener {
                        onRateClick(citaWithDoctorInfo)
                    }
                } else {
                    btnRate.visibility = View.GONE
                }
            }
        }
    }

    private class AppointmentDiffCallback : DiffUtil.ItemCallback<CitaWithDoctorFirestore>() {
        override fun areItemsTheSame(oldItem: CitaWithDoctorFirestore, newItem: CitaWithDoctorFirestore): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CitaWithDoctorFirestore, newItem: CitaWithDoctorFirestore): Boolean {
            return oldItem == newItem
        }
    }
}
