package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.notifications.EventNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CounterWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH_WIDGET -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, CounterWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    animateRefreshRotation(context, appWidgetManager, appWidgetIds)
                } else {
                    updateAllWidgets(context)
                }
            }

            ACTION_ITEM_CLICK -> {
                when (intent.getStringExtra("CUSTOM_ACTION")) {
                    ACTION_TOGGLE_EXPAND -> {
                        val eventId = intent.getLongExtra("EXTRA_TOGGLE_EVENT_ID", -1L)
                        if (eventId != -1L) {
                            val prefs = context.getSharedPreferences("counter_widget_prefs", Context.MODE_PRIVATE)
                            val currentSet = prefs.getStringSet("expanded_event_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                            val idStr = eventId.toString()
                            if (currentSet.contains(idStr)) {
                                currentSet.remove(idStr)
                            } else {
                                currentSet.add(idStr)
                            }
                            prefs.edit().putStringSet("expanded_event_ids", HashSet(currentSet)).commit()

                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val componentName = ComponentName(context, CounterWidgetProvider::class.java)
                            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
                            }
                        }
                    }

                    ACTION_OPEN_EVENT -> {
                        val eventId = intent.getLongExtra("EXTRA_EVENT_ID", -1L)
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            if (eventId != -1L) {
                                putExtra("EXTRA_EVENT_ID", eventId)
                            }
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(mainIntent)
                    }

                    ACTION_VIEW_IMAGE -> {
                        val imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL")
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            action = ACTION_VIEW_IMAGE
                            putExtra("EXTRA_IMAGE_URL", imageUrl)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(mainIntent)
                    }

                    ACTION_OPEN_URL -> {
                        val rawUrl = intent.getStringExtra("EXTRA_URL")
                        if (!rawUrl.isNullOrBlank()) {
                            val fixedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                "https://$rawUrl"
                            } else {
                                rawUrl
                            }
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    ACTION_PIN_NOTIFICATION -> {
                        val eventId = intent.getLongExtra("EXTRA_EVENT_ID", -1L)
                        if (eventId != -1L) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val event = AppDatabase.getDatabase(context).eventDao().getEventById(eventId)
                                    if (event != null) {
                                        withContext(Dispatchers.Main) {
                                            EventNotificationManager.showPinnedNotification(context, event, showToast = true)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                    ACTION_COPY_TEXT -> {
                        val textToCopy = intent.getStringExtra("EXTRA_COPY_TEXT") ?: ""
                        val title = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: "Event"
                        val copyContent = if (textToCopy.isNotBlank()) textToCopy else title
                        if (copyContent.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Event Content", copyContent)
                            clipboard.setPrimaryClip(clip)

                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.counter.ACTION_REFRESH_WIDGET"
        const val ACTION_ITEM_CLICK = "com.example.counter.ACTION_ITEM_CLICK"
        const val ACTION_TOGGLE_EXPAND = "com.example.counter.ACTION_TOGGLE_EXPAND"
        const val ACTION_OPEN_EVENT = "com.example.counter.ACTION_OPEN_EVENT"
        const val ACTION_OPEN_URL = "com.example.counter.ACTION_OPEN_URL"
        const val ACTION_PIN_NOTIFICATION = "com.example.counter.ACTION_PIN_NOTIFICATION"
        const val ACTION_COPY_TEXT = "com.example.counter.ACTION_COPY_TEXT"
        const val ACTION_VIEW_IMAGE = "com.example.counter.ACTION_VIEW_IMAGE"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_counter)

            // Category filter and custom widget name display (Requirement 3 & 9)
            val prefs = context.getSharedPreferences("counter_widget_prefs", Context.MODE_PRIVATE)
            val widgetName = prefs.getString("widget_${appWidgetId}_name", "")?.trim() ?: ""
            val selectedCategories = prefs.getStringSet("widget_${appWidgetId}_categories", null)
                ?: setOf(prefs.getString("widget_${appWidgetId}_category", "ALL") ?: "ALL")

            val titleText = when {
                widgetName.isNotBlank() -> "Evenza • $widgetName"
                !selectedCategories.contains("ALL") && selectedCategories.isNotEmpty() -> {
                    val label = if (selectedCategories.size == 1) selectedCategories.first() else "${selectedCategories.size} Categories"
                    "Evenza • $label"
                }
                else -> "Evenza"
            }
            views.setTextViewText(R.id.widget_title, titleText)

            if (!selectedCategories.contains("ALL") && selectedCategories.isNotEmpty() && widgetName.isNotBlank()) {
                views.setViewVisibility(R.id.widget_category_badge, View.VISIBLE)
                val label = if (selectedCategories.size == 1) selectedCategories.first() else "${selectedCategories.size} Cats"
                views.setTextViewText(R.id.widget_category_badge, label)
            } else {
                views.setViewVisibility(R.id.widget_category_badge, View.GONE)
            }

            // Clicking on app name inside widget opens the app (Requirement 9)
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 30000,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)

            // Clicking anywhere in widget outside events opens widget config (Requirement 9)
            val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 40000,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, configPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty, configPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent)

            // Bind RemoteViewsService for the scrollable list
            val serviceIntent = Intent(context, CounterWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, serviceIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // Broadcast PendingIntent template for item clicks (expand toggle, open event, open link)
            val itemTemplateIntent = Intent(context, CounterWidgetProvider::class.java).apply {
                action = ACTION_ITEM_CLICK
            }
            val itemTemplatePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                itemTemplateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, itemTemplatePendingIntent)

            // PendingIntent for clicking "+" Add counter button
            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.counter.ACTION_ADD"
                putExtra("EXTRA_OPEN_ADD", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 10000,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add, addPendingIntent)

            // PendingIntent for clicking Refresh button
            val refreshIntent = Intent(context, CounterWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 20000,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // Responsive sizing based on widget options
            val widgetOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            if (minHeight in 1..100) {
                // Compact mode for small widget heights
                views.setViewVisibility(R.id.widget_category_badge, View.GONE)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }

        fun animateRefreshRotation(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_widget_refresh)
                if (drawable != null) {
                    val size = 64
                    val baseBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(baseBmp)
                    drawable.setBounds(0, 0, size, size)
                    drawable.draw(canvas)

                    val angles = listOf(45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f)
                    for (angle in angles) {
                        val matrix = Matrix().apply {
                            postRotate(angle, size / 2f, size / 2f)
                        }
                        val rotated = Bitmap.createBitmap(baseBmp, 0, 0, size, size, matrix, true)
                        for (id in appWidgetIds) {
                            val partialRv = RemoteViews(context.packageName, R.layout.widget_counter)
                            partialRv.setImageViewBitmap(R.id.widget_btn_refresh, rotated)
                            appWidgetManager.partiallyUpdateAppWidget(id, partialRv)
                        }
                        delay(40)
                    }
                }

                withContext(Dispatchers.Main) {
                    updateAllWidgets(context)
                }
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CounterWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)
            }
        }
    }
}
