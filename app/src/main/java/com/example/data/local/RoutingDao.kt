package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutingDao {
    @Query("SELECT * FROM routing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RoutingEntity>>

    @Query("SELECT * FROM routing_history WHERE category = :category ORDER BY timestamp DESC")
    fun getHistoryByCategory(category: String): Flow<List<RoutingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoutingEntity): Long

    @Delete
    suspend fun delete(entity: RoutingEntity)

    @Query("DELETE FROM routing_history")
    suspend fun clearAll()

    @Query("UPDATE routing_history SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
