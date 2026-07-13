package com.marketplace.notification.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WhatsAppAction(private val context: Context) {

    suspend fun execute(action: ActionConfig, notification: NotificationEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (action.whatsappApiKey.isNotBlank() && action.whatsappApiUrl.isNotBlank()) {
                sendViaApi(action, notification)
            } else {
                sendViaIntent(action, notification)
            }
        }

    private fun sendViaApi(action: ActionConfig, notification: NotificationEntity): Result<Unit> {
        return try {
            val url = URL(action.whatsappApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Authorization", "Bearer ${action.whatsappApiKey}")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val body = JSONObject().apply {
                put("messaging_product", "whatsapp")
                put("to", action.whatsappRecipient)
                put("type", "text")
                put("text", JSONObject().put("body", buildMessage(notification)))
            }.toString()

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode in 200..299) Result.success(Unit)
            else Result.failure(Exception("WhatsApp API HTTP $responseCode"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sendViaIntent(action: ActionConfig, notification: NotificationEntity): Result<Unit> {
        return try {
            val message = buildMessage(notification)
            val encoded = URLEncoder.encode(message, "UTF-8")
            val number = action.whatsappRecipient.filter { it.isDigit() || it == '+' }
            val uri = Uri.parse("https://wa.me/$number?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildMessage(notification: NotificationEntity): String =
        "🛒 *Marketplace Alert*\n" +
        "App: ${notification.appName}\n" +
        "Title: ${notification.title}\n" +
        "Message: ${notification.text}"
}
