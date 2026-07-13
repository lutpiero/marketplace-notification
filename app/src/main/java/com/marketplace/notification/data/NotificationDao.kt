package com.marketplace.notification.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isAcknowledged = 0 AND timestamp < :cutoffTime ORDER BY timestamp ASC")
    suspend fun getUnacknowledgedBefore(cutoffTime: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getById(id: Long): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isAcknowledged = 1, isRead = 1 WHERE id = :id")
    suspend fun markAsAcknowledged(id: Long)

    @Query("UPDATE notifications SET actionsSent = actionsSent + 1, lastActionTime = :time WHERE id = :id")
    suspend fun updateActionCount(id: Long, time: Long)

    @Query("DELETE FROM notifications WHERE isAcknowledged = 1 AND timestamp < :cutoffTime")
    suspend fun deleteOldAcknowledged(cutoffTime: Long)
}
