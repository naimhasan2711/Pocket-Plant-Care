# 🌱 Pocket Plant Care

A modern Android application for tracking and caring for your plants, built with Kotlin, Jetpack Compose, Hilt, Room Database, and MVVM architecture.

## 📱 Features

### Core Functionality

- **🌿 Plant Management**: Add, edit, and delete plant entries with comprehensive information
- **📸 Photo Support**: Capture photos using camera or select from gallery with Coil image loading
- **💧 Watering Tracking**: Track when plants were last watered with quick watering actions
- **🔔 Smart Reminders**: Daily watering reminders using AlarmManager with notification channels
- **💾 Offline Storage**: Complete offline functionality with Room database persistence

### User Experience

- **🎨 Modern Material 3 UI**: Beautiful, responsive interface following Material Design guidelines
- **📱 Reactive UI**: Real-time updates using StateFlow and Kotlin coroutines
- **🧭 Intuitive Navigation**: Seamless navigation between screens with Jetpack Navigation Compose
- **⚡ Performance Optimized**: Efficient image loading, lazy layouts, and proper resource management

## 🏗️ Architecture & Technology Stack

### Architecture Pattern

- **MVVM (Model-View-ViewModel)**: Clean separation of concerns with reactive data flow
- **Repository Pattern**: Single source of truth for data operations
- **Dependency Injection**: Hilt for clean, testable, and maintainable code

### Core Technologies

- **Kotlin**: 100% Kotlin with coroutines for asynchronous operations
- **Jetpack Compose**: Modern declarative UI toolkit with Material 3
- **Room Database**: Local persistence with type-safe SQL queries
- **Hilt**: Dependency injection framework for Android
- **Navigation Compose**: Type-safe navigation between screens
- **Coil**: Efficient image loading and caching library

### Android Components

- **AlarmManager**: Scheduled watering reminders
- **NotificationManager**: Rich notification system with channels
- **FileProvider**: Secure file sharing for camera operations
- **ActivityResultContracts**: Modern activity result handling

## 📂 Project Structure

```
app/src/main/java/com/abdur/rahman/pocketplantcare/
├── data/                          # Data layer
│   ├── entity/                    # Room entities
│   │   └── Plant.kt              # Plant data model
│   ├── dao/                      # Data Access Objects
│   │   └── PlantDao.kt           # Plant database operations
│   ├── database/                 # Database configuration
│   │   └── PlantDatabase.kt      # Room database setup
│   ├── repository/               # Repository pattern implementation
│   │   └── PlantRepository.kt    # Data operations coordinator
│   └── converter/                # Type converters for Room
│       └── DateConverter.kt      # Date to timestamp conversion
├── presentation/                 # Presentation layer
│   ├── viewmodel/               # ViewModels for UI state
│   │   └── PlantViewModel.kt    # Plant operations ViewModel
│   └── screen/                  # Compose UI screens
│       ├── PlantListScreen.kt   # Main plant list interface
│       ├── PlantDetailScreen.kt # Plant detail and editing
│       └── AddPlantScreen.kt    # New plant creation form
├── di/                          # Dependency injection modules
│   ├── DatabaseModule.kt        # Database dependencies
│   └── UtilityModule.kt         # Utility class dependencies
├── utils/                       # Utility classes
│   ├── PhotoManager.kt          # Camera and gallery operations
│   └── ReminderManager.kt       # Alarm and notification handling
├── ui/theme/                    # Material 3 theming
│   ├── Color.kt                 # Color scheme definitions
│   ├── Theme.kt                 # Theme configuration
│   └── Type.kt                  # Typography definitions
├── PlantCareApplication.kt      # Application class with Hilt
└── MainActivity.kt              # Single activity with navigation
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 28 (minimum) / 36 (target)
- Kotlin 2.0.21 or later

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/pocket-plant-care.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

### Building the Project

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

## 📋 Key Classes Overview

### Data Layer

- **`Plant`**: Room entity representing plant data with all necessary fields
- **`PlantDao`**: Database operations interface with reactive Flow queries
- **`PlantDatabase`**: Room database configuration with type converters
- **`PlantRepository`**: Repository pattern implementation coordinating data operations

### Presentation Layer

- **`PlantViewModel`**: Manages UI state and coordinates business logic operations
- **`PlantListScreen`**: Main screen displaying all plants with Material 3 components
- **`PlantDetailScreen`**: Detailed plant view with editing and photo management
- **`AddPlantScreen`**: Form interface for creating new plant entries

### Utility Classes

- **`PhotoManager`**: Handles camera capture and gallery selection with FileProvider
- **`ReminderManager`**: Manages AlarmManager scheduling and notification display

## 🔧 Configuration

### Permissions

The app requires the following permissions:

- `CAMERA`: For taking plant photos
- `READ_MEDIA_IMAGES`: For selecting images from gallery (Android 13+)
- `POST_NOTIFICATIONS`: For watering reminder notifications
- `SCHEDULE_EXACT_ALARM`: For precise reminder scheduling
- `RECEIVE_BOOT_COMPLETED`: For reminder persistence across reboots

### FileProvider Setup

Configured for secure photo file sharing:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.abdur.rahman.pocketplantcare.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## 📚 External Libraries & Citations

### Image Loading - Coil

```kotlin
// Using Coil for efficient image loading and caching
// Documentation: https://coil-kt.github.io/coil/compose/
implementation("io.coil-kt:coil-compose:2.6.0")
```

### Dependency Injection - Hilt

```kotlin
// Using Hilt for dependency injection following Android best practices
// Documentation: https://developer.android.com/training/dependency-injection/hilt-android
implementation("com.google.dagger:hilt-android:2.51")
```

### Database - Room

```kotlin
// Using Room for local database persistence with reactive queries
// Documentation: https://developer.android.com/training/data-storage/room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
```

## 🎯 Development Best Practices

### Code Quality

- **KDoc Documentation**: Comprehensive documentation for all public APIs
- **Type Safety**: Leveraging Kotlin's type system for robust code
- **Resource Management**: Proper lifecycle-aware resource cleanup
- **Error Handling**: Comprehensive error handling with user feedback

### Architecture Decisions

- **Single Activity**: Modern single-activity architecture with Compose
- **Reactive Programming**: StateFlow and Flow for reactive UI updates
- **Separation of Concerns**: Clear layer separation following MVVM pattern
- **Testability**: Dependency injection enabling easy unit testing

### Performance Optimizations

- **Lazy Loading**: Efficient list rendering with LazyColumn
- **Image Caching**: Coil handles image loading and memory management
- **Database Optimization**: Efficient queries with proper indexing
- **State Management**: Optimized state updates and recomposition

## 🔄 App Workflow

1. **Plant List**: Users start with a list of all their plants
2. **Add Plant**: Tap FAB to add new plants with photo and details
3. **Plant Details**: Tap any plant to view/edit details and manage care
4. **Photo Management**: Camera/gallery integration for plant photos
5. **Watering Tracking**: Quick watering actions with date updates
6. **Reminders**: Toggle daily watering reminders with notifications

## 📱 Compatibility

- **Minimum SDK**: API 28 (Android 9.0)
- **Target SDK**: API 36 (Android 14+)
- **Supported**: Phones and tablets with various screen sizes
- **Language**: English (with RTL layout support)

## 🤝 Contributing

This project demonstrates modern Android development practices and is suitable for:

- Learning MVVM architecture with Jetpack Compose
- Understanding Room database integration
- Exploring Hilt dependency injection
- Working with camera and file operations
- Implementing notification and alarm systems

## 📄 License

This project is created for educational purposes and demonstrates Android development best practices with modern technologies and architecture patterns.

---

**Built with ❤️ using Kotlin, Jetpack Compose, and modern Android development practices**
