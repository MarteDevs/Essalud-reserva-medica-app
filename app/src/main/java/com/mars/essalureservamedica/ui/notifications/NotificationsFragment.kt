package com.mars.essalureservamedica.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mars.essalureservamedica.R
import com.mars.essalureservamedica.data.repository.AppRepository
import com.mars.essalureservamedica.databinding.FragmentNotificationsBinding
import com.mars.essalureservamedica.ui.ViewModelFactory
import com.mars.essalureservamedica.utils.SessionManager

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = AppRepository.getInstance(requireContext())
        val sessionManager = SessionManager(requireContext())
        val factory = ViewModelFactory(repository, sessionManager, requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[NotificationsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = NotificationsAdapter { notificacion ->
            // Marcar como leída al hacer click
            if (!notificacion.leida) {
                viewModel.marcarComoLeida(notificacion.id)
            }
        }

        binding.recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NotificationsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.btnMarkAllRead.setOnClickListener {
            viewModel.marcarTodasComoLeidas()
        }

        binding.btnClearRead.setOnClickListener {
            viewModel.limpiarNotificacionesLeidas()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            // El ViewModel ya observa los cambios automáticamente
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        viewModel.notificaciones.observe(viewLifecycleOwner) { notificaciones ->
            adapter.submitList(notificaciones)

            // Mostrar mensaje si no hay notificaciones
            if (notificaciones.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE // <-- CORREGIDO
                binding.recyclerViewNotifications.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE // <-- CORREGIDO
                binding.recyclerViewNotifications.visibility = View.VISIBLE
            }
        }


        viewModel.countNoLeidas.observe(viewLifecycleOwner) { count ->
            binding.tvUnreadCount.text = "Notificaciones no leídas: $count"
            
            // Habilitar/deshabilitar botones según el estado
            binding.btnMarkAllRead.isEnabled = count > 0
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearOperationResult()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}