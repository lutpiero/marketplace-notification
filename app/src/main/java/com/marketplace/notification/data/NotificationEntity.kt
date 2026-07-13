package com.marketplace.notification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isAcknowledged: Boolean = false,
    val actionsSent: Int = 0,
    val lastActionTime: Long = 0
)
