package com.mars.essalureservamedica.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
                tvDoctorName.text = "Dr. ${citaWithDoctorInfo.doctorNombre}"
                tvSpecialty.text = citaWithDoctorInfo.doctorEspecialidad
                tvDateTime.text = dateFormat.format(citaWithDoctorInfo.fechaHora)
                tvStatus.text = citaWithDoctorInfo.estado

                // Configurar visibilidad de botones según el estado
                val estado = EstadoCita.fromString(citaWithDoctorInfo.estado)
                val canModify = estado == EstadoCita.PENDIENTE || estado == EstadoCita.CONFIRMADA
                
                llActionButtons.visibility = if (canModify) View.VISIBLE else View.GONE
                
                // Configurar colores del estado
                when (estado) {
                    EstadoCita.PENDIENTE -> {
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                    }
                    EstadoCita.CONFIRMADA -> {
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                    }
                    EstadoCita.CANCELADA -> {
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                    }
                    EstadoCita.COMPLETADA -> {
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
                    }
                    EstadoCita.REPROGRAMADA -> {
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_purple))
                    }
                }

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

    private class AppointmentDiffCallback : DiffUtil.ItemCallback<CitaWithDoctorInfo>() {
        override fun areItemsTheSame(oldItem: CitaWithDoctorInfo, newItem: CitaWithDoctorInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CitaWithDoctorInfo, newItem: CitaWithDoctorInfo): Boolean {
            return oldItem == newItem
        }
    }
}