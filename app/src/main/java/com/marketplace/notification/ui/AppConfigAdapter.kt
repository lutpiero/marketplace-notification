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
import com.marketplace.notification.data.AppConfig

class AppConfigAdapter(
    private val onToggle: (AppConfig, Boolean) -> Unit,
    private val onEditDelay: (AppConfig) -> Unit,
    private val onEditFilters: (AppConfig) -> Unit,
    private val onDelete: (AppConfig) -> Unit
) : ListAdapter<AppConfig, AppConfigAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_config, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAppName: TextView = view.findViewById(R.id.tv_monitored_app_name)
        private val tvPackage: TextView = view.findViewById(R.id.tv_monitored_package)
        private val tvDelay: TextView = view.findViewById(R.id.tv_retrigger_delay)
        private val tvFiltersLabel: TextView = view.findViewById(R.id.tv_filters_label)
        private val tvTitleFilter: TextView = view.findViewById(R.id.tv_title_filter)
        private val tvContentFilter: TextView = view.findViewById(R.id.tv_content_filter)
        private val swEnabled: Switch = view.findViewById(R.id.sw_app_enabled)
        private val btnEditDelay: ImageButton = view.findViewById(R.id.btn_edit_delay)
        private val btnDeleteApp: ImageButton = view.findViewById(R.id.btn_delete_app)

        fun bind(config: AppConfig) {
            tvAppName.text = config.appName
            tvPackage.text = config.packageName
            tvDelay.text = itemView.context.getString(
                R.string.retrigger_delay_value, config.retriggerDelayMinutes
            )
            swEnabled.isChecked = config.enabled

            // Show/hide filters section
            val hasFilters = config.titleFilter.isNotBlank() || config.contentFilter.isNotBlank()
            tvFiltersLabel.visibility = if (hasFilters) View.VISIBLE else View.GONE
            
            if (config.titleFilter.isNotBlank()) {
                tvTitleFilter.visibility = View.VISIBLE
                tvTitleFilter.text = "Title: ${config.titleFilter}"
            } else {
                tvTitleFilter.visibility = View.GONE
            }
            
            if (config.contentFilter.isNotBlank()) {
                tvContentFilter.visibility = View.VISIBLE
                tvContentFilter.text = "Content: ${config.contentFilter}"
            } else {
                tvContentFilter.visibility = View.GONE
            }

            swEnabled.setOnCheckedChangeListener(null)
            swEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(config, checked)
            }

            btnEditDelay.setOnClickListener { onEditDelay(config) }
            btnDeleteApp.setOnClickListener { onDelete(config) }
            
            // Make filters clickable to edit
            if (hasFilters) {
                tvFiltersLabel.setOnClickListener { onEditFilters(config) }
                tvTitleFilter.setOnClickListener { onEditFilters(config) }
                tvContentFilter.setOnClickListener { onEditFilters(config) }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AppConfig>() {
        override fun areItemsTheSame(old: AppConfig, new: AppConfig) =
            old.packageName == new.packageName
        override fun areContentsTheSame(old: AppConfig, new: AppConfig) = old == new
    }
}
