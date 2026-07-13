package com.marketplace.notification.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.marketplace.notification.R
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.databinding.FragmentActionsTabBinding

class ActionsFragment : Fragment() {

    private var _binding: FragmentActionsTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var configViewModel: ConfigViewModel
    private lateinit var adapter: ActionConfigAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configViewModel = (requireActivity() as ConfigActivity).viewModel

        adapter = ActionConfigAdapter(
            onEdit = { config -> showActionDialog(config) },
            onDelete = { config -> confirmDeleteAction(config) },
            onToggle = { config, enabled ->
                configViewModel.saveAction(config.copy(enabled = enabled))
            }
        )

        binding.recyclerActions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ActionsFragment.adapter
        }

        configViewModel.allActions.observe(viewLifecycleOwner) { actions ->
            adapter.submitList(actions)
            binding.tvEmptyActions.visibility =
                if (actions.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddAction.setOnClickListener {
            showActionDialog(null)
        }
    }

    private fun showActionDialog(config: ActionConfig?) {
        ActionDialogFragment.newInstance(config) { savedConfig ->
            configViewModel.saveAction(savedConfig)
        }.show(childFragmentManager, "action_dialog")
    }

    private fun confirmDeleteAction(config: ActionConfig) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_action)
            .setMessage(getString(R.string.delete_action_confirm, config.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                configViewModel.deleteAction(config)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
