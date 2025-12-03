package com.example.sistemamovimiento.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemamovimiento.databinding.FragmentHistoryBinding
import com.example.sistemamovimiento.viewmodels.HistoryViewModel
import com.example.sistemamovimiento.viewmodels.HistoryViewModelFactory
import com.example.sistemamovimiento.ui.history.EventAdapter


class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EventAdapter

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 Botón regresar
        binding.toolbarHistory.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Recycler
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())

        adapter = EventAdapter(emptyList()) { event ->
            // Abrir detalle
            val ts = event.timestamp.let {
                if (it < 1_000_000_000_000L) it * 1000 else it
            }.toString()

            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(
                title = "Prioridad ${event.severity}",
                location = "IR ${event.ir}, PIR ${event.pir}, Sonido ${event.sound}",
                timestamp = ts,
                imageRes = com.example.sistemamovimiento.R.drawable.securewatch_logo,
                irValue = event.ir,
                pirValue = event.pir,
                soundValue = event.sound
            )

            findNavController().navigate(action)
        }

        binding.recyclerHistory.adapter = adapter

        // Cargar eventos
        viewModel.events.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)  // Método nuevo
        }

        viewModel.loadLocalEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
