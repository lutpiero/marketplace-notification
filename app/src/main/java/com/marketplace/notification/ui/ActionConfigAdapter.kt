package com.marketplace.notification.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marketplace.notification.R
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType

class ActionConfigAdapter(
    private val onEdit: (ActionConfig) -> Unit,
    private val onDelete: (ActionConfig) -> Unit,
    private val onToggle: (ActionConfig, Boolean) -> Unit
) : ListAdapter<ActionConfig, ActionConfigAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_action_config, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_action_name)
        private val tvType: TextView = view.findViewById(R.id.tv_action_type)
        private val swEnabled: Switch = view.findViewById(R.id.sw_action_enabled)
        private val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_action)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_action)

        fun bind(config: ActionConfig) {
            tvName.text = config.name
            tvType.text = actionTypeLabel(config.type)
            swEnabled.isChecked = config.enabled

            swEnabled.setOnCheckedChangeListener(null)
            swEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(config, checked)
            }

            btnEdit.setOnClickListener { onEdit(config) }
            btnDelete.setOnClickListener { onDelete(config) }

            itemView.setOnClickListener { onEdit(config) }
        }

        private fun actionTypeLabel(type: ActionType): String = when (type) {
            ActionType.API_REQUEST -> "API Request"
            ActionType.SCP_FILE    -> "SCP File"
            ActionType.EMAIL       -> "Email"
            ActionType.WHATSAPP    -> "WhatsApp"
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ActionConfig>() {
        override fun areItemsTheSame(old: ActionConfig, new: ActionConfig) = old.id == new.id
        override fun areContentsTheSame(old: ActionConfig, new: ActionConfig) = old == new
    }
}
