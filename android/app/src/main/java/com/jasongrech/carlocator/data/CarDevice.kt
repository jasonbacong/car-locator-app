package com.jasongrech.carlocator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A paired Bluetooth device treated as "a car" — disconnecting from any of these can trigger a save. */
@Entity(tableName = "car_devices")
data class CarDevice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val name: String
)
