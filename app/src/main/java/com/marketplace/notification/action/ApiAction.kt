package com.marketplace.notification.action

import android.content.Context
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiAction(private val context: Context) {

    suspend fun execute(action: ActionConfig, notification: NotificationEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(action.apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = action.apiMethod
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }

                // Apply custom headers from JSON string
                parseHeaders(action.apiHeaders).forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                if (action.apiMethod != "GET" && action.apiMethod != "HEAD") {
                    connection.doOutput = true
                    val body = buildPayload(action.apiBodyTemplate, notification)
                    OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                if (responseCode in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP $responseCode returned for ${action.apiUrl}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseHeaders(headersJson: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        return try {
            val obj = JSONObject(headersJson)
            obj.keys().forEach { key -> map[key] = obj.getString(key) }
            map
        } catch (e: Exception) {
            map
        }
    }

    private fun buildPayload(template: String, notification: NotificationEntity): String {
        if (template.isNotBlank()) {
            return template
                .replace("{app}", notification.appName)
                .replace("{title}", notification.title)
                .replace("{text}", notification.text)
                .replace("{timestamp}", notification.timestamp.toString())
                .replace("{packageName}", notification.packageName)
        }
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .format(Date(notification.timestamp))
        return JSONObject().apply {
            put("app", notification.appName)
            put("package", notification.packageName)
            put("title", notification.title)
            put("text", notification.text)
            put("timestamp", notification.timestamp)
            put("datetime", dateStr)
        }.toString()
    }
}
