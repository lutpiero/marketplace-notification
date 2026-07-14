package com.marketplace.notification.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.marketplace.notification.R
import com.marketplace.notification.data.AppConfig
import com.marketplace.notification.databinding.FragmentAppsTabBinding

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var configViewModel: ConfigViewModel
    private lateinit var adapter: AppConfigAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configViewModel = (requireActivity() as ConfigActivity).viewModel

        adapter = AppConfigAdapter(
            onToggle = { config, enabled ->
                configViewModel.updateAppConfig(config.copy(enabled = enabled))
            },
            onEditDelay = { config -> showDelayDialog(config) },
            onEditFilters = { config -> showFiltersDialog(config) },
            onDelete = { config -> confirmDeleteApp(config) }
        )

        binding.recyclerApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AppsFragment.adapter
        }

        configViewModel.allApps.observe(viewLifecycleOwner) { apps ->
            adapter.submitList(apps)
            binding.tvEmptyApps.visibility =
                if (apps.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddApp.setOnClickListener {
            startActivity(Intent(requireContext(), AppSelectionActivity::class.java))
        }
    }

    private fun showDelayDialog(config: AppConfig) {
        val options = arrayOf("5 min", "10 min", "15 min", "30 min", "60 min", "120 min")
        val values = intArrayOf(5, 10, 15, 30, 60, 120)
        val currentIndex = values.indexOfFirst { it == config.retriggerDelayMinutes }.coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.retrigger_delay)
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                configViewModel.updateAppConfig(config.copy(retriggerDelayMinutes = values[which]))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showFiltersDialog(config: AppConfig) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_config, null)
        val etTitleFilter = dialogView.findViewById<android.widget.EditText>(R.id.et_title_filter)
        val etContentFilter = dialogView.findViewById<android.widget.EditText>(R.id.et_content_filter)
        
        etTitleFilter.setText(config.titleFilter)
        etContentFilter.setText(config.contentFilter)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Filters for ${config.appName}")
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val titleFilter = etTitleFilter.text.toString().trim()
                val contentFilter = etContentFilter.text.toString().trim()
                configViewModel.updateAppConfig(
                    config.copy(
                        titleFilter = titleFilter,
                        contentFilter = contentFilter
                    )
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDeleteApp(config: AppConfig) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.remove_app)
            .setMessage(getString(R.string.remove_app_confirm, config.appName))
            .setPositiveButton(R.string.remove) { _, _ ->
                configViewModel.deleteAppConfig(config)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
