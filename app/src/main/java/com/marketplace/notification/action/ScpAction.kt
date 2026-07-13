package com.marketplace.notification.action

import android.content.Context
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

class ScpAction(private val context: Context) {

    suspend fun execute(action: ActionConfig, notification: NotificationEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val jsch = JSch()
                val session = jsch.getSession(action.scpUsername, action.scpHost, action.scpPort)
                session.setPassword(action.scpPassword)

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                session.setConfig(config)
                session.connect(15_000)

                val content = buildFileContent(notification)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(Date(notification.timestamp))
                val fileName = "notification_${notification.id}_$timestamp.json"
                val remotePath = action.scpRemotePath.trimEnd('/') + "/$fileName"

                scpUpload(session, content.toByteArray(Charsets.UTF_8), remotePath)
                session.disconnect()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun scpUpload(session: com.jcraft.jsch.Session, data: ByteArray, remotePath: String) {
        val channel = session.openChannel("exec") as ChannelExec
        channel.setCommand("scp -t $remotePath")

        val outputStream = channel.outputStream
        val inputStream = channel.inputStream
        channel.connect()

        checkAck(inputStream)

        // Send file header: "C<permissions> <size> <filename>\n"
        val fileName = remotePath.substringAfterLast('/')
        val header = "C0644 ${data.size} $fileName\n"
        outputStream.write(header.toByteArray(Charsets.UTF_8))
        outputStream.flush()
        checkAck(inputStream)

        // Send file content
        val dataStream = ByteArrayInputStream(data)
        val buf = ByteArray(4096)
        var len: Int
        while (dataStream.read(buf).also { len = it } != -1) {
            outputStream.write(buf, 0, len)
        }
        // Send null byte to signal end of file
        outputStream.write(0)
        outputStream.flush()
        checkAck(inputStream)

        channel.disconnect()
    }

    private fun checkAck(inputStream: java.io.InputStream) {
        val b = inputStream.read()
        if (b != 0) throw Exception("SCP protocol error: received $b")
    }

    private fun buildFileContent(notification: NotificationEntity): String {
        return JSONObject().apply {
            put("id", notification.id)
            put("app", notification.appName)
            put("package", notification.packageName)
            put("title", notification.title)
            put("text", notification.text)
            put("timestamp", notification.timestamp)
            put("datetime", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .format(Date(notification.timestamp)))
            put("actionsSent", notification.actionsSent)
        }.toString(2)
    }
}
