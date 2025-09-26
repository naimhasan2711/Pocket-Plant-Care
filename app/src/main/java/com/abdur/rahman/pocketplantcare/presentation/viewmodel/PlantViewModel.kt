package com.abdur.rahman.pocketplantcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.pocketplantcare.data.entity.Plant
import com.abdur.rahman.pocketplantcare.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for managing plant-related UI state and business logic.
 * 
 * Implements the MVVM architecture pattern by serving as the intermediary between
 * the UI (Compose screens) and the data layer (Repository). Manages UI state using
 * StateFlow for reactive UI updates and coordinates all plant-related operations.
 * 
 * Key responsibilities:
 * - Manage UI state for plant list and detail screens
 * - Coordinate plant CRUD operations through repository
 * - Handle business logic for plant care operations
 * - Manage loading states and error handling
 * - Provide reactive data streams for UI consumption
 * 
 * Uses Hilt for dependency injection and follows reactive programming principles
 * with Kotlin Flow and StateFlow for automatic UI updates.
 * 
 * @property repository PlantRepository for data operations
 */
@HiltViewModel
class PlantViewModel @Inject constructor(
    private val repository: PlantRepository
) : ViewModel() {
    
    /**
     * StateFlow for UI loading state management.
     * Private mutable state with public read-only exposure.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * StateFlow for error message management.
     * Null when no error, contains error message when error occurs.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * StateFlow for currently selected plant details.
     * Used by plant detail screen for showing/editing plant information.
     */
    private val _selectedPlant = MutableStateFlow<Plant?>(null)
    val selectedPlant: StateFlow<Plant?> = _selectedPlant.asStateFlow()
    
    /**
     * Flow of all plants from repository.
     * Automatically updates UI when plant data changes in database.
     */
    val allPlants: StateFlow<List<Plant>> = repository.getAllPlants()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    
    /**
     * Flow of plants with active reminders.
     * Used for reminder management and notification scheduling.
     */
    val plantsWithReminders: StateFlow<List<Plant>> = repository.getPlantsWithReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    
    /**
     * Add a new plant to the database.
     * 
     * Handles loading state and error management during the operation.
     * 
     * @param name Plant name (required)
     * @param notes Optional care notes
     * @param imagePath Optional path to plant photo
     * @param reminderEnabled Whether to enable watering reminders
     */
    fun addPlant(
        name: String,
        notes: String = "",
        imagePath: String? = null,
        reminderEnabled: Boolean = false,
        reminderHour: Int = 9,
        reminderMinute: Int = 0
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Plant name cannot be empty"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val plant = Plant(
                    name = name.trim(),
                    notes = notes.trim(),
                    lastWateredDate = Date(),
                    imagePath = imagePath,
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute
                )
                
                repository.addPlant(plant)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add plant: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update an existing plant's information.
     * 
     * @param plant Updated plant object
     */
    fun updatePlant(plant: Plant) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                repository.updatePlant(plant)
                
                // Update selected plant if it's the same
                if (_selectedPlant.value?.id == plant.id) {
                    _selectedPlant.value = plant
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update plant: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete a plant from the database.
     * 
     * @param plant Plant to delete
     */
    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                repository.deletePlant(plant)
                
                // Clear selected plant if it was deleted
                if (_selectedPlant.value?.id == plant.id) {
                    _selectedPlant.value = null
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete plant: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Mark a plant as watered by updating the last watered date.
     * 
     * @param plantId ID of the plant to mark as watered
     */
    fun markPlantAsWatered(plantId: Long) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.updateLastWateredDate(plantId, Date())
                
                // Update selected plant if it matches
                _selectedPlant.value?.let { plant ->
                    if (plant.id == plantId) {
                        _selectedPlant.value = plant.copy(lastWateredDate = Date())
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update watering date: ${e.localizedMessage}"
            }
        }
    }
    
    /**
     * Select a plant for detail view.
     * 
     * Loads the plant data and sets it as the selected plant for the detail screen.
     * 
     * @param plantId ID of the plant to select
     */
    fun selectPlant(plantId: Long) {
        viewModelScope.launch {
            repository.getPlantById(plantId).collect { plant ->
                _selectedPlant.value = plant
            }
        }
    }
    
    /**
     * Clear the currently selected plant.
     * 
     * Used when navigating away from plant detail screen.
     */
    fun clearSelectedPlant() {
        _selectedPlant.value = null
    }
    
    /**
     * Update reminder settings for a plant.
     * 
     * @param plantId Plant ID to update
     * @param enabled Whether reminders should be active
     * @param reminderId Alarm manager request ID
     */
    fun updateReminderSettings(plantId: Long, enabled: Boolean, reminderId: Int = 0) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.updateReminderSettings(plantId, enabled, reminderId)
                
                // Update selected plant if it matches
                _selectedPlant.value?.let { plant ->
                    if (plant.id == plantId) {
                        _selectedPlant.value = plant.copy(
                            reminderEnabled = enabled,
                            reminderId = reminderId
                        )
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update reminder settings: ${e.localizedMessage}"
            }
        }
    }
    
    /**
     * Clear error message.
     * 
     * Should be called after error message is displayed to user.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}