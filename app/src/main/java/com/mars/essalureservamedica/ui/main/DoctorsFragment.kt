package com.mars.essalureservamedica.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mars.essalureservamedica.databinding.FragmentDoctorsBinding
import com.mars.essalureservamedica.ui.doctor.DoctorDetailActivity
import com.mars.essalureservamedica.ui.main.adapter.DoctorsAdapter

class DoctorsFragment : Fragment() {

    private var _binding: FragmentDoctorsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorsViewModel by viewModels()
    private lateinit var doctorsAdapter: DoctorsAdapter
    private lateinit var especialidadAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupSpinner()
        observeViewModel()
        viewModel.loadDoctors()
    }

    private fun setupRecyclerView() {
        doctorsAdapter = DoctorsAdapter { doctor ->
            val intent = Intent(requireContext(), DoctorDetailActivity::class.java)
            intent.putExtra("doctor_id", doctor.id)
            startActivity(intent)
        }

        binding.rvDoctors.apply {
            adapter = doctorsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchDoctors(newText ?: "")
                return true
            }
        })
    }

    private fun setupSpinner() {
        especialidadAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        especialidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEspecialidad.adapter = especialidadAdapter

        binding.spinnerEspecialidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedEspecialidad = especialidadAdapter.getItem(position) ?: "Todas"
                viewModel.filterByEspecialidad(selectedEspecialidad)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun observeViewModel() {
        // Observar la lista filtrada en lugar de la lista completa
        viewModel.filteredDoctors.observe(viewLifecycleOwner) { doctors ->
            doctorsAdapter.submitList(doctors)
            binding.tvEmptyState.visibility = if (doctors.isEmpty()) View.VISIBLE else View.GONE
            
            // Actualizar mensaje de estado vacío
            binding.tvEmptyState.text = if (doctors.isEmpty()) {
                "No se encontraron doctores con los filtros aplicados"
            } else {
                "No hay doctores disponibles"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar las especialidades para llenar el spinner
        viewModel.especialidades.observe(viewLifecycleOwner) { especialidades ->
            especialidadAdapter.clear()
            especialidadAdapter.addAll(especialidades)
            especialidadAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}