package com.example.sistemamovimiento.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemamovimiento.EventAdapter
import com.example.sistemamovimiento.FakeData
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.databinding.FragmentHistoryBinding
import com.example.sistemamovimiento.ui.history.HistoryFragmentDirections

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Toolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarHistory)
        binding.toolbarHistory.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // RecyclerView
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())

        val adapter = EventAdapter(FakeData.events) { event ->

            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(
                title = event.title,
                timestamp = event.timestamp,
                location = event.location,
                imageRes = R.drawable.securewatch_logo
            )

            findNavController().navigate(action)
        }

        binding.recyclerViewEvents.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
