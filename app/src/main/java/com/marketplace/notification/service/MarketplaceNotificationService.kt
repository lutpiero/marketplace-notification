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
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
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

                // Get app config to check filters
                val appConfig = db.appConfigDao().getEnabledApps().find { it.packageName == sbn.packageName }
                if (appConfig != null) {
                    // Apply filters if configured
                    val titleFilter = appConfig.titleFilter.trim()
                    val contentFilter = appConfig.contentFilter.trim()
                    
                    if (titleFilter.isNotBlank() || contentFilter.isNotBlank()) {
                        var matchesFilter = false
                        
                        // Check title filter (case-insensitive)
                        if (titleFilter.isNotBlank() && title.contains(titleFilter, ignoreCase = true)) {
                            matchesFilter = true
                        }
                        
                        // Check content filter (case-insensitive)
                        if (contentFilter.isNotBlank() && text.contains(contentFilter, ignoreCase = true)) {
                            matchesFilter = true
                        }
                        
                        // If filters are set but notification doesn't match, ignore it
                        if (!matchesFilter) {
                            Log.d(tag, "Notification from ${sbn.packageName} filtered out: title='$title', text='$text'")
                            return@launch
                        }
                    }
                }

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
                val actionResults = mutableListOf<Pair<String, Boolean>>()
                
                for (action in actions) {
                    val result = actionExecutor.execute(action, savedNotification)
                    val success = result.isSuccess
                    actionResults.add(action.name to success)
                    
                    // Log action execution
                    val actionLog = com.marketplace.notification.data.ActionLog(
                        notificationId = id,
                        actionName = action.name,
                        actionType = action.type.name,
                        success = success,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                    db.actionLogDao().insert(actionLog)
                    
                    if (success) {
                        repository.updateActionCount(id, System.currentTimeMillis())
                    }
                }
                
                // Log summary of action execution
                if (actionResults.isNotEmpty()) {
                    val successCount = actionResults.count { it.second }
                    val failureCount = actionResults.size - successCount
                    Log.i(tag, "Notification $id: Forwarding completed. Actions executed: $successCount succeeded, $failureCount failed")
                    
                    if (failureCount > 0) {
                        val failedActions = actionResults.filter { !it.second }.map { it.first }
                        Log.w(tag, "Notification $id: Failed actions: ${failedActions.joinToString(", ")}")
                    }
                } else {
                    Log.i(tag, "Notification $id: No actions configured to execute")
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
