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
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.entity.Cita
import com.mars.essalureservamedica.databinding.FragmentHomeBinding
import com.mars.essalureservamedica.ui.auth.AuthActivity
import com.mars.essalureservamedica.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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



        homeViewModel.loadStats()
        homeViewModel.loadDoctoresFrecuentes()
        homeViewModel.loadCitasUsuario()

    }

    private fun setupUI() {

        val userName = sessionManager.getUserName()
        binding.tvUserName.text = "Hola, $userName"
    }

    private fun setupObservers() {
        homeViewModel.totalDoctors.observe(viewLifecycleOwner, Observer { count ->
            binding.tvTotalDoctors.text = count.toString()
        })

        homeViewModel.totalAppointments.observe(viewLifecycleOwner, Observer { count ->
            binding.tvTotalAppointments.text = count.toString()
        })

        homeViewModel.doctoresFrecuentes.observe(viewLifecycleOwner) { doctores ->
            val container = binding.containerDoctoresFrecuentes
            container.removeAllViews()

            if (doctores.isEmpty()) {
                val textView = TextView(requireContext()).apply {
                    text = "Aún no tienes médicos frecuentes."
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.futuristic_hint))
                }
                container.addView(textView)
            } else {
                doctores.take(3).forEachIndexed { index, doctor ->
                    val itemView = layoutInflater.inflate(R.layout.item_doctor_frecuente, container, false)

                    val tvRank = itemView.findViewById<TextView>(R.id.tvRank)
                    val ivDoctorPhoto = itemView.findViewById<ShapeableImageView>(R.id.ivDoctorPhoto)
                    val tvDoctorName = itemView.findViewById<TextView>(R.id.tvDoctorName)
                    val tvSpecialty = itemView.findViewById<TextView>(R.id.tvSpecialty)
                    val tvVisitsCount = itemView.findViewById<TextView>(R.id.tvVisitsCount)

                    // Asignar datos
                    tvRank.text = (index + 1).toString()
                    tvDoctorName.text = doctor.nombre
                    tvSpecialty.text = doctor.especialidad
                    tvVisitsCount.text = "${doctor.totalCitas} visita${if (doctor.totalCitas != 1) "s" else ""}"

                    // Cargar imagen (si tienes url o recurso)
                    if (!doctor.foto.isNullOrEmpty()) {
                        Glide.with(this).load(doctor.foto).into(ivDoctorPhoto)
                    } else {
                        ivDoctorPhoto.setImageResource(R.drawable.ic_doctors)
                    }

                    container.addView(itemView)
                }
            }
        }


        homeViewModel.citasUsuario.observe(viewLifecycleOwner) { citas ->
            if (citas.isNotEmpty()) {
                setupCalendar(citas)
            } else {
                binding.tvMes.text = "Sin citas registradas"
            }
        }

    }

    //NUEVO
    // =======================
    // CALENDARIO PERSONALIZADO(el calendar anterior no servia)
    // =======================
    private fun setupCalendar(citas: List<Cita>) {
        val grid = binding.gridCalendar
        val tvMes = binding.tvMes


        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)


        val nombreMes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "PE"))
        tvMes.text = "${nombreMes?.replaceFirstChar { it.uppercase() }} $year"


        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        val primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1
        val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)


        val diasConCita = citas.map {
            val c = Calendar.getInstance().apply { time = it.fechaHora }
            c.get(Calendar.DAY_OF_MONTH)
        }


        grid.removeAllViews()


        for (i in 0 until primerDiaSemana) {
            grid.addView(crearCelda(null, false))
        }

        // 🔸 Crear celdas de los días
        for (dia in 1..diasEnMes) {
            val tieneCita = diasConCita.contains(dia)
            val celda = crearCelda(dia, tieneCita)
            celda.setOnClickListener {
                mostrarCitasDelDia(dia, citas, binding.containerCitasDia, binding.cardDetalleCita)
            }
            grid.addView(celda)
        }
    }

    private fun crearCelda(dia: Int?, tieneCita: Boolean): TextView {
        val tv = TextView(requireContext())
        val params = android.widget.GridLayout.LayoutParams().apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
            setMargins(4, 4, 4, 4)
        }
        tv.layoutParams = params
        tv.gravity = Gravity.CENTER
        tv.textSize = 16f
        tv.setPadding(0, 12, 0, 12)

        if (dia != null) {
            tv.text = dia.toString()
            if (tieneCita) {
                tv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                tv.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
        } else {
            tv.text = ""
            tv.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

        return tv
    }

    private fun mostrarCitasDelDia(
        dia: Int,
        citas: List<Cita>,
        container: LinearLayout,
        card: View
    ) {
        val citasDia = citas.filter {
            val c = Calendar.getInstance().apply { time = it.fechaHora }
            c.get(Calendar.DAY_OF_MONTH) == dia
        }

        container.removeAllViews()

        if (citasDia.isEmpty()) {
            card.visibility = View.GONE
        } else {
            card.visibility = View.VISIBLE
            val formatoHora = SimpleDateFormat("HH:mm", Locale("es", "PE"))

            //Citas del día
            citasDia.forEach { cita ->
                val itemView = layoutInflater.inflate(R.layout.item_cita, container, false)
                val tvHora = itemView.findViewById<TextView>(R.id.tvHoraCita)
                val tvDoctor = itemView.findViewById<TextView>(R.id.tvDoctorCita)
                val tvEstado = itemView.findViewById<TextView>(R.id.tvEstadoCita)

                tvHora.text = formatoHora.format(cita.fechaHora)
                tvEstado.text = cita.estado

                // Doctor
                homeViewModel.viewModelScope.launch() {
                    val doctor = homeViewModel.getDoctorById(cita.doctorId)
                    tvDoctor.text = "${doctor?.nombre ?: "Desconocido"}"
                }

                container.addView(itemView)
            }
        }
    }



    //
    private fun setupClickListeners() {
        binding.btnViewDoctors.setOnClickListener {
            findNavController().navigate(R.id.nav_doctors)
        }

        binding.btnViewAppointments.setOnClickListener {
            findNavController().navigate(R.id.nav_appointments)
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}