package com.marketplace.notification.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActionConfigDao {

    @Query("SELECT * FROM action_configs ORDER BY name ASC")
    fun getAllActions(): LiveData<List<ActionConfig>>

    @Query("SELECT * FROM action_configs WHERE enabled = 1 ORDER BY name ASC")
    suspend fun getEnabledActions(): List<ActionConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ActionConfig): Long

    @Update
    suspend fun update(config: ActionConfig)

    @Delete
    suspend fun delete(config: ActionConfig)
}
