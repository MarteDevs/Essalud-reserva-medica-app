package com.mars.essalureservamedica.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.data.entity.Doctor
import com.mars.essalureservamedica.databinding.ItemDoctorBinding

class DoctorsAdapter(
    private val onDoctorClick: (Doctor) -> Unit
) : ListAdapter<Doctor, DoctorsAdapter.DoctorViewHolder>(DoctorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DoctorViewHolder(
        private val binding: ItemDoctorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.apply {
                tvDoctorName.text = "Dr. ${doctor.nombre}"
                tvSpecialty.text = doctor.especialidad
                tvExperience.text = "${doctor.experiencia} años de experiencia"

                root.setOnClickListener {
                    onDoctorClick(doctor)
                }
            }
        }
    }

    private class DoctorDiffCallback : DiffUtil.ItemCallback<Doctor>() {
        override fun areItemsTheSame(oldItem: Doctor, newItem: Doctor): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Doctor, newItem: Doctor): Boolean {
            return oldItem == newItem
        }
    }
}