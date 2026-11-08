package com.example.data

import android.content.Context
import com.example.widget.CounterWidgetProvider
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val context: Context,
    private val eventDao: EventDao
) {
    val allEventsFlow: Flow<List<EventEntity>> = eventDao.getAllEventsFlow()

    fun getEventByIdFlow(id: Long): Flow<EventEntity?> = eventDao.getEventByIdFlow(id)

    suspend fun getEventById(id: Long): EventEntity? = eventDao.getEventById(id)

    suspend fun insert(event: EventEntity): Long {
        val id = eventDao.insertEvent(event)
        notifyWidgets()
        return id
    }

    suspend fun update(event: EventEntity) {
        eventDao.updateEvent(event)
        notifyWidgets()
    }

    suspend fun delete(event: EventEntity) {
        eventDao.deleteEvent(event)
        notifyWidgets()
    }

    suspend fun deleteById(id: Long) {
        eventDao.deleteEventById(id)
        notifyWidgets()
    }

    suspend fun duplicate(id: Long) {
        val original = eventDao.getEventById(id) ?: return
        val now = System.currentTimeMillis()
        val duration = original.targetTimeMillis - original.startTimeMillis
        val target = if (duration > 0) now + duration else now + (24 * 3600 * 1000L)
        val duplicate = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            startTimeMillis = now,
            targetTimeMillis = target,
            createdDateMillis = now,
            isCompleted = false
        )
        eventDao.insertEvent(duplicate)
        notifyWidgets()
    }

    suspend fun restart(id: Long) {
        val original = eventDao.getEventById(id) ?: return
        val now = System.currentTimeMillis()
        val duration = original.targetTimeMillis - original.startTimeMillis
        val target = if (duration > 0) now + duration else now + (24 * 3600 * 1000L)
        val restarted = original.copy(
            startTimeMillis = now,
            targetTimeMillis = target,
            isCompleted = false
        )
        eventDao.updateEvent(restarted)
        notifyWidgets()
    }

    suspend fun toggleCompleted(id: Long) {
        val original = eventDao.getEventById(id) ?: return
        val updated = original.copy(isCompleted = !original.isCompleted)
        eventDao.updateEvent(updated)
        notifyWidgets()
    }

    suspend fun updateOrder(orderedList: List<EventEntity>) {
        val updated = orderedList.mapIndexed { index, event ->
            event.copy(customOrder = index)
        }
        eventDao.updateEvents(updated)
        notifyWidgets()
    }

    suspend fun updateCategoryName(oldCategory: String, newCategory: String) {
        eventDao.updateCategoryName(oldCategory, newCategory)
        notifyWidgets()
    }

    suspend fun updateCategoryColor(category: String, newColor: String) {
        eventDao.updateCategoryColor(category, newColor)
        notifyWidgets()
    }

    private fun notifyWidgets() {
        try {
            CounterWidgetProvider.updateAllWidgets(context)
        } catch (_: Exception) {
            // Widget not yet bound or ignored
        }
    }
}
