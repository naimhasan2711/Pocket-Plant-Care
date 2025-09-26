package com.abdur.rahman.pocketplantcare.data.repository

import com.abdur.rahman.pocketplantcare.data.dao.PlantDao
import com.abdur.rahman.pocketplantcare.data.entity.Plant
import com.abdur.rahman.pocketplantcare.utils.ReminderManager
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for Plant data management.
 * 
 * Implements the Repository pattern as part of the MVVM architecture, serving as a single
 * source of truth for plant data. Abstracts the data layer from ViewModels and handles
 * data operations coordination between local database and potential future remote sources.
 * 
 * Key responsibilities:
 * - Provide clean API for data operations to ViewModels
 * - Coordinate between different data sources (currently only local database)
 * - Handle data transformation and business logic
 * - Maintain data consistency and integrity
 * 
 * This class uses Hilt for dependency injection and follows reactive programming
 * principles with Flow for automatic UI updates.
 * 
 * @property plantDao Data access object for plant database operations
 */
@Singleton
class PlantRepository @Inject constructor(
    private val plantDao: PlantDao,
    private val reminderManager: ReminderManager
) {
    
    /**
     * Get all plants as a reactive Flow.
     * 
     * Returns Flow for automatic UI updates when plant data changes.
     * Plants are ordered by creation date with newest first.
     * 
     * @return Flow<List<Plant>> Reactive stream of all plants
     */
    fun getAllPlants(): Flow<List<Plant>> = plantDao.getAllPlants()
    
    /**
     * Get a specific plant by ID as a reactive Flow.
     * 
     * @param id Plant ID to retrieve
     * @return Flow<Plant?> Reactive stream of the plant (null if not found)
     */
    fun getPlantById(id: Long): Flow<Plant?> = plantDao.getPlantById(id)
    
    /**
     * Get plants that have reminders enabled.
     * 
     * Useful for reminder management operations and notification scheduling.
     * 
     * @return Flow<List<Plant>> Reactive stream of plants with active reminders
     */
    fun getPlantsWithReminders(): Flow<List<Plant>> = plantDao.getPlantsWithReminders()
    
    /**
     * Add a new plant to the database.
     * 
     * If reminders are enabled, schedules a watering reminder at the specified time.
     * 
     * @param plant Plant object to insert
     * @return Long The row ID of the newly inserted plant
     */
    suspend fun addPlant(plant: Plant): Long {
        val plantId = plantDao.insertPlant(plant)
        
        // Schedule reminder if enabled
        if (plant.reminderEnabled) {
            val reminderId = reminderManager.scheduleWateringReminder(
                plantId = plantId,
                plantName = plant.name,
                hour = plant.reminderHour,
                minute = plant.reminderMinute
            )
            
            // Update plant with reminder ID
            plantDao.updateReminderSettings(plantId, true, reminderId)
        }
        
        return plantId
    }
    
    /**
     * Update an existing plant's information.
     * 
     * @param plant Updated plant object
     * @return Int Number of rows updated (should be 1 for success)
     */
    suspend fun updatePlant(plant: Plant): Int {
        return plantDao.updatePlant(plant)
    }
    
    /**
     * Delete a plant from the database.
     * 
     * @param plant Plant object to delete
     * @return Int Number of rows deleted (should be 1 for success)
     */
    suspend fun deletePlant(plant: Plant): Int {
        return plantDao.deletePlant(plant)
    }
    
    /**
     * Delete a plant by its ID.
     * 
     * Convenience method when only the plant ID is available.
     * 
     * @param id Plant ID to delete
     * @return Int Number of rows deleted (should be 1 for success)
     */
    suspend fun deletePlantById(id: Long): Int {
        return plantDao.deletePlantById(id)
    }
    
    /**
     * Update the last watered date for a plant.
     * 
     * Common operation when user marks plant as watered.
     * 
     * @param id Plant ID to update
     * @param date New last watered date
     * @return Int Number of rows updated (should be 1 for success)
     */
    suspend fun updateLastWateredDate(id: Long, date: Date = Date()): Int {
        return plantDao.updateLastWateredDate(id, date)
    }
    
    /**
     * Update reminder settings for a plant.
     * 
     * Used when enabling/disabling reminders or changing reminder configuration.
     * 
     * @param id Plant ID to update
     * @param enabled Whether reminders should be active
     * @param reminderId Alarm manager request ID for the reminder
     * @return Int Number of rows updated (should be 1 for success)
     */
    suspend fun updateReminderSettings(id: Long, enabled: Boolean, reminderId: Int = 0): Int {
        return plantDao.updateReminderSettings(id, enabled, reminderId)
    }
}