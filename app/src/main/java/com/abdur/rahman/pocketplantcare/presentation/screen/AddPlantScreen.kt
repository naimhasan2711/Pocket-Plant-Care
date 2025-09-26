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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import com.abdur.rahman.pocketplantcare.presentation.viewmodel.PlantViewModel
import com.abdur.rahman.pocketplantcare.utils.DateFormatter
import com.abdur.rahman.pocketplantcare.utils.PhotoManager

/**
 * Add Plant Screen - Form for creating new plant entries.
 * 
 * This screen provides a comprehensive form for adding new plants to the user's collection.
 * Built with Jetpack Compose and Material 3 design, it includes photo capture functionality,
 * form validation, and proper state management. Demonstrates modern Android development patterns
 * including activity result launchers and reactive UI updates.
 * 
 * Key features:
 * - Clean form interface for plant information entry
 * - Photo capture integration with camera and gallery
 * - Real-time form validation and error display
 * - Material 3 design components and theming
 * - Proper lifecycle-aware resource management
 * - Integration with PhotoManager for image operations
 * - Reminder scheduling option for new plants
 * 
 * Architecture: Presentation layer component that coordinates with PlantViewModel for data
 * persistence and PhotoManager for image operations, following MVVM pattern.
 * 
 * @param onNavigateBack Callback for navigating back to previous screen
 * @param viewModel PlantViewModel injected by Hilt for plant operations
 * @param photoManager PhotoManager injected by Hilt for photo operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Form state management
    var plantName by remember { mutableStateOf("") }
    var plantNotes by remember { mutableStateOf("") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var enableReminders by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Form validation state
    var nameError by remember { mutableStateOf<String?>(null) }
    
    // ViewModel state
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Photo manager for handling camera and gallery operations
    val photoManager = remember { PhotoManager(context) }
    
    // Track if plant was added successfully
    var plantWasAdded by remember { mutableStateOf(false) }
    
    // Handle successful plant addition
    LaunchedEffect(plantWasAdded) {
        if (plantWasAdded) {
            onNavigateBack()
        }
    }
    
    // Photo capture launchers using ActivityResultContracts
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoManager.getCurrentPhotoUri()?.let { uri ->
                    val filePath = photoManager.copyImageToAppStorage(uri)
                    selectedImagePath = filePath
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
                selectedImagePath = filePath
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

    /**
     * Proceeds with adding the plant after permission checks
     */
    fun proceedWithAddingPlant() {
        viewModel.addPlant(
            name = plantName.trim(),
            notes = plantNotes.trim(),
            imagePath = selectedImagePath,
            reminderEnabled = enableReminders,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
        // For simplicity, set flag to navigate back
        // In a production app, you'd wait for success from ViewModel
        plantWasAdded = true
    }
    
    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with adding plant
                proceedWithAddingPlant()
            } else {
                // Permission denied, but still allow plant creation without reminders
                // Show a message that reminders won't work without notification permission
                // For now, proceed anyway
                proceedWithAddingPlant()
            }
        }
    )
    
    /**
     * Validates form input and shows appropriate errors.
     * 
     * @return Boolean True if form is valid
     */
    fun validateForm(): Boolean {
        nameError = null
        
        if (plantName.isBlank()) {
            nameError = "Plant name is required"
            return false
        }
        
        if (plantName.length > 50) {
            nameError = "Plant name must be less than 50 characters"
            return false
        }
        
        return true
    }

    /**
     * Handles form submission with validation and permission checks.
     */
    fun submitForm() {
        if (validateForm()) {
            // If reminders are enabled and we're on Android 13+, check notification permission
            if (enableReminders && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )) {
                    PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted
                        proceedWithAddingPlant()
                    }
                    else -> {
                        // Request notification permission
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // No permission needed or reminders disabled
                proceedWithAddingPlant()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Add New Plant",
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
                    TextButton(
                        onClick = { submitForm() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Photo section
            PhotoSection(
                imagePath = selectedImagePath,
                onPhotoClick = { showPhotoDialog = true },
                onRemovePhoto = { 
                    selectedImagePath?.let { photoManager.deleteImageFile(it) }
                    selectedImagePath = null 
                }
            )
            
            // Plant information form
            PlantInfoForm(
                name = plantName,
                notes = plantNotes,
                nameError = nameError,
                onNameChange = { 
                    plantName = it
                    nameError = null // Clear error when user types
                },
                onNotesChange = { plantNotes = it }
            )
            
            // Reminder settings
            ReminderSection(
                enabled = enableReminders,
                onToggle = { enableReminders = it },
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                onTimeClick = { showTimePicker = true }
            )
            
            // Error display
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Add bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
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
    
    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onTimeSelected = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

/**
 * Photo section showing selected image with add/change functionality.
 * 
 * @param imagePath Path to selected image file, null if no image
 * @param onPhotoClick Callback when photo area is clicked
 * @param onRemovePhoto Callback when remove photo button is clicked
 */
@Composable
private fun PhotoSection(
    imagePath: String?,
    onPhotoClick: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onPhotoClick
    ) {
        Box {
            if (imagePath != null) {
                // Using Coil for efficient image loading and caching
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected plant photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Remove photo button
                IconButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Placeholder when no photo selected
                Column(
                    modifier = Modifier.fillMaxSize(),
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
                    Text(
                        text = "(optional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Form section for plant information input.
 * 
 * @param name Current plant name
 * @param notes Current plant notes
 * @param nameError Error message for name field, null if no error
 * @param onNameChange Callback when name changes
 * @param onNotesChange Callback when notes change
 */
@Composable
private fun PlantInfoForm(
    name: String,
    notes: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
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
                text = "Plant Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Plant Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    nameError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nature),
                        contentDescription = null
                    )
                }
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Care Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { 
                    Text("Add care instructions, watering schedule, or other notes...")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_description),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

/**
 * Section for configuring reminder settings.
 * 
 * @param enabled Whether reminders are enabled
 * @param onToggle Callback when reminder toggle changes
 * @param reminderHour Hour for reminder (0-23)
 * @param reminderMinute Minute for reminder (0-59)
 * @param onTimeClick Callback when time selection button is clicked
 */
@Composable
private fun ReminderSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    reminderHour: Int,
    reminderMinute: Int,
    onTimeClick: () -> Unit
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
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Watering Reminders",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (enabled) "${DateFormatter.formatTime(reminderHour, reminderMinute)} daily" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }
            
            if (enabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTimeClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_schedule),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Reminder Time: ${DateFormatter.formatTime(reminderHour, reminderMinute)}")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "You'll receive daily notifications to water your plant",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        title = { Text("Add Plant Photo") },
        text = { 
            Text("Choose how you'd like to add a photo for your plant. Photos help you track your plant's growth over time!")
        },
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
 * Dialog for selecting reminder time.
 * 
 * @param initialHour Initial hour value (0-23)
 * @param initialMinute Initial minute value (0-59)
 * @param onTimeSelected Callback when time is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Reminder Time",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
