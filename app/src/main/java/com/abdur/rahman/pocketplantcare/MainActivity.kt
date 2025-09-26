package com.abdur.rahman.pocketplantcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abdur.rahman.pocketplantcare.presentation.screen.AddPlantScreen
import com.abdur.rahman.pocketplantcare.presentation.screen.PlantDetailScreen
import com.abdur.rahman.pocketplantcare.presentation.screen.PlantListScreen
import com.abdur.rahman.pocketplantcare.ui.theme.PocketPlantCareTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Pocket Plant Care application.
 * 
 * This is the single activity that hosts all Compose screens using Jetpack Navigation.
 * Serves as the entry point for the application and manages the overall navigation flow.
 * Configured with Hilt for dependency injection and follows modern Android development patterns.
 * 
 * Key features:
 * - Single Activity architecture with Compose Navigation
 * - Hilt dependency injection enabled with @AndroidEntryPoint
 * - Edge-to-edge display for modern Android experience  
 * - Material 3 theming throughout the application
 * - Proper navigation state management and back stack handling
 * 
 * Navigation Routes:
 * - "plant_list": Main screen showing all plants
 * - "plant_detail/{plantId}": Detail screen for specific plant
 * - "add_plant": Screen for adding new plants
 * 
 * Architecture: Presentation layer entry point that coordinates navigation between
 * different screens in the MVVM architecture, with no business logic contained here.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    /**
     * Called when the activity is starting.
     * 
     * Sets up the Compose UI with navigation and theming. Enables edge-to-edge display
     * for a modern full-screen experience following Material Design guidelines.
     * 
     * @param savedInstanceState Previously saved state, if any
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketPlantCareTheme {
                PlantCareNavigation()
            }
        }
    }
}

/**
 * Main navigation component for the Plant Care application.
 * 
 * Manages navigation between different screens using Jetpack Navigation Compose.
 * Provides type-safe navigation with proper argument passing for plant details.
 * 
 * Navigation flow:
 * 1. Start at plant list screen
 * 2. Navigate to plant detail when plant is tapped
 * 3. Navigate to add plant screen when FAB is tapped
 * 4. Proper back navigation handling throughout
 */
@Composable
fun PlantCareNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "plant_list"
    ) {
        // Plant list screen - main screen showing all plants
        composable("plant_list") {
            PlantListScreen(
                onNavigateToDetail = { plantId ->
                    navController.navigate("plant_detail/$plantId")
                },
                onNavigateToAdd = {
                    navController.navigate("add_plant")
                }
            )
        }
        
        // Plant detail screen - shows specific plant information
        composable("plant_detail/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")?.toLongOrNull() ?: 0L
            PlantDetailScreen(
                plantId = plantId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Add plant screen - form for creating new plants
        composable("add_plant") {
            AddPlantScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Preview composable for development and design verification.
 * 
 * Shows the main navigation structure in Android Studio's Compose preview.
 * Useful for quickly seeing the overall app structure during development.
 */
@Preview(showBackground = true)
@Composable
fun PlantCareNavigationPreview() {
    PocketPlantCareTheme {
        PlantCareNavigation()
    }
}