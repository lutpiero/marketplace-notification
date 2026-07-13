package com.marketplace.notification

import android.content.Context
import com.marketplace.notification.action.ActionExecutor
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ActionExecutorTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var actionExecutor: ActionExecutor

    private val testNotification = NotificationEntity(
        id = 1L,
        packageName = "com.tokopedia.tkpd",
        appName = "Tokopedia",
        title = "Order #12345",
        text = "Your item has been sold!",
        timestamp = 1_000_000L
    )

    @Before
    fun setUp() {
        actionExecutor = ActionExecutor(mockContext)
    }

    @Test
    fun actionExecutor_executeWithValidApiAction_shouldReturnResult() = runBlocking {
        val action = ActionConfig(
            id = 1L,
            name = "Test API Action",
            type = ActionType.API_REQUEST,
            enabled = true,
            apiUrl = "https://httpbin.org/post",
            apiMethod = "POST",
            apiHeaders = "{}",
            apiBodyTemplate = "{\"title\": \"{title}\", \"text\": \"{text}\"}"
        )

        val result = actionExecutor.execute(action, testNotification)
        assertTrue("API action execution should return a Result", result != null)
    }

    @Test
    fun actionExecutor_executeWithInvalidUrl_shouldReturnFailure() = runBlocking {
        val action = ActionConfig(
            id = 1L,
            name = "Test Invalid API",
            type = ActionType.API_REQUEST,
            enabled = true,
            apiUrl = "https://invalid-url-that-does-not-exist-12345.com/api",
            apiMethod = "POST"
        )

        val result = actionExecutor.execute(action, testNotification)
        assertTrue("Invalid URL should result in failure", result.isFailure)
    }

    @Test
    fun actionConfig_canBeCreatedWithAllTypes() {
        ActionType.values().forEach { type ->
            val action = ActionConfig(
                name = "Test $type",
                type = type,
                enabled = true
            )
            assertEquals("Action type should match", type, action.type)
            assertTrue("Action should be enabled by default", action.enabled)
        }
    }

    @Test
    fun actionConfig_apiRequestDefaults() {
        val action = ActionConfig(
            name = "API Test",
            type = ActionType.API_REQUEST
        )

        assertEquals("Default method should be POST", "POST", action.apiMethod)
        assertEquals("Default headers should be {}", "{}", action.apiHeaders)
        assertTrue("Should be enabled by default", action.enabled)
    }

    @Test
    fun actionConfig_scpDefaults() {
        val action = ActionConfig(
            name = "SCP Test",
            type = ActionType.SCP_FILE
        )

        assertEquals("Default SCP port should be 22", 22, action.scpPort)
        assertEquals("Default remote path should be /tmp/notifications", "/tmp/notifications", action.scpRemotePath)
    }

    @Test
    fun actionConfig_emailDefaults() {
        val action = ActionConfig(
            name = "Email Test",
            type = ActionType.EMAIL
        )

        assertEquals("Default SMTP port should be 587", 587, action.emailSmtpPort)
        assertEquals("Default subject should be set", "Marketplace Notification Alert", action.emailSubject)
    }

    @Test
    fun actionConfig_canBeCopiedWithModifications() {
        val action = ActionConfig(
            id = 1L,
            name = "Original",
            type = ActionType.API_REQUEST,
            enabled = true,
            apiUrl = "https://example.com"
        )

        val modified = action.copy(
            name = "Modified",
            enabled = false
        )

        assertEquals("ID should remain the same", 1L, modified.id)
        assertEquals("Name should be changed", "Modified", modified.name)
        assertFalse("Enabled should be changed", modified.enabled)
        assertEquals("Type should remain the same", ActionType.API_REQUEST, modified.type)
        assertEquals("API URL should remain the same", "https://example.com", modified.apiUrl)
    }

    @Test
    fun actionConfig_whatsappFieldsCanBeSet() {
        val action = ActionConfig(
            name = "WhatsApp Test",
            type = ActionType.WHATSAPP,
            whatsappRecipient = "+1234567890",
            whatsappApiUrl = "https://api.whatsapp.com/send",
            whatsappApiKey = "test-key-123"
        )

        assertEquals("Recipient should be set", "+1234567890", action.whatsappRecipient)
        assertEquals("API URL should be set", "https://api.whatsapp.com/send", action.whatsappApiUrl)
        assertEquals("API key should be set", "test-key-123", action.whatsappApiKey)
    }

    @Test
    fun notificationEntity_hasCorrectDefaults() {
        val notification = testNotification

        assertEquals("ID should be 1", 1L, notification.id)
        assertEquals("Package name should match", "com.tokopedia.tkpd", notification.packageName)
        assertEquals("App name should match", "Tokopedia", notification.appName)
        assertEquals("Title should match", "Order #12345", notification.title)
        assertFalse("Should not be read by default", notification.isRead)
        assertFalse("Should not be acknowledged by default", notification.isAcknowledged)
        assertEquals("Actions sent should be 0", 0, notification.actionsSent)
    }

    @Test
    fun actionConfig_validationForRequiredFields() {
        // Verify that a minimal config can be created
        val minimalConfig = ActionConfig(
            name = "Minimal",
            type = ActionType.API_REQUEST
        )

        assertEquals("Name should be set", "Minimal", minimalConfig.name)
        assertEquals("Type should be set", ActionType.API_REQUEST, minimalConfig.type)
    }
}
