package com.marketplace.notification.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marketplace.notification.R
import com.marketplace.notification.data.ActionLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActionLogAdapter : ListAdapter<ActionLog, ActionLogAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_action_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tv_log_icon)
        private val tvActionName: TextView = itemView.findViewById(R.id.tv_log_action_name)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_log_time)

        fun bind(log: ActionLog) {
            // Set icon based on success/failure
            if (log.success) {
                tvIcon.text = "✓"
                tvIcon.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            } else {
                tvIcon.text = "✗"
                tvIcon.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
            }

            // Set action name and type
            val actionText = if (log.success) {
                "${log.actionName} (${log.actionType})"
            } else {
                "${log.actionName} (${log.actionType}): ${log.errorMessage ?: "Failed"}"
            }
            tvActionName.text = actionText

            // Format timestamp
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            tvTime.text = timeFormat.format(Date(log.timestamp))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ActionLog>() {
        override fun areItemsTheSame(oldItem: ActionLog, newItem: ActionLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActionLog, newItem: ActionLog): Boolean {
            return oldItem == newItem
        }
    }
}
