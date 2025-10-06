package com.mars.essalureservamedica.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.data.dao.CitaWithDoctorInfo
import com.mars.essalureservamedica.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsAdapter(
    private val onAppointmentClick: (CitaWithDoctorInfo) -> Unit
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

                root.setOnClickListener {
                    onAppointmentClick(citaWithDoctorInfo)
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