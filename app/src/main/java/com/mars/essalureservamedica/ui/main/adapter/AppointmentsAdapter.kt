package com.mars.essalureservamedica.ui.main.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.data.entity.EstadoCita
import com.mars.essalureservamedica.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsAdapter(
    private val onAppointmentClick: (CitaWithDoctorInfo) -> Unit,
    private val onCancelClick: (CitaWithDoctorInfo) -> Unit,
    private val onRescheduleClick: (CitaWithDoctorInfo) -> Unit,
    private val onRateClick: (CitaWithDoctorInfo) -> Unit
) : ListAdapter<CitaWithDoctorInfo, AppointmentsAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

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

        fun bind(citaWithDoctorInfo: CitaWithDoctorInfo) {
            binding.apply {

                // ========== INICIO DE LAS CORRECCIONES ==========

                // 1. CORRECCIÓN "Dr. Dr.": Se elimina el prefijo hardcodeado.
                tvDoctorName.text = citaWithDoctorInfo.doctorNombre

                tvSpecialty.text = citaWithDoctorInfo.doctorEspecialidad
                tvDateTime.text = dateFormat.format(citaWithDoctorInfo.fechaHora)

                chipStatus.text = citaWithDoctorInfo.estado

                // 2. CORRECCIÓN TOOLTIP: Se asigna dinámicamente el texto del tooltip.
                chipStatus.tooltipText = "Estado de la cita: ${citaWithDoctorInfo.estado}"

                // ========== FIN DE LAS CORRECCIONES ==========

                val estado = EstadoCita.fromString(citaWithDoctorInfo.estado)

                val statusColor = when (estado) {
                    EstadoCita.PENDIENTE -> ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                    EstadoCita.CONFIRMADA -> ContextCompat.getColor(root.context, R.color.futuristic_secondary)
                    EstadoCita.CANCELADA -> ContextCompat.getColor(root.context, R.color.error)
                    EstadoCita.COMPLETADA -> ContextCompat.getColor(root.context, R.color.futuristic_primary)
                    EstadoCita.REPROGRAMADA -> ContextCompat.getColor(root.context, android.R.color.holo_purple)
                }

                chipStatus.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.futuristic_background))
                chipStatus.setTextColor(statusColor)

                val canModify = estado == EstadoCita.PENDIENTE || estado == EstadoCita.CONFIRMADA
                llActionButtons.visibility = if (canModify) View.VISIBLE else View.GONE

                root.setOnClickListener {
                    onAppointmentClick(citaWithDoctorInfo)
                }

                btnCancel.setOnClickListener {
                    onCancelClick(citaWithDoctorInfo)
                }

                btnReschedule.setOnClickListener {
                    onRescheduleClick(citaWithDoctorInfo)
                }

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

    private class AppointmentDiffCallback : DiffUtil.ItemCallback<CitaWithDoctorInfo>() {
        override fun areItemsTheSame(oldItem: CitaWithDoctorInfo, newItem: CitaWithDoctorInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CitaWithDoctorInfo, newItem: CitaWithDoctorInfo): Boolean {
            return oldItem == newItem
        }
    }
}
