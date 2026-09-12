package com.jasongrech.carlocator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSpotDao {
    @Query("SELECT * FROM parking_spots ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ParkingSpot>>

    @Query("SELECT * FROM parking_spots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestOnce(): ParkingSpot?

    @Insert
    suspend fun insert(spot: ParkingSpot): Long

    @Query("UPDATE parking_spots SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String?)

    @Query("DELETE FROM parking_spots WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM parking_spots")
    suspend fun deleteAll()

    @Query("DELETE FROM parking_spots WHERE timestamp < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long)
}
