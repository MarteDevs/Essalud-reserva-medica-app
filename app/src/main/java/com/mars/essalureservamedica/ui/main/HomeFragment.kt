package com.mars.essalureservamedica.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.databinding.FragmentHomeBinding
import com.mars.essalureservamedica.ui.auth.AuthActivity
import com.mars.essalureservamedica.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private val calendario = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        setupUI()
        setupObservers()
        setupClickListeners()

        binding.btnMesAnterior.setOnClickListener {
            calendario.add(Calendar.MONTH, -1)
            homeViewModel.appointments.value?.let { setupCalendar(it) }
        }

        binding.btnMesSiguiente.setOnClickListener {
            calendario.add(Calendar.MONTH, 1)
            homeViewModel.appointments.value?.let { setupCalendar(it) }
        }
        // Cargar datos desde el ViewModel
        homeViewModel.refreshStats()
    }
    private fun setupUI() {
        // Inicialmente puedes mostrar un saludo genérico
        binding.tvUserName.text = "Hola, Usuario"
    }
    private fun setupObservers() {
        // Nombre de usuario
        homeViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = "Hola, $name"
        }
        // Total de citas
        homeViewModel.totalAppointments.observe(viewLifecycleOwner) { total ->
            binding.tvTotalAppointments.text = total.toString()
        }

        // Total de doctores
        homeViewModel.totalDoctors.observe(viewLifecycleOwner) { total ->
            binding.tvTotalDoctors.text = total.toString()
        }
        homeViewModel.appointments.observe(viewLifecycleOwner) { citas ->
            if (citas.isNotEmpty()) {
                // Encontrar la cita más próxima
                val ahora = Date()
                val proximaCita = citas.filter { it.fechaHora.after(ahora) }
                    .minByOrNull { it.fechaHora.time }
                    ?: citas.minByOrNull { it.fechaHora.time } // si no hay futuras, tomar la más antigua

                // Ajustar el calendario al mes de la cita seleccionada
                proximaCita?.let {
                    calendario.time = it.fechaHora
                }

                // Mostrar mes y año
                val nombreMes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "PE"))
                val year = calendario.get(Calendar.YEAR)
                binding.tvMes.text = "${nombreMes?.replaceFirstChar { it.uppercase() }} $year"

                // Construir el calendario normalmente
                setupCalendar(citas)
            } else {
                binding.tvMes.text = "Sin citas registradas"
                binding.gridCalendar.removeAllViews()
            }
        }


        homeViewModel.frequentDoctors.observe(viewLifecycleOwner) { doctors ->
            val container = binding.containerDoctoresFrecuentes
            container.removeAllViews()

            if (doctors.isEmpty()) {
                val tv = TextView(requireContext()).apply {
                    text = "No hay médicos frecuentes"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.futuristic_hint))
                    textSize = 14f
                    setPadding(8, 8, 8, 8)
                }
                container.addView(tv)
                return@observe
            }

            doctors.forEachIndexed { index, doctorPair ->
                val doctor = doctorPair.first
                val totalCitas = doctorPair.second

                val itemView = layoutInflater.inflate(R.layout.item_doctor_frecuente, container, false)

                val tvRank = itemView.findViewById<TextView>(R.id.tvRank)
                val ivPhoto = itemView.findViewById<ShapeableImageView>(R.id.ivDoctorPhoto)
                val tvName = itemView.findViewById<TextView>(R.id.tvDoctorName)
                val tvSpecialty = itemView.findViewById<TextView>(R.id.tvSpecialty)
                val tvVisits = itemView.findViewById<TextView>(R.id.tvVisitsCount)

                tvRank.text = (index + 1).toString()
                tvName.text = doctor.nombre
                tvSpecialty.text = doctor.especialidad
                tvVisits.text = "$totalCitas visitas"

                if (!doctor.foto.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(doctor.foto)
                        .placeholder(R.drawable.ic_doctors)
                        .into(ivPhoto)
                } else {
                    ivPhoto.setImageResource(R.drawable.ic_doctors)
                }

                container.addView(itemView)
            }
        }
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        homeViewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            binding.tvSyncStatus.text = status
        }
    }

    private fun setupCalendar(citas: List<CitaWithDoctorFirestore>) {
        val grid = binding.gridCalendar
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)

        val nombreMes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "PE"))
        binding.tvMes.text = "${nombreMes?.replaceFirstChar { it.uppercase() }} $year"

        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        val primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1
        val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Filtrar citas solo de este mes/año
        val diasConCita = citas
            .filter {
                val c = Calendar.getInstance().apply { time = it.fechaHora }
                c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
            }
            .map { Calendar.getInstance().apply { time = it.fechaHora }.get(Calendar.DAY_OF_MONTH) }

        grid.removeAllViews()

        for (i in 0 until primerDiaSemana) grid.addView(crearCelda(null, false))

        for (dia in 1..diasEnMes) {
            val tieneCita = diasConCita.contains(dia)
            val celda = crearCelda(dia, tieneCita)
            celda.setOnClickListener {
                mostrarCitasDelDia(dia, citas.filter {
                    val c = Calendar.getInstance().apply { time = it.fechaHora }
                    c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
                }, binding.containerCitasDia, binding.cardDetalleCita)
            }
            grid.addView(celda)
        }
    }
    private fun crearCelda(dia: Int?, tieneCita: Boolean): TextView {
        val tv = TextView(requireContext()).apply {
            layoutParams = android.widget.GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            gravity = Gravity.CENTER
            textSize = 16f
            setPadding(0, 12, 0, 12)
            text = dia?.toString() ?: ""
            setBackgroundColor(
                if (tieneCita) ContextCompat.getColor(requireContext(), R.color.primary_green)
                else ContextCompat.getColor(requireContext(), android.R.color.transparent)
            )
            setTextColor(
                if (tieneCita) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.text_primary)
            )
        }
        return tv
    }

    private fun mostrarCitasDelDia(
        dia: Int,
        citas: List<CitaWithDoctorFirestore>,
        container: LinearLayout,
        card: View
    ) {
        val citasDia = citas.filter {
            Calendar.getInstance().apply { time = it.fechaHora }.get(Calendar.DAY_OF_MONTH) == dia
        }

        container.removeAllViews()
        card.visibility = if (citasDia.isEmpty()) View.GONE else View.VISIBLE

        val formatoHora = SimpleDateFormat("HH:mm", Locale("es", "PE"))
        citasDia.forEach { cita ->
            val itemView = layoutInflater.inflate(R.layout.item_cita, container, false)
            itemView.findViewById<TextView>(R.id.tvHoraCita).text = formatoHora.format(cita.fechaHora)
            itemView.findViewById<TextView>(R.id.tvDoctorCita).text = cita.doctorNombre
            itemView.findViewById<TextView>(R.id.tvEstadoCita).text = cita.estado
            container.addView(itemView)
        }
    }

    private fun setupClickListeners() {
        binding.btnViewDoctors.setOnClickListener { findNavController().navigate(R.id.nav_doctors) }
        binding.btnViewAppointments.setOnClickListener { findNavController().navigate(R.id.nav_appointments) }
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(
                Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            requireActivity().finish()
        }
    }

    private fun formatDate(timestamp: Long): String = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    } catch (e: Exception) {
        "Fecha no válida"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
