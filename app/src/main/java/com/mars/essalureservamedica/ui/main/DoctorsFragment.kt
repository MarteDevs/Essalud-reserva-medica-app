package com.mars.essalureservamedica.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private fun observeViewModel() {
        viewModel.doctors.observe(viewLifecycleOwner) { doctors ->
            doctorsAdapter.submitList(doctors)
            binding.tvEmptyState.visibility = if (doctors.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}