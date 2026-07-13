package com.marketplace.notification.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marketplace.notification.R
import com.marketplace.notification.data.NotificationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onRead: (NotificationEntity) -> Unit,
    private val onAcknowledge: (NotificationEntity) -> Unit,
    private val onDelete: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback()) {

    fun getNotification(position: Int): NotificationEntity = currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAppName: TextView = view.findViewById(R.id.tv_app_name)
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvText: TextView = view.findViewById(R.id.tv_text)
        private val tvTime: TextView = view.findViewById(R.id.tv_time)
        private val tvStatus: TextView = view.findViewById(R.id.tv_status)
        private val btnRead: ImageButton = view.findViewById(R.id.btn_read)
        private val btnAcknowledge: ImageButton = view.findViewById(R.id.btn_acknowledge)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
        private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        fun bind(notification: NotificationEntity) {
            tvAppName.text = notification.appName
            tvTitle.text = notification.title
            tvText.text = notification.text
            tvTime.text = dateFormat.format(Date(notification.timestamp))

            // Status indicator
            val ctx = itemView.context
            when {
                notification.isAcknowledged -> {
                    tvStatus.text = ctx.getString(R.string.status_acknowledged)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_acknowledged))
                    itemView.alpha = 0.6f
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                notification.isRead -> {
                    tvStatus.text = ctx.getString(R.string.status_read)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_read))
                    itemView.alpha = 0.85f
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                else -> {
                    tvStatus.text = ctx.getString(R.string.status_new)
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_new))
                    itemView.alpha = 1.0f
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

            btnRead.isEnabled = !notification.isRead
            btnRead.alpha = if (notification.isRead) 0.4f else 1.0f

            btnAcknowledge.isEnabled = !notification.isAcknowledged
            btnAcknowledge.alpha = if (notification.isAcknowledged) 0.4f else 1.0f

            btnRead.setOnClickListener { onRead(notification) }
            btnAcknowledge.setOnClickListener { onAcknowledge(notification) }
            btnDelete.setOnClickListener { onDelete(notification) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(old: NotificationEntity, new: NotificationEntity) =
            old.id == new.id
        override fun areContentsTheSame(old: NotificationEntity, new: NotificationEntity) =
            old == new
    }
}
