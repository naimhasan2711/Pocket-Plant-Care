package com.abdur.rahman.pocketplantcare.di

import android.content.Context
import com.abdur.rahman.pocketplantcare.utils.PhotoManager
import com.abdur.rahman.pocketplantcare.utils.ReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for utility class dependency injection.
 * 
 * This module provides utility classes that are needed by repositories and other components.
 * Uses @InstallIn(SingletonComponent::class) to make these dependencies available
 * application-wide as singletons for consistent behavior.
 * 
 * Key provisions:
 * - PhotoManager for camera and gallery operations
 * - ReminderManager for alarm and notification scheduling
 * - Application-wide singleton instances
 * 
 * Architecture: Part of the dependency injection setup that provides utility services
 * to both data layer (repositories) and presentation layer components.
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {
    
    /**
     * Provides PhotoManager singleton instance.
     * 
     * Creates PhotoManager with application context for file operations.
     * The @Singleton annotation ensures the same instance is used
     * throughout the application lifecycle.
     * 
     * @param context Application context provided by Hilt
     * @return PhotoManager instance for photo operations
     */
    @Provides
    @Singleton
    fun providePhotoManager(@ApplicationContext context: Context): PhotoManager {
        return PhotoManager(context)
    }
    
    /**
     * Provides ReminderManager singleton instance.
     * 
     * Creates ReminderManager with application context for alarm and notification operations.
     * The @Singleton annotation ensures consistent behavior across the application.
     * 
     * @param context Application context provided by Hilt
     * @return ReminderManager instance for reminder operations
     */
    @Provides
    @Singleton
    fun provideReminderManager(@ApplicationContext context: Context): ReminderManager {
        return ReminderManager(context)
    }
}