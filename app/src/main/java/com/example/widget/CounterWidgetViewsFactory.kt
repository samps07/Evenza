package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.EventEntity
import com.example.domain.EventSorter
import com.example.domain.ProgressCalculator
import com.example.domain.SortMode
import com.example.domain.UrgencyLevel
import com.example.util.ImageStorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CounterWidgetViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private val events = mutableListOf<EventEntity>()
    private val expandedEventIds = mutableSetOf<String>()
    private val bitmapCache = mutableMapOf<String, android.graphics.Bitmap>()
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreate() {
        // Initial setup
    }

    override fun onDataSetChanged() {
        events.clear()
        expandedEventIds.clear()
        try {
            val prefs = context.getSharedPreferences("counter_widget_prefs", Context.MODE_PRIVATE)
            val selectedCategories = prefs.getStringSet("widget_${appWidgetId}_categories", null)
                ?: setOf(prefs.getString("widget_${appWidgetId}_category", "ALL") ?: "ALL")
            val sortPref = prefs.getString("widget_${appWidgetId}_sort", "URGENCY") ?: "URGENCY"
            val savedExpanded = prefs.getStringSet("expanded_event_ids", emptySet()) ?: emptySet()
            expandedEventIds.addAll(savedExpanded)

            val dao = AppDatabase.getDatabase(context).eventDao()
            val rawEvents = dao.getAllEvents()

            val categoryFiltered = if (selectedCategories.contains("ALL") || selectedCategories.isEmpty()) {
                rawEvents
            } else {
                rawEvents.filter { event ->
                    selectedCategories.any { cat -> cat.equals(event.category, ignoreCase = true) }
                }
            }

            val sortMode = if (sortPref == "CUSTOM") SortMode.CUSTOM else SortMode.URGENCY
            val now = System.currentTimeMillis()
            // Do not show expired/history events in widget
            val activeEvents = categoryFiltered.filter { !it.isCompleted && it.targetTimeMillis > now }
            val sorted = EventSorter.sort(activeEvents, sortMode, now)
            events.addAll(sorted)

            // Pre-cache bitmaps for active events with images so they display immediately on first tap
            for (event in activeEvents) {
                if (event.imageUrl.isNotBlank() && !bitmapCache.containsKey(event.imageUrl)) {
                    val bmp = ImageStorageHelper.loadRoundedBitmap(event.imageUrl, sizePx = 112, cornerRadiusPx = 16f, context = context)
                    if (bmp != null) {
                        bitmapCache[event.imageUrl] = bmp
                    }
                }
            }
        } catch (_: Exception) {
            // Handle error gracefully
        }
    }

    override fun onDestroy() {
        events.clear()
        expandedEventIds.clear()
        bitmapCache.clear()
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position !in events.indices) return RemoteViews(context.packageName, R.layout.widget_item)

        val event = events[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item)
        val calc = ProgressCalculator.calculate(event)

        views.setTextViewText(R.id.widget_item_icon, event.icon)
        views.setTextViewText(R.id.widget_item_title, event.title)
        views.setTextViewText(
            R.id.widget_item_time,
            if (calc.urgencyLevel == UrgencyLevel.YET_TO_START) "Yet to start" else calc.formattedRemaining
        )
        views.setTextViewText(R.id.widget_item_percent, calc.percentText)

        // Subtext date
        val subtext = when (calc.urgencyLevel) {
            UrgencyLevel.YET_TO_START -> "Starts ${dateFormat.format(Date(event.startTimeMillis))}"
            UrgencyLevel.EXPIRED -> "Expired on ${dateFormat.format(Date(event.targetTimeMillis))}"
            UrgencyLevel.COMPLETED -> "Completed"
            else -> "Due ${dateFormat.format(Date(event.targetTimeMillis))}"
        }
        views.setTextViewText(R.id.widget_item_subtext, subtext)

        // Determine effective event theme color
        val effectiveThemeColorHex = when {
            event.colorHex.isNotBlank() -> event.colorHex
            else -> {
                val cat = com.example.data.CategoryManager.getCategoryByName(context, event.category)
                cat.colorHex
            }
        }

        // Card Background and Color Palette matching event theme and app bento grid
        val (cardBgRes, timeColor, subtextColor) = when {
            calc.isCompleted -> Triple(R.drawable.widget_card_bg_completed, Color.parseColor("#34D399"), Color.parseColor("#10B981"))
            calc.isExpired -> Triple(R.drawable.widget_card_bg_expired, Color.parseColor("#94A3B8"), Color.parseColor("#94A3B8"))
            effectiveThemeColorHex.isNotBlank() -> {
                val parsedColor = try { Color.parseColor(effectiveThemeColorHex) } catch (_: Exception) { Color.parseColor("#38BDF8") }
                val hsv = FloatArray(3)
                Color.colorToHSV(parsedColor, hsv)
                val hue = hsv[0]
                val bg = when {
                    effectiveThemeColorHex.uppercase() in setOf("#10B981", "#059669", "#14B8A6", "#0D9488") || hue in 70f..165f -> R.drawable.widget_card_bg_emerald
                    effectiveThemeColorHex.uppercase() in setOf("#8B5CF6", "#7C3AED", "#6366F1", "#4F46E5") || hue in 255f..295f -> R.drawable.widget_card_bg_purple
                    effectiveThemeColorHex.uppercase() in setOf("#EC4899", "#DB2777") || hue in 295f..345f -> R.drawable.widget_card_bg_rose
                    effectiveThemeColorHex.uppercase() in setOf("#F97316", "#EA580C", "#EF4444", "#DC2626") || hue in 345f..360f || hue in 0f..20f -> R.drawable.widget_card_bg_urgent
                    effectiveThemeColorHex.uppercase() in setOf("#EAB308", "#D97706", "#F59E0B") || hue in 20f..70f -> R.drawable.widget_card_bg_amber
                    else -> R.drawable.widget_card_bg_blue
                }
                Triple(bg, parsedColor, parsedColor)
            }
            calc.urgencyLevel == UrgencyLevel.CRITICAL || (calc.remainingMillis in 1..86400_000L) -> {
                Triple(R.drawable.widget_card_bg_urgent, Color.parseColor("#FB923C"), Color.parseColor("#FB923C"))
            }
            calc.urgencyLevel == UrgencyLevel.WARNING || (calc.remainingMillis in 86400_001L..(7 * 86400_000L)) -> {
                Triple(R.drawable.widget_card_bg_blue, Color.parseColor("#60A5FA"), Color.parseColor("#60A5FA"))
            }
            else -> {
                Triple(R.drawable.widget_card_bg_neutral, Color.parseColor("#94A3B8"), Color.parseColor("#64748B"))
            }
        }

        views.setImageViewResource(R.id.widget_item_bg, cardBgRes)
        views.setTextColor(R.id.widget_item_time, timeColor)
        views.setTextColor(R.id.widget_item_percent, subtextColor)

        // Render seek bar bitmap with effective theme color
        try {
            val bitmap = CounterWidgetRenderer.renderSeekBarBitmap(
                progress = calc.progress,
                accentColorHex = effectiveThemeColorHex,
                urgency = calc.urgencyLevel,
                width = 440,
                height = 20
            )
            views.setImageViewBitmap(R.id.widget_item_progress_bar, bitmap)
        } catch (_: Exception) {
            // Fallback
        }

        // Notification Button (push to persistent notification shade immediately)
        val notifyIntent = Intent().apply {
            putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_PIN_NOTIFICATION)
            putExtra("EXTRA_EVENT_ID", event.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_btn_notify, notifyIntent)

        // Copy Button (copies event body text directly to clipboard without opening app)
        val textToCopy = com.example.notifications.EventNotificationManager.getCopyableText(event)
        val copyIntent = Intent().apply {
            putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_COPY_TEXT)
            putExtra("EXTRA_EVENT_ID", event.id)
            putExtra("EXTRA_COPY_TEXT", textToCopy)
            putExtra("EXTRA_EVENT_TITLE", event.title)
        }
        views.setOnClickFillInIntent(R.id.widget_item_btn_copy, copyIntent)

        // Check content availability for dropdown
        val hasContent = event.description.isNotBlank() || event.linkUrl.isNotBlank() || event.imageUrl.isNotBlank()
        val isExpanded = hasContent && expandedEventIds.contains(event.id.toString())

        if (hasContent) {
            views.setViewVisibility(R.id.widget_item_btn_dropdown, View.VISIBLE)

            // FillInIntent to toggle expansion
            val toggleIntent = Intent().apply {
                putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_TOGGLE_EXPAND)
                putExtra("EXTRA_TOGGLE_EVENT_ID", event.id)
            }
            views.setOnClickFillInIntent(R.id.widget_item_btn_dropdown, toggleIntent)

            if (isExpanded) {
                views.setImageViewResource(R.id.widget_item_btn_dropdown, R.drawable.ic_widget_chevron_up)
                views.setViewVisibility(R.id.widget_item_dropdown_box, View.VISIBLE)

                // Image in small square on left (clickable to enlarge)
                if (event.imageUrl.isNotBlank()) {
                    val roundedBmp = bitmapCache[event.imageUrl]
                        ?: ImageStorageHelper.loadRoundedBitmap(event.imageUrl, sizePx = 112, cornerRadiusPx = 16f, context = context)?.also {
                            bitmapCache[event.imageUrl] = it
                        }
                    if (roundedBmp != null) {
                        views.setImageViewBitmap(R.id.widget_item_dropdown_image, roundedBmp)
                        views.setViewVisibility(R.id.widget_item_dropdown_image, View.VISIBLE)

                        // FillInIntent to view enlarged image
                        val imageIntent = Intent().apply {
                            putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_VIEW_IMAGE)
                            putExtra("EXTRA_IMAGE_URL", event.imageUrl)
                            putExtra("EXTRA_EVENT_ID", event.id)
                        }
                        views.setOnClickFillInIntent(R.id.widget_item_dropdown_image, imageIntent)
                    } else {
                        views.setViewVisibility(R.id.widget_item_dropdown_image, View.GONE)
                    }
                } else {
                    views.setViewVisibility(R.id.widget_item_dropdown_image, View.GONE)
                }

                // Description on right
                if (event.description.isNotBlank()) {
                    views.setTextViewText(R.id.widget_item_dropdown_desc, event.description)
                    views.setViewVisibility(R.id.widget_item_dropdown_desc, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_item_dropdown_desc, View.GONE)
                }

                // URL on right
                if (event.linkUrl.isNotBlank()) {
                    views.setTextViewText(R.id.widget_item_dropdown_url, "🔗 " + event.linkUrl)
                    views.setViewVisibility(R.id.widget_item_dropdown_url, View.VISIBLE)

                    // FillInIntent to open URL
                    val urlIntent = Intent().apply {
                        putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_OPEN_URL)
                        putExtra("EXTRA_URL", event.linkUrl)
                    }
                    views.setOnClickFillInIntent(R.id.widget_item_dropdown_url, urlIntent)
                } else {
                    views.setViewVisibility(R.id.widget_item_dropdown_url, View.GONE)
                }
            } else {
                views.setImageViewResource(R.id.widget_item_btn_dropdown, R.drawable.ic_widget_chevron_down)
                views.setViewVisibility(R.id.widget_item_dropdown_box, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_item_btn_dropdown, View.GONE)
            views.setViewVisibility(R.id.widget_item_dropdown_box, View.GONE)
        }

        // FillInIntent on main card content to open Event Details in MainActivity
        val openIntent = Intent().apply {
            putExtra("CUSTOM_ACTION", CounterWidgetProvider.ACTION_OPEN_EVENT)
            putExtra("EXTRA_EVENT_ID", event.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_main_content, openIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in events.indices) events[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
