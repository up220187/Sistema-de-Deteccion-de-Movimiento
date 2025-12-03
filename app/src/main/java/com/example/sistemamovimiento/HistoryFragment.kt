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

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EventAdapter

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarHistory.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())

        adapter = EventAdapter(mutableListOf(), onClick = { event ->
            val ts = if (event.timestamp < 1_000_000_000_000L) event.timestamp * 1000 else event.timestamp
            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(
                title = "Prioridad ${event.severity}",
                location = "IR ${event.ir}, PIR ${event.pir}, Sonido ${event.sound}",
                timestamp = ts.toString(),
                imageRes = com.example.sistemamovimiento.R.drawable.securewatch_logo,
                irValue = event.ir,
                pirValue = event.pir,
                soundValue = event.sound
            )
            // Guardar blobUrl en activity intent para que Detail pueda leerla (alternativa sería añadir arg nav)
            activity?.intent?.putExtra("detail_blobUrl", event.blobUrl)
            findNavController().navigate(action)
        })

        binding.recyclerHistory.adapter = adapter

        viewModel.events.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.loadLocalEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
