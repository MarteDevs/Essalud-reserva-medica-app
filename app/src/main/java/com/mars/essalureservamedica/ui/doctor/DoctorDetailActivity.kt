package com.mars.essalureservamedica.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mars.essalureservamedica.databinding.ActivityDoctorDetailBinding
import com.mars.essalureservamedica.ui.doctor.adapter.ScheduleAdapter
import com.mars.essalureservamedica.ui.schedule.ScheduleActivity

class DoctorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorDetailBinding
    private val viewModel: DoctorDetailViewModel by viewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        val doctorId = intent.getStringExtra("doctor_id")
        if (doctorId != null && doctorId.isNotEmpty()) {
            viewModel.loadDoctorDetails(doctorId)
            viewModel.loadAvailableSchedules(doctorId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Detalles del Doctor"
        }
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter { schedule ->
            // Navegar a la pantalla de agendamiento
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("doctor_id", viewModel.doctor.value?.id)
            intent.putExtra("schedule_date", schedule.date)
            intent.putExtra("schedule_time", schedule.time)
            startActivity(intent)
        }

        binding.rvSchedules.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(this@DoctorDetailActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.doctor.observe(this) { doctor ->
            doctor?.let {
                binding.apply {
                    tvDoctorName.text = it.nombre
                    tvSpecialty.text = it.especialidad
                    tvExperience.text = "${it.experiencia} años de experiencia"
                }
            }
        }

        viewModel.availableSchedules.observe(this) { schedules ->
            scheduleAdapter.submitList(schedules)
        }
    }

    private fun setupClickListeners() {
        binding.btnScheduleAppointment.setOnClickListener {
            val doctorId = intent.getStringExtra("doctor_id")
            if (doctorId != null && doctorId.isNotEmpty()) {
                val intent = Intent(this, ScheduleActivity::class.java)
                intent.putExtra("doctor_id", doctorId)
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}