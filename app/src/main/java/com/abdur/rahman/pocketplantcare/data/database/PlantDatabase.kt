package com.abdur.rahman.pocketplantcare.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abdur.rahman.pocketplantcare.data.converter.DateConverter
import com.abdur.rahman.pocketplantcare.data.dao.PlantDao
import com.abdur.rahman.pocketplantcare.data.entity.Plant

/**
 * Room database class for Pocket Plant Care application.
 * 
 * This is the main database configuration following Android's Room persistence library
 * patterns. Serves as the primary access point to the underlying SQLite database and
 * coordinates with the DAO classes for data operations.
 * 
 * Key features:
 * - Version 2 database schema with Plant entity including reminder time fields
 * - Date type converters for proper date handling
 * - Singleton pattern for database instance management
 * - Database migration support for version updates
 * - Export schema disabled for simplicity (can be enabled for production)
 * 
 * Architecture: This database class is part of the data layer in the MVVM architecture,
 * providing the persistent storage component that repositories interact with.
 */
@Database(
    entities = [Plant::class],
    version = 2,
    exportSchema = false // Set to true in production for schema management
)
@TypeConverters(DateConverter::class)
abstract class PlantDatabase : RoomDatabase() {
    
    /**
     * Provides access to Plant DAO for plant-related database operations.
     * Room automatically implements this abstract method.
     */
    abstract fun plantDao(): PlantDao
    
    companion object {
        /**
         * Singleton instance of the database.
         * Volatile ensures proper visibility across threads.
         */
        @Volatile
        private var INSTANCE: PlantDatabase? = null
        
        /**
         * Database name constant for consistency.
         */
        private const val DATABASE_NAME = "plant_database"
        
        /**
         * Migration from version 1 to 2.
         * Adds reminderHour and reminderMinute columns to plants table.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN reminderHour INTEGER NOT NULL DEFAULT 9")
                database.execSQL("ALTER TABLE plants ADD COLUMN reminderMinute INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        /**
         * Get database instance using singleton pattern.
         * 
         * Uses double-checked locking for thread safety while avoiding
         * unnecessary synchronization overhead in subsequent calls.
         * 
         * @param context Application context for database creation
         * @return PlantDatabase singleton instance
         */
        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}