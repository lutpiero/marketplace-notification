package com.marketplace.notification.action

import android.content.Context
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailAction(private val context: Context) {

    suspend fun execute(action: ActionConfig, notification: NotificationEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", action.emailSmtpHost)
                    put("mail.smtp.port", action.emailSmtpPort.toString())
                    put("mail.smtp.connectiontimeout", "15000")
                    put("mail.smtp.timeout", "15000")
                }

                val mailSession = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication =
                        PasswordAuthentication(action.emailUsername, action.emailPassword)
                })

                val message = MimeMessage(mailSession).apply {
                    setFrom(InternetAddress(action.emailFrom))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(action.emailTo))
                    subject = "${action.emailSubject} — ${notification.appName}"
                    setText(buildEmailBody(notification), "UTF-8")
                    sentDate = Date()
                }

                Transport.send(message)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun buildEmailBody(notification: NotificationEntity): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(notification.timestamp))
        return """
            |Marketplace Notification Alert
            |================================
            |App     : ${notification.appName}
            |Package : ${notification.packageName}
            |Title   : ${notification.title}
            |Message : ${notification.text}
            |Time    : $dateStr
            |
            |-- Marketplace Notification App
        """.trimMargin()
    }
}
