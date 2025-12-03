package com.example.sistemamovimiento.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.sistemamovimiento.databinding.FragmentDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Toolbar close action
        binding.toolbarDetail.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Convert timestamp
        val timestamp = args.timestamp.toLongOrNull() ?: 0L
        val date = Date(timestamp * 1000)

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfHour = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        binding.FechaDet.text = sdfDate.format(date)
        binding.HoraDet.text = sdfHour.format(date)

        // Mostrar sensores
        binding.PrecenciaDet.text =
            "IR: ${args.irValue}\nPIR: ${args.pirValue}\nSonido: ${args.soundValue}"

        // Mostrar si es humano
        binding.EsHumanoDet.text = if (args.isHuman == 1) "Sí" else "No"

        // Cargar imagen con Glide
        binding.progressBarImage.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(args.imageUrl)
            .into(binding.imageFullEvent)

        binding.progressBarImage.visibility = View.GONE
    }
}
