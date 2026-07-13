package com.marketplace.notification.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.marketplace.notification.data.AppDatabase
import com.marketplace.notification.data.NotificationEntity
import com.marketplace.notification.data.NotificationRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository = NotificationRepository(
        AppDatabase.getDatabase(application)
    )

    val notifications: LiveData<List<NotificationEntity>> = repository.allNotifications

    fun markAsRead(id: Long) = viewModelScope.launch {
        repository.markAsRead(id)
    }

    fun markAsAcknowledged(id: Long) = viewModelScope.launch {
        repository.markAsAcknowledged(id)
    }

    fun deleteNotification(notification: NotificationEntity) = viewModelScope.launch {
        repository.deleteNotification(notification)
    }
}
