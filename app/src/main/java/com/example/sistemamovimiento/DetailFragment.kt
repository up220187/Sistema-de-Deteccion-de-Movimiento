package com.example.sistemamovimiento.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sistemamovimiento.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cerrar con icono
        binding.toolbarDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Recibir datos del Safe Args
        val title = args.title
        val location = args.location
        val timestamp = args.timestamp
        val imageRes = args.imageRes

        // Simular loading
        binding.progressBarImage.visibility = View.VISIBLE

        binding.imageFullEvent.postDelayed({
            binding.imageFullEvent.setImageResource(imageRes)
            binding.progressBarImage.visibility = View.GONE
        }, 800)

        // Llenar textos (si los necesitas dinámicos)
        binding.textDet.text = title
        binding.PrecenciaDet.text = location
        binding.FechaDet.text = timestamp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
