package com.marketplace.notification.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLogDao {
    
    @Insert
    suspend fun insert(log: ActionLog): Long
    
    @Query("SELECT * FROM action_logs WHERE notificationId = :notificationId ORDER BY timestamp DESC")
    fun getLogsForNotification(notificationId: Long): Flow<List<ActionLog>>
    
    @Query("SELECT * FROM action_logs WHERE notificationId = :notificationId ORDER BY timestamp DESC")
    suspend fun getLogsForNotificationSync(notificationId: Long): List<ActionLog>
    
    @Query("DELETE FROM action_logs WHERE notificationId = :notificationId")
    suspend fun deleteLogsForNotification(notificationId: Long)
    
    @Query("SELECT * FROM action_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<ActionLog>>
}
