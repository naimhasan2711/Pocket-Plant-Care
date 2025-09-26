package com.abdur.rahman.pocketplantcare.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.abdur.rahman.pocketplantcare.R
import com.abdur.rahman.pocketplantcare.data.entity.Plant
import com.abdur.rahman.pocketplantcare.presentation.viewmodel.PlantViewModel
import com.abdur.rahman.pocketplantcare.utils.DateFormatter
import com.abdur.rahman.pocketplantcare.utils.PhotoManager
import com.abdur.rahman.pocketplantcare.utils.ReminderManager
import java.text.DateFormat

/**
 * Plant Detail Screen - Detailed view and editing for individual plants.
 * 
 * This screen provides comprehensive plant management built with Jetpack Compose and Material 3.
 * Users can view/edit plant details, change photos, mark as watered, and manage reminders.
 * Demonstrates advanced Compose patterns including activity result launchers and state management.
 * 
 * Key features:
 * - Full plant information display and editing
 * - Photo management with camera and gallery integration
 * - Watering reminder toggle with scheduling
 * - Last watered date tracking and updates
 * - Material 3 design with responsive layouts
 * - Proper lifecycle-aware resource management
 * - Integration with PhotoManager and ReminderManager utilities
 * 
 * Architecture: Presentation layer component consuming PlantViewModel state and coordinating
 * with utility classes for photo and reminder operations.
 * 
 * @param plantId ID of the plant to display/edit
 * @param onNavigateBack Callback for navigating back to previous screen
 * @param viewModel PlantViewModel injected by Hilt
 * @param photoManager PhotoManager injected by Hilt for photo operations
 * @param reminderManager ReminderManager injected by Hilt for reminder scheduling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // State management for plant data and UI
    val selectedPlant by viewModel.selectedPlant.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Local state for editing
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedNotes by remember { mutableStateOf("") }
    var showPhotoDialog by remember { mutableStateOf(false) }
    
    // Initialize plant data when plantId changes
    LaunchedEffect(plantId) {
        viewModel.selectPlant(plantId)
    }
    
    // Update local editing state when plant data loads
    LaunchedEffect(selectedPlant) {
        selectedPlant?.let { plant ->
            editedName = plant.name
            editedNotes = plant.notes
        }
    }
    
    // For now, we'll handle photo operations in the ViewModel
    // In a more complex app, you might inject these utilities differently
    val photoManager = remember { PhotoManager(context) }
    val reminderManager = remember { ReminderManager(context) }
    
    // Photo capture launchers using ActivityResultContracts
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoManager.getCurrentPhotoUri()?.let { uri ->
                    val filePath = photoManager.copyImageToAppStorage(uri)
                    selectedPlant?.let { plant ->
                        viewModel.updatePlant(plant.copy(imagePath = filePath))
                    }
                }
            }
            photoManager.clearCurrentPhotoUri()
        }
    )
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val filePath = photoManager.copyImageToAppStorage(it)
                selectedPlant?.let { plant ->
                    viewModel.updatePlant(plant.copy(imagePath = filePath))
                }
            }
        }
    )
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, now launch camera
                val (intent, _) = photoManager.createCameraIntent()
                intent?.let { cameraIntent ->
                    photoManager.getCurrentPhotoUri()?.let { uri ->
                        cameraLauncher.launch(uri)
                    }
                }
            }
            // If permission denied, we could show a message or handle it gracefully
        }
    )

    // State for managing reminder activation
    var pendingReminderActivation by remember { mutableStateOf<Plant?>(null) }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            pendingReminderActivation?.let { plant ->
                // Proceed with reminder activation regardless of permission result
                // If permission denied, reminders just won't be visible
                val reminderId = reminderManager.scheduleWateringReminder(
                    plantId = plant.id,
                    plantName = plant.name,
                    hour = plant.reminderHour,
                    minute = plant.reminderMinute
                )
                viewModel.updateReminderSettings(plant.id, true, reminderId)
                pendingReminderActivation = null
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = selectedPlant?.name ?: "Plant Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        // Save and cancel buttons when editing
                        TextButton(
                            onClick = {
                                selectedPlant?.let { plant ->
                                    viewModel.updatePlant(
                                        plant.copy(
                                            name = editedName.trim(),
                                            notes = editedNotes.trim()
                                        )
                                    )
                                    isEditing = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    } else {
                        // Edit button when not editing
                        IconButton(
                            onClick = { isEditing = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit plant"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        selectedPlant?.let { plant ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Plant photo section
                PlantPhotoSection(
                    imagePath = plant.imagePath,
                    plantName = plant.name,
                    onPhotoClick = { showPhotoDialog = true }
                )
                
                // Plant information section
                if (isEditing) {
                    PlantEditingSection(
                        name = editedName,
                        notes = editedNotes,
                        onNameChange = { editedName = it },
                        onNotesChange = { editedNotes = it },
                        onCancel = {
                            isEditing = false
                            editedName = plant.name
                            editedNotes = plant.notes
                        }
                    )
                } else {
                    PlantInfoSection(plant = plant)
                }
                
                // Care actions section
                PlantCareSection(
                    plant = plant,
                    onWaterClick = { 
                        viewModel.markPlantAsWatered(plant.id)
                    },
                    onReminderToggle = { enabled ->
                        if (enabled) {
                            // Check notification permission on Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        // Permission already granted, proceed
                                        val reminderId = reminderManager.scheduleWateringReminder(
                                            plantId = plant.id,
                                            plantName = plant.name,
                                            hour = plant.reminderHour,
                                            minute = plant.reminderMinute
                                        )
                                        viewModel.updateReminderSettings(plant.id, true, reminderId)
                                    }
                                    else -> {
                                        // Store plant for later activation and request permission
                                        pendingReminderActivation = plant
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            } else {
                                // No permission needed on older Android versions
                                val reminderId = reminderManager.scheduleWateringReminder(
                                    plantId = plant.id,
                                    plantName = plant.name,
                                    hour = plant.reminderHour,
                                    minute = plant.reminderMinute
                                )
                                viewModel.updateReminderSettings(plant.id, true, reminderId)
                            }
                        } else {
                            reminderManager.cancelWateringReminder(plant.reminderId)
                            viewModel.updateReminderSettings(plant.id, false, 0)
                        }
                    }
                )
            }
        } ?: run {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Photo selection dialog
    if (showPhotoDialog) {
        PhotoSelectionDialog(
            onCameraClick = {
                showPhotoDialog = false
                // Request camera permission first, then launch camera in the callback
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                showPhotoDialog = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showPhotoDialog = false }
        )
    }
}

/**
 * Photo section showing plant image with tap-to-change functionality.
 * 
 * @param imagePath Path to plant photo file
 * @param plantName Plant name for accessibility
 * @param onPhotoClick Callback when photo is tapped
 */
@Composable
private fun PlantPhotoSection(
    imagePath: String?,
    plantName: String,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onPhotoClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imagePath != null) {
                // Using Coil for efficient image loading and caching
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Photo of $plantName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.plant),
                    error = painterResource(id = R.drawable.plant)
                )
            } else {
                // Placeholder when no photo
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Photo of $plantName",
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.plant),
                    error = painterResource(id = R.drawable.plant)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Add photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to add photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section displaying plant information in read-only mode.
 * 
 * @param plant Plant data to display
 */
@Composable
private fun PlantInfoSection(plant: Plant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Plant Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            InfoRow(
                label = "Name",
                value = plant.name
            )
            
            if (plant.notes.isNotEmpty()) {
                InfoRow(
                    label = "Notes",
                    value = plant.notes
                )
            }
            
            InfoRow(
                label = "Added",
                value = DateFormat.getDateInstance().format(plant.createdAt)
            )
        }
    }
}

/**
 * Section for editing plant information.
 * 
 * @param name Current name being edited
 * @param notes Current notes being edited
 * @param onNameChange Callback when name changes
 * @param onNotesChange Callback when notes change
 * @param onCancel Callback when editing is cancelled
 */
@Composable
private fun PlantEditingSection(
    name: String,
    notes: String,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edit Plant Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Section for plant care actions (watering, reminders).
 * 
 * @param plant Plant data
 * @param onWaterClick Callback when water button is clicked
 * @param onReminderToggle Callback when reminder toggle changes
 */
@Composable
private fun PlantCareSection(
    plant: Plant,
    onWaterClick: () -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Plant Care",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Last watered info and water button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last Watered",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateFormatter.formatFullDateTime(plant.lastWateredDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Show reminder time if enabled
                    if (plant.reminderEnabled) {
                        Text(
                            text = "⏰ ${DateFormatter.formatTime(plant.reminderHour, plant.reminderMinute)} daily",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Button(
                    onClick = onWaterClick,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Water Now")
                }
            }
            
            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Reminders",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (plant.reminderEnabled) "9:00 AM daily" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = plant.reminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }
        }
    }
}

/**
 * Dialog for selecting photo source (camera or gallery).
 * 
 * @param onCameraClick Callback when camera option is selected
 * @param onGalleryClick Callback when gallery option is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
private fun PhotoSelectionDialog(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Photo Source") },
        text = { Text("Choose how you'd like to add a photo for your plant.") },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCameraClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
                TextButton(onClick = onGalleryClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Utility composable for displaying label-value pairs.
 * 
 * @param label Field label
 * @param value Field value
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}