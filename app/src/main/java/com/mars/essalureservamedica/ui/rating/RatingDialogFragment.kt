package com.mars.essalureservamedica.ui.rating

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.firebase.models.CitaWithDoctorFirestore
import com.mars.essalureservamedica.data.firebase.models.CalificacionFirestore
import com.mars.essalureservamedica.databinding.DialogRatingBinding
import com.mars.essalureservamedica.ui.main.AppointmentsViewModel
import com.mars.essalureservamedica.ui.ViewModelFactory
import com.mars.essalureservamedica.utils.SessionManager
import java.util.*

class RatingDialogFragment : DialogFragment() {
    
    private var _binding: DialogRatingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var appointmentsViewModel: AppointmentsViewModel
    private lateinit var citaInfo: CitaWithDoctorFirestore
    private var userId: Int = 0
    
    companion object {
        private const val ARG_CITA_INFO = "cita_info"
        private const val ARG_USER_ID = "user_id"
        
        fun newInstance(citaInfo: CitaWithDoctorFirestore, userId: Int): RatingDialogFragment {
            val fragment = RatingDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_CITA_INFO, citaInfo)
            args.putInt(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            citaInfo = it.getSerializable(ARG_CITA_INFO) as CitaWithDoctorFirestore
            userId = it.getInt(ARG_USER_ID)
        }
        appointmentsViewModel = ViewModelProvider(requireActivity())[AppointmentsViewModel::class.java]
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRatingBinding.inflate(layoutInflater)
        
        setupViews()
        setupObservers()
        
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Calificar Consulta")
            .setPositiveButton("Enviar Calificación") { _, _ ->
                submitRating()
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
    
    private fun setupViews() {
        binding.apply {
            tvDoctorName.text = citaInfo.doctorNombre
            tvSpecialty.text = citaInfo.doctorEspecialidad
            tvAppointmentDate.text = "Fecha: ${citaInfo.fechaHora}"
            
            // Configurar RatingBar
            ratingBar.rating = 5.0f // Valor por defecto
            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                updateRatingText(rating)
            }
            updateRatingText(5.0f)
        }
    }
    
    private fun updateRatingText(rating: Float) {
        val ratingText = when {
            rating >= 4.5f -> "Excelente"
            rating >= 3.5f -> "Muy Bueno"
            rating >= 2.5f -> "Bueno"
            rating >= 1.5f -> "Regular"
            else -> "Malo"
        }
        binding.tvRatingText.text = "$ratingText (${String.format("%.1f", rating)} estrellas)"
    }
    
    private fun setupObservers() {
        appointmentsViewModel.operationResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, "Calificación enviada exitosamente", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, 
                        "Error al enviar calificación: ${it.exceptionOrNull()?.message ?: "Error desconocido"}", 
                        Toast.LENGTH_LONG).show()
                }
                appointmentsViewModel.clearOperationResult()
            }
        }
    }
    
    private fun submitRating() {
        val rating = binding.ratingBar.rating
        val comment = binding.etComment.text.toString().trim()
        
        if (rating == 0f) {
            Toast.makeText(context, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            return
        }
        
        val calificacion = CalificacionFirestore(
            id = "", // Se auto-genera en Firestore
            usuarioId = userId.toString(),
            doctorId = citaInfo.doctorId,
            citaId = citaInfo.id,
            puntuacion = rating.toInt(),
            comentario = if (comment.isNotEmpty()) comment else "",
            fecha = Date().time
        )
        
        appointmentsViewModel.submitRating(calificacion)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}