package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    fun parseCustomReminders(json: String): List<Int> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<Int>()
            for (i in 0 until array.length()) {
                val d = array.getInt(i)
                if (d > 0 && !list.contains(d)) list.add(d)
            }
            list.sorted()
        } catch (_: Exception) {
            json.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it > 0 }.distinct().sorted()
        }
    }

    fun scheduleReminders(context: Context, event: EventEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        // Check event custom reminders or category-specific custom reminders
        val categoryItem = com.example.data.CategoryManager.getCategoryByName(context, event.category)
        val eventReminders = parseCustomReminders(event.customRemindersJson)
        val daysSet = mutableSetOf<Int>()

        if (eventReminders.isNotEmpty()) {
            daysSet.addAll(eventReminders)
        } else {
            if (event.notify1DayBefore) daysSet.add(1)
            categoryItem.customReminders.forEach { if (it > 0) daysSet.add(it) }
        }

        if (daysSet.isNotEmpty()) {
            daysSet.forEach { days ->
                val triggerTime = event.targetTimeMillis - (days * 24 * 3600 * 1000L)
                if (triggerTime > now) {
                    scheduleAlarm(context, alarmManager, event, "${days}days", triggerTime)
                } else {
                    cancelAlarm(context, alarmManager, event, "${days}days")
                }
            }
        } else {
            cancelAlarm(context, alarmManager, event, "1day")
        }

        // 6 Hours before
        if (event.notify6HoursBefore) {
            val triggerTime = event.targetTimeMillis - (6 * 3600 * 1000L)
            if (triggerTime > now) {
                scheduleAlarm(context, alarmManager, event, "6hours", triggerTime)
            } else {
                cancelAlarm(context, alarmManager, event, "6hours")
            }
        } else {
            cancelAlarm(context, alarmManager, event, "6hours")
        }

        // 1 Hour before
        if (event.notify1HourBefore) {
            val triggerTime = event.targetTimeMillis - (3600 * 1000L)
            if (triggerTime > now) {
                scheduleAlarm(context, alarmManager, event, "1hour", triggerTime)
            } else {
                cancelAlarm(context, alarmManager, event, "1hour")
            }
        } else {
            cancelAlarm(context, alarmManager, event, "1hour")
        }

        // At Expiry
        if (event.notifyAtExpiry) {
            val triggerTime = event.targetTimeMillis
            if (triggerTime > now) {
                scheduleAlarm(context, alarmManager, event, "expiry", triggerTime)
            } else {
                cancelAlarm(context, alarmManager, event, "expiry")
            }
        } else {
            cancelAlarm(context, alarmManager, event, "expiry")
        }
    }

    fun cancelAllReminders(context: Context, event: EventEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val categoryItem = com.example.data.CategoryManager.getCategoryByName(context, event.category)
        val eventReminders = parseCustomReminders(event.customRemindersJson)
        eventReminders.forEach { days ->
            cancelAlarm(context, alarmManager, event, "${days}days")
        }
        categoryItem.customReminders.forEach { days ->
            cancelAlarm(context, alarmManager, event, "${days}days")
        }
        cancelAlarm(context, alarmManager, event, "${categoryItem.notifyDaysBefore}days")
        cancelAlarm(context, alarmManager, event, "1day")
        cancelAlarm(context, alarmManager, event, "2days")
        cancelAlarm(context, alarmManager, event, "3days")
        cancelAlarm(context, alarmManager, event, "7days")
        cancelAlarm(context, alarmManager, event, "14days")
        cancelAlarm(context, alarmManager, event, "30days")
        cancelAlarm(context, alarmManager, event, "6hours")
        cancelAlarm(context, alarmManager, event, "1hour")
        cancelAlarm(context, alarmManager, event, "expiry")
    }

    /**
     * Exact alarm scheduling logic following modern Android AlarmManager practices (as used in PinIt).
     * Automatically handles Android 12+ (API 31+) canScheduleExactAlarms checks,
     * Doze mode / Idle allow-while-idle, and falls back gracefully if permissions are restricted.
     */
    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        event: EventEntity,
        type: String,
        triggerTimeMillis: Long
    ) {
        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            putExtra("EXTRA_EVENT_ID", event.id)
            putExtra("EXTRA_EVENT_TITLE", event.title)
            putExtra("EXTRA_EVENT_DESC", event.description)
            putExtra("EXTRA_EVENT_ICON", event.icon)
            putExtra("EXTRA_EVENT_IMAGE", event.imageUrl)
            putExtra("EXTRA_EVENT_LINK", event.linkUrl)
            putExtra("EXTRA_TARGET_TIME", event.targetTimeMillis)
            putExtra("EXTRA_REMINDER_TYPE", type)
        }
        val requestCode = ((event.id.hashCode() * 31 + type.hashCode()) and 0x7FFFFFFF)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact allow while idle if exact alarms permission is disabled
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission unavailable, falling back to setAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm", ex)
            }
        }
    }

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        event: EventEntity,
        type: String
    ) {
        val intent = Intent(context, EventAlarmReceiver::class.java)
        val requestCode = ((event.id.hashCode() * 31 + type.hashCode()) and 0x7FFFFFFF)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Reschedules all upcoming reminders for all active events (called on device boot or package replace).
     */
    fun rescheduleAllActive(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).eventDao()
                val now = System.currentTimeMillis()
                val activeEvents = dao.getAllEvents().filter { !it.isCompleted && it.targetTimeMillis > now }
                for (event in activeEvents) {
                    scheduleReminders(context, event)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling active alarms", e)
            }
        }
    }
}
