package com.marketplace.notification.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marketplace.notification.R

class AppSelectionAdapter(
    private val onSelect: (AppSelectionActivity.AppInfo) -> Unit
) : ListAdapter<AppSelectionActivity.AppInfo, AppSelectionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_selection_app_name)
        private val tvPackage: TextView = view.findViewById(R.id.tv_selection_package)

        fun bind(appInfo: AppSelectionActivity.AppInfo) {
            tvName.text = appInfo.appName
            tvPackage.text = appInfo.packageName
            itemView.alpha = if (appInfo.isAdded) 0.5f else 1.0f
            itemView.isEnabled = !appInfo.isAdded
            itemView.setOnClickListener {
                if (!appInfo.isAdded) onSelect(appInfo)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AppSelectionActivity.AppInfo>() {
        override fun areItemsTheSame(
            old: AppSelectionActivity.AppInfo,
            new: AppSelectionActivity.AppInfo
        ) = old.packageName == new.packageName
        override fun areContentsTheSame(
            old: AppSelectionActivity.AppInfo,
            new: AppSelectionActivity.AppInfo
        ) = old == new
    }
}
