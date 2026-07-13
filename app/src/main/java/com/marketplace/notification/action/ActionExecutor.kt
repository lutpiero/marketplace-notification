package com.marketplace.notification.action

import android.content.Context
import android.util.Log
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType
import com.marketplace.notification.data.NotificationEntity

class ActionExecutor(private val context: Context) {

    private val tag = "ActionExecutor"

    suspend fun execute(action: ActionConfig, notification: NotificationEntity): Result<Unit> {
        Log.d(tag, "Executing action '${action.name}' (${action.type}) for notification ${notification.id}")
        return when (action.type) {
            ActionType.API_REQUEST -> ApiAction(context).execute(action, notification)
            ActionType.SCP_FILE    -> ScpAction(context).execute(action, notification)
            ActionType.EMAIL       -> EmailAction(context).execute(action, notification)
            ActionType.WHATSAPP    -> WhatsAppAction(context).execute(action, notification)
        }.also { result ->
            if (result.isFailure) {
                Log.e(tag, "Action '${action.name}' failed: ${result.exceptionOrNull()?.message}")
            } else {
                Log.i(tag, "Action '${action.name}' succeeded")
            }
        }
    }
}
