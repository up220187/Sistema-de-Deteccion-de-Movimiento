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

        binding.toolbarDetail.setNavigationOnClickListener { findNavController().navigateUp() }

        // Título
        binding.textDet.text = args.title

        // Imagen (luego será URL real)
        Glide.with(this)
            .load(args.imageRes)
            .placeholder(R.drawable.securewatch_logo)
            .into(binding.imageFullEvent)

        binding.progressBarImage.visibility = View.GONE

        // Timestamp
        val tsLong = args.timestamp.toLongOrNull() ?: 0L
        val date = Date(tsLong)

        binding.FechaDet.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        binding.HoraDet.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)

        // Sensores
        val sensors = mutableListOf<String>()
        if (args.irValue == 1) sensors.add("• Sensor Infrarrojo (IR)")
        if (args.pirValue == 1) sensors.add("• Sensor de Movimiento (PIR)")
        if (args.soundValue == 1) sensors.add("• Sensor de Sonido")

        binding.PrecenciaDet.text =
            if (sensors.isNotEmpty()) sensors.joinToString("\n")
            else "Sin información específica de sensores."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
