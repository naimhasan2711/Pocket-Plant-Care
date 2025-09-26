package com.abdur.rahman.pocketplantcare.di

import android.content.Context
import androidx.room.Room
import com.abdur.rahman.pocketplantcare.data.dao.PlantDao
import com.abdur.rahman.pocketplantcare.data.database.PlantDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependency injection.
 * 
 * This module provides database-related dependencies throughout the application.
 * Uses @InstallIn(SingletonComponent::class) to make dependencies available
 * application-wide as singletons.
 * 
 * Key provisions:
 * - PlantDatabase singleton instance
 * - PlantDao from the database
 * - Proper lifecycle management for database resources
 * 
 * Architecture: Part of the dependency injection setup that enables clean separation
 * of concerns in the MVVM architecture by providing data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides singleton PlantDatabase instance.
     * 
     * Creates and configures the Room database with proper application context.
     * The @Singleton annotation ensures only one database instance exists throughout
     * the application lifecycle.
     * 
     * @param context Application context provided by Hilt
     * @return PlantDatabase singleton instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlantDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PlantDatabase::class.java,
            "plant_database"
        ).build()
    }
    
    /**
     * Provides PlantDao from the database.
     * 
     * Extracts the DAO from the database instance for repository injection.
     * Since database is singleton, this DAO will also be effectively singleton.
     * 
     * @param database PlantDatabase instance provided by Hilt
     * @return PlantDao for plant data operations
     */
    @Provides
    fun providePlantDao(database: PlantDatabase): PlantDao {
        return database.plantDao()
    }
}