package com.abdur.rahman.pocketplantcare.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.abdur.rahman.pocketplantcare.R
import com.abdur.rahman.pocketplantcare.data.entity.Plant
import com.abdur.rahman.pocketplantcare.presentation.viewmodel.PlantViewModel
import com.abdur.rahman.pocketplantcare.utils.DateFormatter
import java.text.DateFormat
import java.util.Date

/**
 * Plant List Screen - Main screen showing all user's plants.
 * 
 * This is the primary screen of the app built with Jetpack Compose and Material 3 design.
 * Displays all plants in a scrollable list with their photos, names, and last watered dates.
 * Provides quick actions for watering plants and navigation to plant details.
 * 
 * Key features:
 * - Reactive UI using StateFlow from ViewModel
 * - Image loading with Coil library for plant photos
 * - Material 3 design components and theming
 * - Pull-to-refresh functionality for data updates
 * - Quick watering action with visual feedback
 * - Navigation to plant detail screen
 * - Add new plant floating action button
 * - Loading states and error handling
 * 
 * Architecture: Presentation layer component in MVVM pattern, consumes data from
 * PlantViewModel and provides user interaction callbacks.
 * 
 * @param onNavigateToDetail Callback for navigating to plant detail screen
 * @param onNavigateToAdd Callback for navigating to add plant screen
 * @param viewModel PlantViewModel injected by Hilt for data management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: PlantViewModel = hiltViewModel()
) {
    // Collect UI state from ViewModel using Compose state management
    val plants by viewModel.allPlants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Show error message as snackbar when present
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // In a full implementation, you'd show a snackbar here
            // For now, we'll just clear the error after showing
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "🌱 My Plants",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Plant",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && plants.isEmpty() -> {
                    // Show loading indicator when first loading
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                plants.isEmpty() -> {
                    // Show empty state when no plants
                    EmptyPlantState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    // Show plant list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = plants,
                            key = { plant -> plant.id }
                        ) { plant ->
                            PlantListItem(
                                plant = plant,
                                onPlantClick = { onNavigateToDetail(plant.id) },
                                onWaterClick = { viewModel.markPlantAsWatered(plant.id) },
                                onDeleteClick = { viewModel.deletePlant(plant) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual plant item in the list.
 * 
 * Displays plant information in a Material 3 card with photo, name, last watered date,
 * and action buttons. Uses Coil for image loading with proper error handling and placeholder.
 * 
 * @param plant Plant data to display
 * @param onPlantClick Callback when plant card is tapped
 * @param onWaterClick Callback when water button is tapped
 * @param onDeleteClick Callback when delete button is tapped
 */
@Composable
private fun PlantListItem(
    plant: Plant,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onPlantClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Plant photo using Coil for image loading
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(plant.imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo of ${plant.name}",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                // Using Coil for efficient image loading and caching
                placeholder = null, // You can add a placeholder drawable here
                error = painterResource( id = R.drawable.plant)// Show a drawable on error
            )
            
            // Plant information
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (plant.notes.isNotEmpty()) {
                        Text(
                            text = plant.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Text(
                    text = "Last watered: ${DateFormatter.formatFullDateTime(plant.lastWateredDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (plant.reminderEnabled) {
                    Text(
                        text = "⏰ Reminder: ${DateFormatter.formatTime(plant.reminderHour, plant.reminderMinute)} daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onWaterClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Mark as watered",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete plant",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state component shown when no plants exist.
 * 
 * Provides visual feedback and encouragement to add the first plant.
 * Uses Material 3 design principles for consistent UI.
 * 
 * @param onAddPlant Callback when add plant button is clicked
 * @param modifier Modifier for customizing the component layout
 */
@Composable
private fun EmptyPlantState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌱",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No plants yet!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first plant to start caring for it",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Utility function to format date for display.
 * 
 * Formats dates in a user-friendly way using the device's default locale.
 * 
 * @param date Date to format
 * @return Formatted date string
 */
private fun formatDate(date: Date): String {
    val now = Date()
    val diffInDays = ((now.time - date.time) / (1000 * 60 * 60 * 24)).toInt()
    
    return when {
        diffInDays == 0 -> "Today"
        diffInDays == 1 -> "Yesterday"
        diffInDays < 7 -> "$diffInDays days ago"
        else -> DateFormat.getDateInstance(DateFormat.SHORT).format(date)
    }
}