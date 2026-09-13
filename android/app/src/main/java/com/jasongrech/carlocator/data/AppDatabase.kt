package com.jasongrech.carlocator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ParkingSpot::class, SafeZone::class, CarDevice::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parkingSpotDao(): ParkingSpotDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun carDeviceDao(): CarDeviceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_locator.db"
                )
                    // No migration path from v1 — this is pre-release, personal-use data,
                    // so a clean wipe on schema change is an acceptable trade for not
                    // hand-writing migrations nobody else depends on.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
