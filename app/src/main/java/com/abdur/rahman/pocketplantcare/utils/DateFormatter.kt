package com.abdur.rahman.pocketplantcare.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility object for formatting dates and times in the Pocket Plant Care application.
 * 
 * Provides consistent date and time formatting across the app, following
 * dd/mm/yyyy hh:mm:ss format for timestamps and HH:mm format for reminder times.
 * Centralizes date formatting logic to ensure consistency and easy maintenance.
 */
object DateFormatter {
    
    /**
     * Date format pattern for displaying complete timestamps.
     * Format: dd/mm/yyyy hh:mm:ss (e.g., "26/09/2025 14:30:45")
     */
    private const val FULL_DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss"
    
    /**
     * Time format pattern for displaying reminder times.
     * Format: HH:mm (e.g., "09:30")
     */
    private const val TIME_PATTERN = "HH:mm"
    
    /**
     * Date format pattern for displaying dates only.
     * Format: dd/mm/yyyy (e.g., "26/09/2025")
     */
    private const val DATE_PATTERN = "dd/MM/yyyy"
    
    /**
     * SimpleDateFormat for full date-time formatting.
     * Uses default locale for proper localization.
     */
    private val fullDateTimeFormatter = SimpleDateFormat(FULL_DATE_TIME_PATTERN, Locale.getDefault())
    
    /**
     * SimpleDateFormat for time-only formatting.
     * Uses default locale for proper localization.
     */
    private val timeFormatter = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
    
    /**
     * SimpleDateFormat for date-only formatting.
     * Uses default locale for proper localization.
     */
    private val dateFormatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    
    /**
     * Format a Date object to a complete date-time string.
     * 
     * @param date Date object to format
     * @return Formatted string in dd/mm/yyyy hh:mm:ss format
     */
    fun formatFullDateTime(date: Date): String {
        return fullDateTimeFormatter.format(date)
    }
    
    /**
     * Format hour and minute to a time string.
     * 
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @return Formatted string in HH:mm format
     */
    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return timeFormatter.format(calendar.time)
    }
    
    /**
     * Format a Date object to a date-only string.
     * 
     * @param date Date object to format
     * @return Formatted string in dd/mm/yyyy format
     */
    fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }
    
    /**
     * Get a user-friendly relative time description.
     * 
     * @param date Date to compare with current time
     * @return String describing relative time (e.g., "2 days ago", "Today", "Yesterday")
     */
    fun getRelativeTimeDescription(date: Date): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        
        val diffInMillis = now.timeInMillis - target.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        
        return when {
            diffInDays == 0 -> "Today"
            diffInDays == 1 -> "Yesterday"
            diffInDays < 7 -> "$diffInDays days ago"
            diffInDays < 30 -> "${diffInDays / 7} weeks ago"
            diffInDays < 365 -> "${diffInDays / 30} months ago"
            else -> "${diffInDays / 365} years ago"
        }
    }
}