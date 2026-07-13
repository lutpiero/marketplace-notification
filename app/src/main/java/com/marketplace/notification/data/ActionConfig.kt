package com.marketplace.notification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActionType {
    API_REQUEST,
    SCP_FILE,
    EMAIL,
    WHATSAPP
}

@Entity(tableName = "action_configs")
data class ActionConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: ActionType,
    val enabled: Boolean = true,
    // API fields
    val apiUrl: String = "",
    val apiMethod: String = "POST",
    val apiHeaders: String = "{}",
    val apiBodyTemplate: String = "",
    // SCP fields
    val scpHost: String = "",
    val scpPort: Int = 22,
    val scpUsername: String = "",
    val scpPassword: String = "",
    val scpRemotePath: String = "/tmp/notifications",
    // Email fields
    val emailSmtpHost: String = "",
    val emailSmtpPort: Int = 587,
    val emailUsername: String = "",
    val emailPassword: String = "",
    val emailFrom: String = "",
    val emailTo: String = "",
    val emailSubject: String = "Marketplace Notification Alert",
    // WhatsApp fields
    val whatsappRecipient: String = "",
    val whatsappApiUrl: String = "",
    val whatsappApiKey: String = ""
)
