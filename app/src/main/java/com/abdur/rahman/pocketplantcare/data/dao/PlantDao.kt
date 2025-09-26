package com.abdur.rahman.pocketplantcare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abdur.rahman.pocketplantcare.data.entity.Plant
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Plant entity operations.
 * 
 * Provides reactive database operations using Flow for automatic UI updates
 * following Android's recommended Room patterns. All operations are designed
 * to work with Kotlin coroutines for asynchronous database access.
 * 
 * Key features:
 * - Reactive queries using Flow for automatic UI updates
 * - Suspend functions for non-blocking database operations
 * - Comprehensive CRUD operations following repository pattern
 * - Optimized queries for common plant management operations
 */
@Dao
interface PlantDao {
    
    /**
     * Get all plants ordered by creation date (newest first).
     * Returns Flow for reactive UI updates when data changes.
     */
    @Query("SELECT * FROM plants ORDER BY createdAt DESC")
    fun getAllPlants(): Flow<List<Plant>>
    
    /**
     * Get a specific plant by its ID.
     * Returns Flow to observe changes to the specific plant.
     */
    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantById(id: Long): Flow<Plant?>
    
    /**
     * Get plants that have reminders enabled.
     * Useful for reminder management operations.
     */
    @Query("SELECT * FROM plants WHERE reminderEnabled = 1")
    fun getPlantsWithReminders(): Flow<List<Plant>>
    
    /**
     * Insert a new plant into the database.
     * Returns the row ID of the inserted plant.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long
    
    /**
     * Update an existing plant's information.
     * Returns the number of rows updated (should be 1 for success).
     */
    @Update
    suspend fun updatePlant(plant: Plant): Int
    
    /**
     * Delete a plant from the database.
     * Returns the number of rows deleted (should be 1 for success).
     */
    @Delete
    suspend fun deletePlant(plant: Plant): Int
    
    /**
     * Delete a plant by its ID.
     * More convenient when you only have the plant ID.
     */
    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlantById(id: Long): Int
    
    /**
     * Update the last watered date for a specific plant.
     * Convenience method for the common watering operation.
     */
    @Query("UPDATE plants SET lastWateredDate = :date WHERE id = :id")
    suspend fun updateLastWateredDate(id: Long, date: java.util.Date): Int
    
    /**
     * Update reminder settings for a specific plant.
     * Used when enabling/disabling reminders.
     */
    @Query("UPDATE plants SET reminderEnabled = :enabled, reminderId = :reminderId WHERE id = :id")
    suspend fun updateReminderSettings(id: Long, enabled: Boolean, reminderId: Int): Int
}