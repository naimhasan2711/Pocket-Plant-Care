package com.abdur.rahman.pocketplantcare.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling photo capture and gallery operations.
 * 
 * Provides functionality for taking photos with camera and selecting images from gallery
 * using Android Intents. Handles file creation, FileProvider configuration, and proper
 * resource management for image operations. Follows Android best practices for file handling
 * and external storage access.
 * 
 * Key features:
 * - Create camera intents with FileProvider for secure file access
 * - Generate unique file names with timestamps
 * - Handle external storage and app-specific directory creation
 * - Provide gallery selection intents
 * - Manage file URIs and paths for image operations
 * - Proper cleanup and resource management
 * 
 * Architecture: Utility class that abstracts photo operations for use in ViewModels
 * and UI components, following separation of concerns principle.
 * 
 * Note: Requires FileProvider configuration in AndroidManifest.xml and appropriate
 * permissions for camera and storage access.
 */
@Singleton
class PhotoManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val IMAGE_DIRECTORY = "PlantPhotos"
        private const val FILE_PROVIDER_AUTHORITY = "com.abdur.rahman.pocketplantcare.fileprovider"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
    }
    
    /**
     * Current photo URI being captured.
     * Used to track the file location during camera operations.
     * Should be cleared after photo operations complete.
     */
    private var currentPhotoUri: Uri? = null
    
    /**
     * Create an intent for capturing photo with camera.
     * 
     * Creates a temporary file using FileProvider for secure access and generates
     * a camera intent that will save the captured image to this file. The file
     * URI is stored for later access after photo capture completes.
     * 
     * @return Pair<Intent, String?> Camera intent and file path (null if creation failed)
     * @throws IOException if file creation fails
     */
    fun createCameraIntent(): Pair<Intent?, String?> {
        return try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                photoFile
            )
            
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            
            Pair(cameraIntent, photoFile.absolutePath)
            
        } catch (e: IOException) {
            // Log error in production app
            Pair(null, null)
        }
    }
    
    /**
     * Create an intent for selecting image from gallery.
     * 
     * Creates a standard gallery selection intent that allows users to choose
     * an existing image from their device gallery or photo library.
     * 
     * @return Intent for gallery image selection
     */
    fun createGalleryIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Create a unique image file for photo storage.
     * 
     * Creates a timestamped image file in the app-specific external storage directory.
     * This ensures unique file names and proper organization of plant photos.
     * The file is created in the app's external files directory which doesn't require
     * storage permissions on Android 10+.
     * 
     * @return File object pointing to the created image file
     * @throws IOException if file creation fails
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName = "PLANT_${timeStamp}"
        
        val storageDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            IMAGE_DIRECTORY
        )
        
        // Create directory if it doesn't exist
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
    
    /**
     * Get the current photo URI from recent camera operation.
     * 
     * Returns the URI of the photo that was just captured with camera.
     * Should be called immediately after successful camera result.
     * 
     * @return Uri? Current photo URI or null if no recent operation
     */
    fun getCurrentPhotoUri(): Uri? = currentPhotoUri
    
    /**
     * Clear the current photo URI.
     * 
     * Should be called after photo operations complete to free resources
     * and prevent memory leaks. Good practice to call in activity/fragment lifecycle methods.
     */
    fun clearCurrentPhotoUri() {
        currentPhotoUri = null
    }
    
    /**
     * Convert content URI to file path.
     * 
     * Attempts to convert a content URI (from gallery selection) to a file path.
     * Note: This may not always work on newer Android versions due to security restrictions.
     * Consider copying the file to app storage for reliable access.
     * 
     * @param uri Content URI to convert
     * @return String? File path or null if conversion fails
     */
    fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return it.getString(columnIndex)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Copy image from URI to app storage.
     * 
     * Copies an image from any URI (gallery, camera, etc.) to the app's private storage.
     * This ensures reliable access to the image file regardless of source and Android version.
     * Recommended for storing plant photos permanently.
     * 
     * @param sourceUri URI of the source image
     * @return String? Path to the copied file or null if operation fails
     */
    fun copyImageToAppStorage(sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val destinationFile = createImageFile()
            
            inputStream?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            destinationFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Delete an image file from app storage.
     * 
     * Safely deletes an image file from the app's storage. Should be called
     * when a plant is deleted or when user changes plant photo to clean up
     * unused files and free storage space.
     * 
     * @param filePath Path to the file to delete
     * @return Boolean True if file was successfully deleted
     */
    fun deleteImageFile(filePath: String?): Boolean {
        return if (filePath != null) {
            try {
                val file = File(filePath)
                file.exists() && file.delete()
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Check if file exists at given path.
     * 
     * Utility method to verify if an image file exists before attempting to load it.
     * Useful for validation before displaying plant photos.
     * 
     * @param filePath Path to check
     * @return Boolean True if file exists and is readable
     */
    fun fileExists(filePath: String?): Boolean {
        return filePath?.let { path ->
            try {
                val file = File(path)
                file.exists() && file.canRead()
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
}