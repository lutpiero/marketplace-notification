package com.marketplace.notification.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marketplace.notification.action.ActionExecutor
import com.marketplace.notification.data.AppDatabase
import com.marketplace.notification.data.NotificationRepository

class ReTriggerWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val tag = "ReTriggerWorker"

    override suspend fun doWork(): Result {
        Log.d(tag, "ReTriggerWorker running")
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = NotificationRepository(db)
            val actionExecutor = ActionExecutor(applicationContext)

            // Determine the shortest re-trigger window configured across all enabled apps
            val enabledApps = repository.getEnabledApps()
            if (enabledApps.isEmpty()) return Result.success()

            val minDelayMs = enabledApps.minOf { it.retriggerDelayMinutes } * 60_000L
            val cutoffTime = System.currentTimeMillis() - minDelayMs

            val unacknowledged = repository.getUnacknowledgedBefore(cutoffTime)
            Log.d(tag, "Found ${unacknowledged.size} unacknowledged notifications to re-trigger")

            for (notification in unacknowledged) {
                val actions = repository.getEnabledActions()
                for (action in actions) {
                    val result = actionExecutor.execute(action, notification)
                    if (result.isSuccess) {
                        repository.updateActionCount(notification.id, System.currentTimeMillis())
                        Log.i(tag, "Re-triggered action '${action.name}' for notification ${notification.id}")
                    }
                }
            }

            // Clean up acknowledged notifications older than 7 days
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            repository.deleteOldAcknowledged(weekAgo)

            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "ReTriggerWorker failed", e)
            Result.retry()
        }
    }
}
