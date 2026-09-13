package com.jasongrech.carlocator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDeviceDao {
    @Query("SELECT * FROM car_devices ORDER BY name ASC")
    fun getAll(): Flow<List<CarDevice>>

    @Query("SELECT * FROM car_devices")
    suspend fun getAllOnce(): List<CarDevice>

    @Insert
    suspend fun insert(device: CarDevice): Long

    @Query("DELETE FROM car_devices WHERE id = :id")
    suspend fun delete(id: Long)
}
