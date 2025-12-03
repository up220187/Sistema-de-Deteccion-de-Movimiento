package com.example.sistemamovimiento.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.databinding.FragmentDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar botón de atrás en la toolbar
        binding.toolbarDetail.setNavigationOnClickListener { findNavController().navigateUp() }

        // 1. Título de Prioridad
        binding.textDet.text = args.title

        // 2. Formatear Fecha y Hora
        val tsLong = args.timestamp.toLongOrNull() ?: 0L
        val date = Date(tsLong)
        // Formato ejemplo: "03 Dic 2025 • 14:30:05"
        val fmt = SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale("es", "ES"))
        binding.textFechaHora.text = fmt.format(date)

        // 3. Cargar IMAGEN usando Glide
        val url = args.blobUrl
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.securewatch_logo) // Imagen mientras carga
                .error(R.drawable.securewatch_logo)       // Imagen si falla la carga
                .into(binding.imageFullEvent)
        } else {
            // Si no hay URL, mostramos el logo por defecto
            binding.imageFullEvent.setImageResource(R.drawable.securewatch_logo)
        }

        // 4. Indicador de HUMANO DETECTADO
        // Si args.isHuman es 1, lo mostramos. Si es 0, se oculta (GONE)
        binding.chipHumanDetected.visibility = if (args.isHuman == 1) View.VISIBLE else View.GONE


        // 5. Lógica de los CHIPS DE SENSORES
        // Mostramos u ocultamos cada chip individualmente
        binding.chipIr.visibility = if (args.irValue == 1) View.VISIBLE else View.GONE
        binding.chipPir.visibility = if (args.pirValue == 1) View.VISIBLE else View.GONE
        binding.chipSound.visibility = if (args.soundValue == 1) View.VISIBLE else View.GONE

        // 6. Manejo del mensaje "Sin datos"
        // Si NINGUNO está visible, mostramos el texto de "Sin datos específicos"
        val anySensorActive = (args.irValue == 1 || args.pirValue == 1 || args.soundValue == 1)
        binding.txtNoSensors.visibility = if (anySensorActive) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
