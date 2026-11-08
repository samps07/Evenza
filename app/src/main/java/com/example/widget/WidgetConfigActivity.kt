package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.CategoryManager
import com.example.ui.theme.EvenzaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val prefs = getSharedPreferences("counter_widget_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("widget_${appWidgetId}_name", "") ?: ""
        val savedCats = prefs.getStringSet("widget_${appWidgetId}_categories", null)
            ?: setOf(prefs.getString("widget_${appWidgetId}_category", "ALL") ?: "ALL")
        val savedSort = prefs.getString("widget_${appWidgetId}_sort", "URGENCY") ?: "URGENCY"
        val uiScale = prefs.getFloat("ui_scale", 1.0f)

        // Load all available categories
        val managedCategories = CategoryManager.getCategories(this).map { it.name }

        setContent {
            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density * uiScale,
                fontScale = currentDensity.fontScale * uiScale
            )

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                EvenzaTheme {
                    WidgetConfigScreen(
                        appWidgetId = appWidgetId,
                        initialName = savedName,
                        initialCategories = savedCats,
                        initialSort = savedSort,
                        availableCategories = managedCategories,
                        onSave = { newName, selectedCategories, sortMode ->
                            prefs.edit()
                                .putString("widget_${appWidgetId}_name", newName.trim())
                                .putStringSet("widget_${appWidgetId}_categories", selectedCategories)
                                .putString("widget_${appWidgetId}_category", if (selectedCategories.contains("ALL")) "ALL" else selectedCategories.firstOrNull() ?: "ALL")
                                .putString("widget_${appWidgetId}_sort", sortMode)
                                .apply()

                            val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigActivity)
                            CounterWidgetProvider.updateAppWidget(this@WidgetConfigActivity, appWidgetManager, appWidgetId)

                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultValue)
                            finish()
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    initialName: String,
    initialCategories: Set<String>,
    initialSort: String,
    availableCategories: List<String>,
    onSave: (String, Set<String>, String) -> Unit,
    onCancel: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF141211) else Color(0xFFFDF8F6)
    val cardBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    var widgetName by remember { mutableStateOf(initialName) }

    // Category selection set
    var isAllSelected by remember { mutableStateOf(initialCategories.contains("ALL") || initialCategories.isEmpty()) }
    val selectedIndividualCategories = remember {
        mutableStateListOf<String>().apply {
            if (!isAllSelected) {
                addAll(initialCategories.filter { it != "ALL" })
            }
        }
    }

    var sortMode by remember { mutableStateOf(initialSort) }

    Scaffold(
        containerColor = canvasBg,
        bottomBar = {
            Surface(
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF1F5F9),
                            contentColor = textPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalCategories = if (isAllSelected || selectedIndividualCategories.isEmpty()) {
                                setOf("ALL")
                            } else {
                                selectedIndividualCategories.toSet()
                            }
                            onSave(widgetName, finalCategories, sortMode)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Save Widget", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Configure Widget",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                    Text(
                        text = "Widget #$appWidgetId • Home Screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 0. Widget Name Card (Requirement 3)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "WIDGET NAME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = textSecondary
                    )
                    Text(
                        text = "Set a custom name for this widget. It will appear beside the app name in the widget header.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = widgetName,
                        onValueChange = { widgetName = it },
                        placeholder = { Text("e.g. Pantry, Medicines, Groceries, Work") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF382F2C) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Categories Card (Multi-Select, Requirement 9)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CATEGORY FILTER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = textSecondary
                    )
                    Text(
                        text = "Choose categories to display. Selecting 'All Events' unticks individual categories.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // "All Events" Chip / Toggle
                    FilterChip(
                        selected = isAllSelected,
                        onClick = {
                            isAllSelected = true
                            selectedIndividualCategories.clear()
                        },
                        leadingIcon = if (isAllSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        } else null,
                        label = {
                            Text(
                                text = "All Events (Everything)",
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF1F5F9),
                            labelColor = textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )

                    // Individual Categories FlowRow
                    val allCats = (availableCategories + listOf("Food", "Medicine", "Dairy", "Beverage", "General")).distinct()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allCats.forEach { cat ->
                            val isChecked = !isAllSelected && selectedIndividualCategories.contains(cat)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked) {
                                        selectedIndividualCategories.remove(cat)
                                        if (selectedIndividualCategories.isEmpty()) {
                                            isAllSelected = true
                                        }
                                    } else {
                                        // Untick All Events when choosing an individual category (Requirement 9)
                                        isAllSelected = false
                                        selectedIndividualCategories.add(cat)
                                    }
                                },
                                leadingIcon = if (isChecked) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                } else null,
                                label = { Text(cat, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF1F5F9),
                                    labelColor = textPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Sorting Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "EVENT SORTING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { sortMode = "URGENCY" }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = sortMode == "URGENCY",
                            onClick = { sortMode = "URGENCY" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "By Urgency (Closest first)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                            Text(
                                text = "Recommended: events expiring soon appear at the top",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { sortMode = "CUSTOM" }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = sortMode == "CUSTOM",
                            onClick = { sortMode = "CUSTOM" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Custom Order",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                            Text(
                                text = "Matches your custom arrangement inside the app",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
