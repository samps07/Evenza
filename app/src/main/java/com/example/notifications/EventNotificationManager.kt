package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.EventEntity
import com.example.domain.ProgressCalculator
import com.example.util.ImageStorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EventNotificationManager {

    const val CHANNEL_PINNED = "evenza_pinned_channel"
    const val CHANNEL_REMINDERS = "evenza_reminders_channel"

    const val ACTION_DISMISS_NOTIFICATION = "com.example.counter.ACTION_DISMISS_NOTIFICATION"
    const val ACTION_COPY_TEXT = "com.example.counter.ACTION_COPY_TEXT"

    const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    const val EXTRA_COPY_TEXT = "EXTRA_COPY_TEXT"

    private val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val pinnedChannel = NotificationChannel(
                CHANNEL_PINNED,
                "Pinned Notes & Events",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent pinned notes and event previews in the notification shade"
                setShowBadge(false)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Countdown Reminders & Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and reminders for scheduled counter deadlines"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(pinnedChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Immediately pushes the event to a persistent notification with full preview,
     * Done dismiss action, and Copy note action.
     */
    fun showPinnedNotification(context: Context, event: EventEntity, showToast: Boolean = true) {
        createNotificationChannels(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (event.id and 0x7FFFFFFF).toInt()

        val calc = ProgressCalculator.calculate(event)
        val icon = event.icon.ifBlank { "⏳" }
        val title = event.title

        // Build full preview text of the note
        val fullBody = buildString {
            if (event.description.isNotBlank()) {
                append(event.description)
            }
            if (event.linkUrl.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append("🔗 ${event.linkUrl}")
            }
            if (isNotEmpty()) append("\n\n")
            append("⏳ Due: ${dateFormat.format(Date(event.targetTimeMillis))} (${calc.formattedRemaining})")
        }

        val textToCopy = getCopyableText(event)

        // PendingIntent for clicking notification body to open Event Details in app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_EVENT_ID", event.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent for "Done" button (dismiss persistent notification)
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS_NOTIFICATION
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent for "Copy" button (copy note text directly without opening app)
        val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COPY_TEXT
            putExtra(EXTRA_COPY_TEXT, textToCopy)
        }
        val copyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200000,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PINNED)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle("$icon $title")
            .setContentText(if (event.description.isNotBlank()) event.description else calc.formattedRemaining)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true) // PERSISTENT - cannot be dismissed by swiping
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_notification_done, "Done", donePendingIntent)
            .addAction(R.drawable.ic_notification_copy, "Copy", copyPendingIntent)

        // Check if there is an image attachment to show full image preview
        var imageBitmap: Bitmap? = null
        if (event.imageUrl.isNotBlank()) {
            imageBitmap = ImageStorageHelper.loadBitmap(event.imageUrl, maxDimension = 640)
        }

        if (imageBitmap != null) {
            val bigPicStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(imageBitmap)
                .setBigContentTitle("$icon $title")
                .setSummaryText(if (event.description.isNotBlank()) event.description else calc.formattedRemaining)
            builder.setStyle(bigPicStyle)
            builder.setLargeIcon(imageBitmap)
        } else {
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(fullBody)
                .setBigContentTitle("$icon $title")
                .setSummaryText(calc.formattedRemaining)
            builder.setStyle(bigTextStyle)
        }

        notificationManager.notify(notificationId, builder.build())

        if (showToast) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Pinned '$title' to notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows a scheduled alert/reminder notification with full note preview and actions.
     */
    fun showReminderNotification(
        context: Context,
        event: EventEntity,
        reminderType: String
    ) {
        createNotificationChannels(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (event.id.hashCode() * 31 + reminderType.hashCode()) and 0x7FFFFFFF

        val icon = event.icon.ifBlank { "⏳" }
        val title = event.title

        val reminderNotice = when (reminderType) {
            "1day" -> "Deadline in 1 day"
            "6hours" -> "Deadline in 6 hours"
            "1hour" -> "Deadline in 1 hour"
            else -> "Deadline reached!"
        }

        val fullBody = buildString {
            append("🔔 $reminderNotice\n")
            if (event.description.isNotBlank()) {
                append("\n${event.description}")
            }
            if (event.linkUrl.isNotBlank()) {
                append("\n\n🔗 ${event.linkUrl}")
            }
            append("\n\nDue: ${dateFormat.format(Date(event.targetTimeMillis))}")
        }

        val textToCopy = getCopyableText(event)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_EVENT_ID", event.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS_NOTIFICATION
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COPY_TEXT
            putExtra(EXTRA_COPY_TEXT, textToCopy)
        }
        val copyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200000,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle("$icon $title")
            .setContentText(reminderNotice)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_notification_done, "Done", donePendingIntent)
            .addAction(R.drawable.ic_notification_copy, "Copy", copyPendingIntent)

        var imageBitmap: Bitmap? = null
        if (event.imageUrl.isNotBlank()) {
            imageBitmap = ImageStorageHelper.loadBitmap(event.imageUrl, maxDimension = 640)
        }

        if (imageBitmap != null) {
            val bigPicStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(imageBitmap)
                .setBigContentTitle("$icon $title • $reminderNotice")
                .setSummaryText(if (event.description.isNotBlank()) event.description else reminderNotice)
            builder.setStyle(bigPicStyle)
            builder.setLargeIcon(imageBitmap)
        } else {
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(fullBody)
                .setBigContentTitle("$icon $title")
                .setSummaryText(reminderNotice)
            builder.setStyle(bigTextStyle)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun getCopyableText(event: EventEntity): String {
        return when {
            event.description.isNotBlank() && event.linkUrl.isNotBlank() ->
                "${event.description}\n${event.linkUrl}"
            event.description.isNotBlank() -> event.description
            event.linkUrl.isNotBlank() -> event.linkUrl
            else -> event.title
        }
    }
}
