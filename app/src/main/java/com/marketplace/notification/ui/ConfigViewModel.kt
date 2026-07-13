package com.marketplace.notification.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.marketplace.notification.data.ActionConfig
import com.marketplace.notification.data.AppConfig
import com.marketplace.notification.data.AppDatabase
import com.marketplace.notification.data.NotificationRepository
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository = NotificationRepository(
        AppDatabase.getDatabase(application)
    )

    val allActions: LiveData<List<ActionConfig>> = repository.allActions
    val allApps: LiveData<List<AppConfig>> = repository.allApps

    // Action config operations
    fun saveAction(config: ActionConfig) = viewModelScope.launch {
        if (config.id == 0L) {
            repository.insertAction(config)
        } else {
            repository.updateAction(config)
        }
    }

    fun deleteAction(config: ActionConfig) = viewModelScope.launch {
        repository.deleteAction(config)
    }

    // App config operations
    fun saveAppConfig(config: AppConfig) = viewModelScope.launch {
        repository.insertAppConfig(config)
    }

    fun updateAppConfig(config: AppConfig) = viewModelScope.launch {
        repository.updateAppConfig(config)
    }

    fun deleteAppConfig(config: AppConfig) = viewModelScope.launch {
        repository.deleteAppConfig(config)
    }
}
