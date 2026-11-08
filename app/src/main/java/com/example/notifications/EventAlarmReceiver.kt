package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("EXTRA_EVENT_ID", 0L)
        val eventTitle = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: "Event"
        val reminderType = intent.getStringExtra("EXTRA_REMINDER_TYPE") ?: "expiry"
        val eventDesc = intent.getStringExtra("EXTRA_EVENT_DESC") ?: ""
        val eventIcon = intent.getStringExtra("EXTRA_EVENT_ICON") ?: "⏳"
        val eventImage = intent.getStringExtra("EXTRA_EVENT_IMAGE") ?: ""
        val eventLink = intent.getStringExtra("EXTRA_EVENT_LINK") ?: ""
        val targetTime = intent.getLongExtra("EXTRA_TARGET_TIME", System.currentTimeMillis())

        if (eventId != 0L) {
            // Verify from Room database to ensure event wasn't deleted or completed
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getDatabase(context).eventDao()
                    val currentEvent = dao.getEventById(eventId)
                    if (currentEvent != null && !currentEvent.isCompleted) {
                        EventNotificationManager.showReminderNotification(context, currentEvent, reminderType)
                    } else if (currentEvent == null) {
                        // Event might have been deleted, do not notify
                    }
                } catch (_: Exception) {
                    // Fallback using extras
                    val fallbackEvent = EventEntity(
                        id = eventId,
                        title = eventTitle,
                        description = eventDesc,
                        startTimeMillis = 0L,
                        targetTimeMillis = targetTime,
                        icon = eventIcon,
                        imageUrl = eventImage,
                        linkUrl = eventLink
                    )
                    EventNotificationManager.showReminderNotification(context, fallbackEvent, reminderType)
                }
            }
        }
    }
}
