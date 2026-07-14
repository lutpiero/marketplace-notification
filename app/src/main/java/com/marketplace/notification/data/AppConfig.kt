package com.marketplace.notification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configs")
data class AppConfig(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    val retriggerDelayMinutes: Int = 30,
    val titleFilter: String = "",
    val contentFilter: String = ""
)
