package com.marketplace.notification.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromActionType(value: ActionType): String = value.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)
}
