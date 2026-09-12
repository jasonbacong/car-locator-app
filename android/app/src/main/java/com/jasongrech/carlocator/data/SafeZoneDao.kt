package com.jasongrech.carlocator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeZoneDao {
    @Query("SELECT * FROM safe_zones ORDER BY name ASC")
    fun getAll(): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones")
    suspend fun getAllOnce(): List<SafeZone>

    @Insert
    suspend fun insert(zone: SafeZone): Long

    @Query("DELETE FROM safe_zones WHERE id = :id")
    suspend fun delete(id: Long)
}
