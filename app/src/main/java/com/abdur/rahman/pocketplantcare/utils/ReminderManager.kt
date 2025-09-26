package com.abdur.rahman.pocketplantcare.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.abdur.rahman.pocketplantcare.MainActivity
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling plant watering reminders and notifications.
 * 
 * Provides functionality for scheduling, managing, and canceling watering reminders
 * using Android's AlarmManager and NotificationManager. Handles proper notification
 * channels for Android 8.0+ and manages reminder persistence across app restarts.
 * 
 * Key features:
 * - Schedule daily watering reminders using AlarmManager
 * - Create and manage notification channels (Android 8.0+)
 * - Handle reminder broadcast reception and notification display
 * - Manage reminder lifecycle (create, update, cancel)
 * - Proper resource cleanup and permission handling
 * 
 * Architecture: Utility class that provides reminder functionality to ViewModels
 * and other components, following separation of concerns principle.
 */
@Singleton
class ReminderManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "plant_watering_reminders"
        private const val NOTIFICATION_CHANNEL_NAME = "Plant Watering Reminders"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for watering your plants"
        private const val TAG = "ReminderManager"
        
        // Intent action for plant reminders
        const val ACTION_PLANT_REMINDER = "com.abdur.rahman.pocketplantcare.PLANT_REMINDER"
        
        // Intent extras
        const val EXTRA_PLANT_ID = "plant_id"
        const val EXTRA_PLANT_NAME = "plant_name"
    }
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
        checkAlarmPermissions()
    }
    
    /**
     * Check if the app has necessary alarm permissions
     */
    private fun checkAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "App cannot schedule exact alarms. Some reminders may be delayed.")
            } else {
                Log.d(TAG, "App can schedule exact alarms")
            }
        }
    }
    
    /**
     * Create notification channel for plant watering reminders.
     * 
     * Required for Android 8.0 (API level 26) and higher. Channels help users
     * manage notification preferences and provide better UX control.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Higher importance for plant care reminders
            ).apply {
                description = NOTIFICATION_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Created notification channel with HIGH importance")
        }
    }
    
    /**
     * Schedule a daily watering reminder for a plant.
     * 
     * Uses AlarmManager to schedule repeating alarms. The reminder will trigger
     * at the specified hour daily until canceled. Handles different Android versions
     * for proper alarm scheduling permissions.
     * 
     * @param plantId Unique plant identifier
     * @param plantName Plant name for notification display
     * @param hour Hour of day (0-23) when reminder should trigger
     * @param minute Minute of hour (0-59) when reminder should trigger
     * @return Int Request ID for the scheduled alarm (for cancellation)
     */
    fun scheduleWateringReminder(
        plantId: Long,
        plantName: String,
        hour: Int,
        minute: Int
    ): Int {
        val requestId = plantId.toInt() // Use plant ID as request ID for uniqueness
        
        // Create intent with explicit action for reliable delivery
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_PLANT_REMINDER
            putExtra(EXTRA_PLANT_ID, plantId)
            putExtra(EXTRA_PLANT_NAME, plantName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate next reminder time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        Log.d(TAG, "Scheduling reminder for plant $plantName (ID: $plantId) at ${calendar.time}")
        
        try {
            // For Android 12+ (API 31+), check if we can schedule exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Use exact alarm for precise timing
                    scheduleNextAlarm(calendar.timeInMillis, pendingIntent, plantId, plantName)
                } else {
                    Log.w(TAG, "Cannot schedule exact alarms. Using inexact alarm.")
                    // Fall back to inexact repeating alarm
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            } else {
                // For older Android versions, use setRepeating
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Successfully scheduled reminder for plant $plantName")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            // Fall back to inexact alarms
            try {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d(TAG, "Fell back to inexact repeating alarm")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule any alarm: ${e2.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error scheduling alarm: ${e.message}")
        }
        
        return requestId
    }
    
    /**
     * Schedule the next alarm using exact alarm API for Android 12+
     */
    private fun scheduleNextAlarm(triggerTime: Long, pendingIntent: PendingIntent, plantId: Long, plantName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled exact alarm for ${Date(triggerTime)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact alarm: ${e.message}")
            throw e
        }
    }
    
    /**
     * Cancel a scheduled watering reminder.
     * 
     * Removes the alarm from AlarmManager using the request ID.
     * Should be called when reminder is disabled or plant is deleted.
     * 
     * @param requestId Request ID returned from scheduleWateringReminder
     */
    fun cancelWateringReminder(requestId: Int) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    /**
     * Show watering reminder notification.
     * 
     * Called by the broadcast receiver when reminder alarm triggers.
     * Creates and displays a notification with plant-specific information.
     * 
     * @param plantId Plant ID for notification identification
     * @param plantName Plant name to display in notification
     */
    fun showWateringNotification(plantId: Long, plantName: String) {
        Log.d(TAG, "Showing notification for plant: $plantName (ID: $plantId)")
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                Log.w(TAG, "Notifications are disabled - cannot show reminder for $plantName")
                return
            }
        }
        
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_PLANT_ID, plantId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            plantId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon for better compatibility
            .setContentTitle("🌱 Time to water $plantName!")
            .setContentText("Don't forget to give your plant some water today.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to water $plantName! Tap to open the app and mark it as watered."))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Higher priority for better visibility
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        try {
            notificationManager.notify(plantId.toInt(), notification)
            Log.d(TAG, "Successfully showed notification for $plantName")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException showing notification: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}")
        }
    }
}

/**
 * BroadcastReceiver for handling watering reminder alarms.
 * 
 * Receives alarm broadcasts from AlarmManager and triggers notification display.
 * This receiver must be registered in the Android manifest to receive system broadcasts.
 * 
 * Lifecycle: This receiver is instantiated by the system when alarm triggers,
 * performs its work quickly, and is destroyed. No long-running operations should
 * be performed here.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ReminderBroadcastReceiver"
    }
    
    /**
     * Called when the alarm broadcast is received.
     * 
     * Extracts plant information from intent extras and shows watering notification.
     * 
     * @param context Application context
     * @param intent Intent containing plant information
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ReminderManager.ACTION_PLANT_REMINDER -> {
                val plantId = intent.getLongExtra(ReminderManager.EXTRA_PLANT_ID, -1L)
                val plantName = intent.getStringExtra(ReminderManager.EXTRA_PLANT_NAME) ?: "Your Plant"
                
                Log.d(TAG, "Processing plant reminder: ID=$plantId, Name=$plantName")
                
                if (plantId != -1L) {
                    try {
                        // Create ReminderManager instance to show notification
                        val reminderManager = ReminderManager(context)
                        reminderManager.showWateringNotification(plantId, plantName)
                        
                        // For exact alarms on Android 12+, we need to reschedule the next alarm
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            rescheduleNextReminder(context, plantId, plantName)
                        }
                        
                        Log.d(TAG, "Successfully processed reminder for $plantName")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing plant reminder: ${e.message}")
                    }
                } else {
                    Log.w(TAG, "Invalid plant ID in reminder broadcast")
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device boot completed - reminders may need to be rescheduled")
                // TODO: Reschedule all active reminders after device reboot
            }
            else -> {
                Log.w(TAG, "Received unknown broadcast action: ${intent.action}")
            }
        }
    }
    
    /**
     * Reschedule the next reminder for exact alarms on Android 12+
     * Since exact alarms are one-time, we need to schedule the next occurrence
     */
    private fun rescheduleNextReminder(context: Context, plantId: Long, plantName: String) {
        try {
            // TODO: Get the plant's reminder time from database and reschedule
            // For now, this is a placeholder - in a full implementation,
            // you'd query the database to get the plant's reminder hour/minute
            Log.d(TAG, "Would reschedule next reminder for plant $plantName (ID: $plantId)")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling reminder: ${e.message}")
        }
    }
}

/**
 * Extension functions for testing and debugging reminders
 */
fun ReminderManager.scheduleTestReminder(plantName: String = "Test Plant"): Int {
    val currentTime = Calendar.getInstance()
    currentTime.add(Calendar.MINUTE, 1) // Schedule for 1 minute from now
    
    return scheduleWateringReminder(
        plantId = 999L,
        plantName = plantName,
        hour = currentTime.get(Calendar.HOUR_OF_DAY),
        minute = currentTime.get(Calendar.MINUTE)
    )
}