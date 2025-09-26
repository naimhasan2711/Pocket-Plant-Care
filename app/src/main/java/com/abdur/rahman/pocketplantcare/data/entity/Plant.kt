package com.abdur.rahman.pocketplantcare.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Plant entity representing a plant entry in the Room database.
 * 
 * This entity stores all the essential information about a user's plant including
 * basic details, care information, and optional media. Used as the core data model
 * for the MVVM architecture following Android's recommended Room database patterns.
 * 
 * @property id Unique identifier for each plant (auto-generated)
 * @property name User-defined name for the plant
 * @property notes Optional care notes or observations
 * @property lastWateredDate Date when the plant was last watered
 * @property imagePath Optional file path to the plant's photo
 * @property reminderId Identifier for the associated watering reminder alarm
 * @property reminderEnabled Whether watering reminders are active for this plant
 * @property reminderHour Hour of day (0-23) when reminder should trigger
 * @property reminderMinute Minute of hour (0-59) when reminder should trigger
 * @property createdAt Timestamp when the plant entry was created
 */
@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    val name: String,
    
    val notes: String = "",
    
    val lastWateredDate: Date,
    
    val imagePath: String? = null,
    
    val reminderId: Int = 0,
    
    val reminderEnabled: Boolean = false,
    
    val reminderHour: Int = 9,  // Default to 9 AM
    
    val reminderMinute: Int = 0,  // Default to :00
    
    val createdAt: Date = Date()
)