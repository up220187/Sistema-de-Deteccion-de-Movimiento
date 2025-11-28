package com.example.sistemamovimiento.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sistemamovimiento.data.UserSession
import androidx.navigation.fragment.findNavController
import com.example.sistemamovimiento.FakeData
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.databinding.CardStatItemBinding
import com.example.sistemamovimiento.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var statTodayBinding: CardStatItemBinding
    private lateinit var statWeekBinding: CardStatItemBinding
    private lateinit var statActiveBinding: CardStatItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Bindings de los include
        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!UserSession.isLogged(requireContext())) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
            return
        }

        setupStats()
        setupButton()
        setupToolbarMenu()

    }

    private fun setupStats() {

        statTodayBinding.textStatValue.text = FakeData.stats.today.toString()
        statTodayBinding.textStatLabel.text = "Hoy"

        statWeekBinding.textStatValue.text = FakeData.stats.week.toString()
        statWeekBinding.textStatLabel.text = "Semana"

        statActiveBinding.textStatValue.text = FakeData.stats.active.toString()
        statActiveBinding.textStatLabel.text = "Activos"
    }

    private fun setupButton() {
        binding.buttonViewDetails.setOnClickListener {
            val e = FakeData.events[0]

            val action = HomeFragmentDirections
                .actionHomeFragmentToDetailFragment(
                    title = e.title,
                    location = e.location,
                    timestamp = e.timestamp
                )

            findNavController().navigate(action)
        }
    }

    private fun setupToolbarMenu() {
        binding.toolbarMain.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.action_notifications -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToHistoryFragment()
                    )
                    true
                }

                R.id.menu_history -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToHistoryFragment()
                    )
                    true
                }

                R.id.menu_settings -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                    )
                    true
                }
                R.id.menu_logout -> {
                    UserSession.logout(requireContext())

                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                    )
                    true
                }


                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
