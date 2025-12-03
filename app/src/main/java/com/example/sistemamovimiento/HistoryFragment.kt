package com.example.sistemamovimiento.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemamovimiento.EventAdapter
// Ya no necesitamos importar FakeEvent
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.databinding.FragmentHistoryBinding
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Importante llamar a super

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarHistory)
        binding.toolbarHistory.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())

        val db = AppDatabase.getDatabase(requireContext())
        val repo = EventRepository(RetrofitClient.instance, db.eventDao())

        lifecycleScope.launch {
            // 1. Obtenemos la lista REAL de la base de datos
            val entities = repo.getLocalEvents()

            // 2. Se la pasamos DIRECTAMENTE al adapter. ¡Mucho más fácil!
            val adapter = EventAdapter(entities) { selectedEvent ->

                // Al hacer click, ya tenemos el objeto 'selectedEvent' (EventEntity)
                // Calculamos timestamp correcto
                val tsMillis = if(selectedEvent.timestamp < 1000000000000L) selectedEvent.timestamp * 1000 else selectedEvent.timestamp

                // Recalculamos textos para enviar al detalle
                val activeCount = listOf(selectedEvent.ir, selectedEvent.pir, selectedEvent.sound).count { it == 1 }

                val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(
                    title = "Prioridad ${selectedEvent.severity.uppercase()}",
                    timestamp = tsMillis.toString(),
                    location = "Sensores activos: $activeCount",
                    imageRes = R.drawable.securewatch_logo, // Argumento legacy, ya no importa tanto porque usamos URL

                    // Datos reales
                    irValue = selectedEvent.ir,
                    pirValue = selectedEvent.pir,
                    soundValue = selectedEvent.sound,
                    isHuman = selectedEvent.isHuman,
                    blobUrl = selectedEvent.blobUrl
                )
                findNavController().navigate(action)
            }

            binding.recyclerViewEvents.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
