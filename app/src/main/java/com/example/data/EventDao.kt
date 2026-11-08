package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM countdown_events ORDER BY customOrder ASC, targetTimeMillis ASC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM countdown_events ORDER BY customOrder ASC, targetTimeMillis ASC")
    fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM countdown_events WHERE category = :category ORDER BY customOrder ASC, targetTimeMillis ASC")
    fun getEventsByCategory(category: String): List<EventEntity>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    fun getEventByIdFlow(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Update
    suspend fun updateEvents(events: List<EventEntity>)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM countdown_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE countdown_events SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String)

    @Query("UPDATE countdown_events SET colorHex = :newColor WHERE category = :category")
    suspend fun updateCategoryColor(category: String, newColor: String)

    @Query("SELECT COUNT(*) FROM countdown_events")
    suspend fun getCount(): Int
}
