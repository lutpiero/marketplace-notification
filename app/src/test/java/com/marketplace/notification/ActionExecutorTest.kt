package com.marketplace.notification

import android.content.Context
import com.marketplace.notification.action.ActionExecutor
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType
import com.marketplace.notification.data.NotificationEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun actionExecutor_canBeInstantiatedWithMockedContext() {
        // This test verifies that ActionExecutor can be created with a mock context
        assertNotNull("ActionExecutor should be instantiated with mock context", actionExecutor)
    }

    @Test
    fun actionConfig_allFieldsCanBeSetAndRetrieved() = runBlocking {
        val action = ActionConfig(
            id = 1L,
            name = "Complete Config",
            type = ActionType.API_REQUEST,
            enabled = true,
            apiUrl = "https://api.example.com/notify",
            apiMethod = "POST",
            apiHeaders = "{\"Authorization\": \"token123\"}",
            apiBodyTemplate = "{\"notification\": \"{title}\"}"
        )

        assertEquals("ID should be set", 1L, action.id)
        assertEquals("Name should be set", "Complete Config", action.name)
        assertEquals("Type should be API_REQUEST", ActionType.API_REQUEST, action.type)
        assertTrue("Should be enabled", action.enabled)
        assertEquals("URL should be set", "https://api.example.com/notify", action.apiUrl)
        assertEquals("Method should be POST", "POST", action.apiMethod)
        assertEquals("Headers should be set", "{\"Authorization\": \"token123\"}", action.apiHeaders)
    }

    @Test
    fun actionExecutor_isNotNull() {
        // Verify that ActionExecutor can be instantiated and is not null
        assertNotNull("ActionExecutor should be created", actionExecutor)
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
    fun actionConfig_canBeCreatedWithMinimalFields() {
        // Verify that a minimal config can be created with required fields only
        val minimalConfig = ActionConfig(
            name = "Minimal",
            type = ActionType.API_REQUEST
        )

        assertEquals("Name should be set", "Minimal", minimalConfig.name)
        assertEquals("Type should be set", ActionType.API_REQUEST, minimalConfig.type)
    }
}
