package com.abdur.rahman.pocketplantcare.data.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room type converters for custom data types.
 * 
 * Room doesn't know how to store Date objects directly, so we need to provide
 * conversion methods between Date and Long (timestamp). These converters are
 * automatically used by Room when storing/retrieving Date fields.
 * 
 * Implementation follows Android's recommended patterns for Room type converters
 * using Unix timestamps for reliable date storage and retrieval.
 */
class DateConverter {
    
    /**
     * Convert Date object to Long timestamp for database storage.
     * 
     * @param date Date object to convert, can be null
     * @return Long timestamp or null if date was null
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convert Long timestamp from database to Date object.
     * 
     * @param timestamp Long timestamp from database, can be null
     * @return Date object or null if timestamp was null
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}