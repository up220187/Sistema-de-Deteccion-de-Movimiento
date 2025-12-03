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
import com.example.sistemamovimiento.FakeEvent // Asegúrate de tener esto o tu modelo UI
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

        // Configurar Toolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarHistory)
        binding.toolbarHistory.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())

        // Obtener datos reales
        val db = AppDatabase.getDatabase(requireContext())
        val repo = EventRepository(RetrofitClient.instance, db.eventDao())

        lifecycleScope.launch {
            // 1. Obtenemos la lista original de la BD (Entidades completas con IR, PIR, Sound)
            val entities = repo.getLocalEvents() // Invertimos aquí para que coincida con la UI

            // 2. Creamos la lista visual para el Adapter
            val uiEvents = entities.map { entity ->
                val ts = if(entity.timestamp < 1000000000000L) entity.timestamp * 1000 else entity.timestamp
                val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(ts))

                // Calcular sensores activos para el subtítulo (solo visual)
                val activeCount = listOf(entity.ir, entity.pir, entity.sound).count { it == 1 }

                FakeEvent(
                    title = "Prioridad ${entity.severity.uppercase()}", // "Prioridad BAJA" (Igual que en Home)
                    location = "Sensores activos: $activeCount",
                    timestamp = dateStr,
                    imageRes = R.drawable.securewatch_logo
                )
            }

            // 3. Configuramos el Adapter
            val adapter = EventAdapter(uiEvents) { fakeEvent ->

                // A) Encontramos el índice de este elemento en la lista visual
                val index = uiEvents.indexOf(fakeEvent)

                // B) Recuperamos la entidad original usando ese mismo índice
                if (index != -1 && index < entities.size) {
                    val originalEntity = entities[index]

                    // C) Navegamos pasando los datos REALES de la entidad
                    val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(
                        title = fakeEvent.title,
                        timestamp = (if(originalEntity.timestamp < 1000000000000L) originalEntity.timestamp * 1000 else originalEntity.timestamp).toString(),
                        location = fakeEvent.location,
                        imageRes = R.drawable.securewatch_logo,

                        // ¡AQUÍ ESTÁ LA CORRECCIÓN!: Pasamos los valores de la BD
                        irValue = originalEntity.ir,
                        pirValue = originalEntity.pir,
                        soundValue = originalEntity.sound,
                        isHuman = originalEntity.isHuman,
                        blobUrl = originalEntity.blobUrl
                    )
                    findNavController().navigate(action)
                }
            }

            binding.recyclerViewEvents.adapter = adapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
