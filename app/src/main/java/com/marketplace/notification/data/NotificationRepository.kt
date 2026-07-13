package com.marketplace.notification.data

import androidx.lifecycle.LiveData

class NotificationRepository(private val db: AppDatabase) {

    private val notificationDao = db.notificationDao()
    private val actionConfigDao = db.actionConfigDao()
    private val appConfigDao = db.appConfigDao()

    // Notifications
    val allNotifications: LiveData<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insert(notification)

    suspend fun markAsRead(id: Long) = notificationDao.markAsRead(id)

    suspend fun markAsAcknowledged(id: Long) = notificationDao.markAsAcknowledged(id)

    suspend fun updateNotification(notification: NotificationEntity) =
        notificationDao.update(notification)

    suspend fun deleteNotification(notification: NotificationEntity) =
        notificationDao.delete(notification)

    suspend fun getUnacknowledgedBefore(cutoffTime: Long): List<NotificationEntity> =
        notificationDao.getUnacknowledgedBefore(cutoffTime)

    suspend fun updateActionCount(id: Long, time: Long) =
        notificationDao.updateActionCount(id, time)

    suspend fun deleteOldAcknowledged(cutoffTime: Long) =
        notificationDao.deleteOldAcknowledged(cutoffTime)

    // Action configs
    val allActions: LiveData<List<ActionConfig>> = actionConfigDao.getAllActions()

    suspend fun insertAction(config: ActionConfig): Long = actionConfigDao.insert(config)

    suspend fun updateAction(config: ActionConfig) = actionConfigDao.update(config)

    suspend fun deleteAction(config: ActionConfig) = actionConfigDao.delete(config)

    suspend fun getEnabledActions(): List<ActionConfig> = actionConfigDao.getEnabledActions()

    // App configs
    val allApps: LiveData<List<AppConfig>> = appConfigDao.getAllApps()

    suspend fun insertAppConfig(config: AppConfig) = appConfigDao.insert(config)

    suspend fun updateAppConfig(config: AppConfig) = appConfigDao.update(config)

    suspend fun deleteAppConfig(config: AppConfig) = appConfigDao.delete(config)

    suspend fun getEnabledPackageNames(): List<String> = appConfigDao.getEnabledPackageNames()

    suspend fun getEnabledApps(): List<AppConfig> = appConfigDao.getEnabledApps()

    suspend fun appExists(packageName: String): Boolean =
        appConfigDao.exists(packageName) > 0
}
