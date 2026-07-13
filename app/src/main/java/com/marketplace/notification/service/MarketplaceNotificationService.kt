package com.marketplace.notification.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.marketplace.notification.action.ActionExecutor
import com.marketplace.notification.data.AppDatabase
import com.marketplace.notification.data.NotificationEntity
import com.marketplace.notification.data.NotificationRepository
import com.marketplace.notification.worker.ReTriggerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MarketplaceNotificationService : NotificationListenerService() {

    private val tag = "MarketplaceNotifSvc"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var repository: NotificationRepository
    private lateinit var actionExecutor: ActionExecutor

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(this)
        repository = NotificationRepository(db)
        actionExecutor = ActionExecutor(this)
        Log.i(tag, "Notification listener service started")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.i(tag, "Notification listener service destroyed")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            try {
                // Only handle notifications from monitored packages
                val enabledPackages = repository.getEnabledPackageNames()
                if (sbn.packageName !in enabledPackages) return@launch

                // Skip group summary and ongoing notifications
                if (sbn.isGroup && sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0) return@launch
                if (sbn.notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0) return@launch

                val extras = sbn.notification.extras
                val title = extras.getString("android.title") ?: ""
                val text = extras.getCharSequence("android.text")?.toString() ?: ""

                if (title.isBlank() && text.isBlank()) return@launch

                val appName = resolveAppName(sbn.packageName)

                val notification = NotificationEntity(
                    packageName = sbn.packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                val id = repository.insertNotification(notification)
                if (id <= 0) return@launch // duplicate or insert failed

                val savedNotification = notification.copy(id = id)
                Log.i(tag, "Captured notification $id from $appName: $title")

                // Execute all enabled actions immediately
                val actions = repository.getEnabledActions()
                for (action in actions) {
                    val result = actionExecutor.execute(action, savedNotification)
                    if (result.isSuccess) {
                        repository.updateActionCount(id, System.currentTimeMillis())
                    }
                }

                // Schedule periodic re-trigger checks
                ReTriggerScheduler.schedule(applicationContext)

            } catch (e: Exception) {
                Log.e(tag, "Error processing notification from ${sbn.packageName}", e)
            }
        }
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
