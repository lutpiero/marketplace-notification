package com.marketplace.notification.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppConfigDao {

    @Query("SELECT * FROM app_configs ORDER BY appName ASC")
    fun getAllApps(): LiveData<List<AppConfig>>

    @Query("SELECT * FROM app_configs WHERE enabled = 1")
    suspend fun getEnabledApps(): List<AppConfig>

    @Query("SELECT packageName FROM app_configs WHERE enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AppConfig)

    @Update
    suspend fun update(config: AppConfig)

    @Delete
    suspend fun delete(config: AppConfig)

    @Query("SELECT COUNT(*) FROM app_configs WHERE packageName = :packageName")
    suspend fun exists(packageName: String): Int
}
