package com.example.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            EventNotificationManager.ACTION_DISMISS_NOTIFICATION -> {
                val notifId = intent.getIntExtra(EventNotificationManager.EXTRA_NOTIFICATION_ID, -1)
                if (notifId != -1) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notifId)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Notification dismissed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            EventNotificationManager.ACTION_COPY_TEXT -> {
                val textToCopy = intent.getStringExtra(EventNotificationManager.EXTRA_COPY_TEXT) ?: ""
                if (textToCopy.isNotBlank()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Event Content", textToCopy)
                    clipboard.setPrimaryClip(clip)

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
