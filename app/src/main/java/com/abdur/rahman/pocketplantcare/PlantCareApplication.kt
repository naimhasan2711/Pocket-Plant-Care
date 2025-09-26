package com.abdur.rahman.pocketplantcare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Pocket Plant Care.
 * 
 * This class serves as the entry point for Hilt dependency injection framework.
 * The @HiltAndroidApp annotation triggers Hilt's code generation including a base class
 * for the application that serves as the application-level dependency container.
 * 
 * Key features:
 * - Enables Hilt dependency injection throughout the application
 * - Serves as the root of the dependency graph
 * - Manages application-wide singletons and scoped objects
 * 
 * Architecture: This class is essential for the MVVM architecture implementation
 * as it enables dependency injection for ViewModels, Repositories, and other components.
 */
@HiltAndroidApp
class PlantCareApplication : Application() {
    
    /**
     * Called when the application is starting, before any activity, service, or receiver
     * objects (excluding content providers) have been created.
     * 
     * Currently no additional initialization needed beyond Hilt setup,
     * but this can be extended for future application-wide initialization tasks.
     */
    override fun onCreate() {
        super.onCreate()
        // Future initialization code can be added here
    }
}