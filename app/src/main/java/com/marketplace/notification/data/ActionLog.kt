package com.marketplace.notification.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "action_logs",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["notificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notificationId")]
)
data class ActionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationId: Long,
    val actionName: String,
    val actionType: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
