package com.mars.essalureservamedica.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mars.essalureservamedica.databinding.ItemScheduleBinding
import com.mars.essalureservamedica.ui.doctor.ScheduleSlot

class ScheduleAdapter(
    private val onScheduleClick: (ScheduleSlot) -> Unit
) : ListAdapter<ScheduleSlot, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleSlot) {
            binding.apply {
                tvDate.text = schedule.date
                tvTime.text = schedule.time
                tvStatus.text = if (schedule.isAvailable) "Disponible" else "Ocupado"

                root.setOnClickListener {
                    if (schedule.isAvailable) {
                        onScheduleClick(schedule)
                    }
                }

                root.alpha = if (schedule.isAvailable) 1.0f else 0.5f
                root.isClickable = schedule.isAvailable
            }
        }
    }

    private class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleSlot>() {
        override fun areItemsTheSame(oldItem: ScheduleSlot, newItem: ScheduleSlot): Boolean {
            return oldItem.date == newItem.date && oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: ScheduleSlot, newItem: ScheduleSlot): Boolean {
            return oldItem == newItem
        }
    }
}