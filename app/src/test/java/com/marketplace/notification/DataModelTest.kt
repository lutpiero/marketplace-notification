package com.marketplace.notification

import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.ActionType
import com.marketplace.notification.data.AppConfig
import com.marketplace.notification.data.NotificationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelTest {

    @Test
    fun notificationEntity_defaultValues() {
        val notification = NotificationEntity(
            packageName = "com.tokopedia.tkpd",
            appName = "Tokopedia",
            title = "Order #12345",
            text = "Your item has been sold!",
            timestamp = 1_000_000L
        )

        assertFalse("Default isRead should be false", notification.isRead)
        assertFalse("Default isAcknowledged should be false", notification.isAcknowledged)
        assertEquals("Default actionsSent should be 0", 0, notification.actionsSent)
        assertEquals("Default lastActionTime should be 0", 0L, notification.lastActionTime)
    }

    @Test
    fun notificationEntity_copyWithAcknowledged() {
        val notification = NotificationEntity(
            id = 1L,
            packageName = "com.tokopedia.tkpd",
            appName = "Tokopedia",
            title = "Order",
            text = "Sold!",
            timestamp = 1_000_000L
        )
        val acknowledged = notification.copy(isAcknowledged = true, isRead = true)

        assertTrue("isAcknowledged should be true", acknowledged.isAcknowledged)
        assertTrue("isRead should be true", acknowledged.isRead)
        assertEquals("id should remain the same", 1L, acknowledged.id)
    }

    @Test
    fun actionConfig_defaultValues() {
        val action = ActionConfig(
            name = "Test API",
            type = ActionType.API_REQUEST
        )

        assertTrue("Default enabled should be true", action.enabled)
        assertEquals("Default apiMethod should be POST", "POST", action.apiMethod)
        assertEquals("Default apiHeaders should be {}", "{}", action.apiHeaders)
        assertEquals("Default scpPort should be 22", 22, action.scpPort)
        assertEquals("Default emailSmtpPort should be 587", 587, action.emailSmtpPort)
    }

    @Test
    fun appConfig_defaultValues() {
        val appConfig = AppConfig(
            packageName = "com.tokopedia.tkpd",
            appName = "Tokopedia"
        )

        assertTrue("Default enabled should be true", appConfig.enabled)
        assertEquals("Default retriggerDelayMinutes should be 30", 30, appConfig.retriggerDelayMinutes)
    }

    @Test
    fun actionType_ordinalMapping() {
        assertEquals(0, ActionType.API_REQUEST.ordinal)
        assertEquals(1, ActionType.SCP_FILE.ordinal)
        assertEquals(2, ActionType.EMAIL.ordinal)
        assertEquals(3, ActionType.WHATSAPP.ordinal)
    }

    @Test
    fun actionConfig_allTypes_canBeCreated() {
        ActionType.values().forEach { type ->
            val config = ActionConfig(name = "Test $type", type = type)
            assertEquals(type, config.type)
        }
    }

    @Test
    fun retriggerCutoffTime_calculation() {
        val delayMinutes = 30
        val now = 1_000_000_000L
        val cutoffTime = now - (delayMinutes * 60 * 1000L)
        assertEquals(now - 1_800_000L, cutoffTime)
    }
}
