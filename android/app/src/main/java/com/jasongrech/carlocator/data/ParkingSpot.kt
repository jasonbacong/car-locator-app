package com.jasongrech.carlocator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spots")
data class ParkingSpot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    val note: String? = null
)
