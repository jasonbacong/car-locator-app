package com.jasongrech.carlocator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A named location, in addition to the home zone, where auto-save should stay
 * quiet — a workplace, a gym, anywhere you regularly disconnect from the car
 * without having actually parked somewhere new.
 */
@Entity(tableName = "safe_zones")
data class SafeZone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lat: Double,
    val lng: Double,
    val radiusMeters: Float
)
