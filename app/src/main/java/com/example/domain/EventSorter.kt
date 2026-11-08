package com.example.domain

import com.example.data.EventEntity

enum class SortMode {
    URGENCY,
    CUSTOM
}

object EventSorter {
    fun sort(
        events: List<EventEntity>,
        sortMode: SortMode,
        nowMillis: Long = System.currentTimeMillis()
    ): List<EventEntity> {
        return when (sortMode) {
            SortMode.URGENCY -> {
                val active = mutableListOf<EventEntity>()
                val expired = mutableListOf<EventEntity>()
                val completed = mutableListOf<EventEntity>()

                for (event in events) {
                    when {
                        event.isCompleted -> completed.add(event)
                        event.targetTimeMillis <= nowMillis -> expired.add(event)
                        else -> active.add(event)
                    }
                }

                // 1. Active: closest deadline first (ascending targetTimeMillis)
                active.sortBy { it.targetTimeMillis }

                // 2. Expired: most recently expired first (descending targetTimeMillis)
                expired.sortByDescending { it.targetTimeMillis }

                // 3. Completed: at the very bottom
                completed.sortByDescending { it.targetTimeMillis }

                active + expired + completed
            }

            SortMode.CUSTOM -> {
                // User's custom order
                events.sortedBy { it.customOrder }
            }
        }
    }
}
