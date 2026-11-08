package com.example.ui.components

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryItem
import com.example.data.CategoryManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySettingsSection(
    modifier: Modifier = Modifier,
    onCategoriesChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF26201D) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    var categories by remember { mutableStateOf(CategoryManager.getCategories(context)) }

    // Dialog states
    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    fun refresh() {
        categories = CategoryManager.getCategories(context)
        onCategoriesChanged()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Categories & Due-In Alerts",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Configure custom categories and their automatic alert timeline before expiry. The AI also automatically categorizes labels into these categories.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category list items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(item.colorHex))
                    } catch (_: Exception) {
                        Color(0xFF3B82F6)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header Row: Icon, Name, Dot, Edit/Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.icon, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = catColor.copy(alpha = if (isDark) 0.25f else 0.12f),
                                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Theme",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = catColor
                                        )
                                    }
                                }
                            }

                            // Action Buttons: Edit and Delete
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { categoryToEdit = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { categoryToDelete = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Category",
                                        tint = if (categories.size > 1) Color(0xFFEF4444) else textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Alerts Section Placed Vertically Below Header (Ample Horizontal Width)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val alertSummary = when {
                                item.customReminders.isEmpty() -> "No early alert configured"
                                item.customReminders.size == 1 -> "Alert ${item.customReminders.first()}d before expiry"
                                else -> "Alerts: ${item.customReminders.joinToString(", ") { "${it}d" }} before expiry"
                            }
                            Text(
                                text = alertSummary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (item.customReminders.isNotEmpty()) Color(0xFF2563EB) else textSecondary
                            )
                        }

                        if (item.customReminders.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                item.customReminders.sorted().forEach { d ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                                        border = BorderStroke(0.5.dp, Color(0xFF3B82F6))
                                    ) {
                                        Text(
                                            text = "${d}d before",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Category Button
        Button(
            onClick = { isCreatingNew = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                contentColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }

    // Add / Edit Category Dialog
    if (isCreatingNew || categoryToEdit != null) {
        val isEditing = categoryToEdit != null
        val initialItem = categoryToEdit ?: CategoryItem(
            name = "",
            icon = "🏷️",
            colorHex = "#3B82F6",
            notifyDaysBefore = 1,
            customReminders = listOf(1)
        )

        CategoryFormDialog(
            title = if (isEditing) "Edit Category" else "Add New Category",
            initialName = initialItem.name,
            initialIcon = initialItem.icon,
            initialColorHex = initialItem.colorHex,
            initialCustomReminders = if (initialItem.customReminders.isNotEmpty()) initialItem.customReminders else (if (initialItem.notifyDaysBefore > 0) listOf(initialItem.notifyDaysBefore) else emptyList()),
            isEditing = isEditing,
            onDismiss = {
                isCreatingNew = false
                categoryToEdit = null
            },
            onSave = { name, icon, colorHex, customReminders ->
                if (name.isBlank()) {
                    Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@CategoryFormDialog
                }
                val newItem = CategoryItem(
                    name = name.trim(),
                    icon = if (icon.isNotBlank()) icon.trim() else "🏷️",
                    colorHex = colorHex,
                    notifyDaysBefore = customReminders.firstOrNull() ?: 1,
                    customReminders = customReminders
                )
                if (isEditing) {
                    if (!initialItem.name.equals(newItem.name, ignoreCase = true)) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            try {
                                com.example.data.AppDatabase.getDatabase(context).eventDao().updateCategoryName(initialItem.name, newItem.name)
                            } catch (_: Exception) {}
                        }
                    }
                    CategoryManager.updateCategory(context, initialItem.name, newItem)
                    Toast.makeText(context, "Updated '${newItem.name}'", Toast.LENGTH_SHORT).show()
                } else {
                    CategoryManager.addCategory(context, newItem)
                    Toast.makeText(context, "Added '${newItem.name}'", Toast.LENGTH_SHORT).show()
                }
                isCreatingNew = false
                categoryToEdit = null
                refresh()
            }
        )
    }

    // Delete Confirmation Dialog
    if (categoryToDelete != null) {
        val target = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete '${target.name}'? Existing events will keep their current category, but it will be removed from future selection and AI options."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        CategoryManager.deleteCategory(context, target.name)
                        Toast.makeText(context, "Deleted '${target.name}'", Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                        refresh()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFormDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialColorHex: String,
    initialCustomReminders: List<Int>,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, colorHex: String, customReminders: List<Int>) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var colorHex by remember { mutableStateOf(initialColorHex) }
    var selectedReminders by remember {
        mutableStateOf(initialCustomReminders.filter { it > 0 }.toSet())
    }
    var customDaysInput by remember { mutableStateOf("") }

    val presetIcons = listOf("🍞", "💊", "🥛", "🧃", "🥫", "💄", "⏳", "🧴", "🍎", "🥩", "🧼", "🍪", "🍷", "🔋", "📦")
    val presetColors = listOf(
        "#F97316" to "Orange",
        "#EC4899" to "Pink",
        "#3B82F6" to "Blue",
        "#0EA5E9" to "Sky",
        "#10B981" to "Emerald",
        "#EAB308" to "Yellow",
        "#8B5CF6" to "Purple",
        "#6366F1" to "Indigo",
        "#EF4444" to "Red",
        "#14B8A6" to "Teal",
        "#F43F5E" to "Rose",
        "#64748B" to "Slate"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji / Icon Selector with Keyboard Emoji Input
                Text(
                    text = "Category Icon (Emoji)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Text(
                    text = "Type any emoji from your keyboard or select a preset below:",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF2C2420) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF43281C) else Color(0xFFCBD5E1)),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = icon.ifBlank { "🏷️" }, fontSize = 24.sp)
                        }
                    }

                    OutlinedTextField(
                        value = icon,
                        onValueChange = { input ->
                            icon = input.take(4).ifBlank { "🏷️" }
                        },
                        label = { Text("Type Keyboard Emoji") },
                        placeholder = { Text("e.g. 🍕, 🥗, 🍰") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetIcons.forEach { em ->
                        val isSelected = icon == em
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { icon = em },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = em, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Default Theme Color
                Text(
                    text = "Default Theme Color",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Text(
                    text = "Events created under this category will automatically adopt this theme color, automating your visual color-coding.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Show selected color preview and hex code
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(colorHex))
                    } catch (_: Exception) {
                        Color(0xFF3B82F6)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(2.dp, if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFFCBD5E1), CircleShape)
                    )
                    OutlinedTextField(
                        value = colorHex,
                        onValueChange = { input ->
                            if (input.startsWith("#") || input.all { it.isLetterOrDigit() }) {
                                colorHex = if (!input.startsWith("#") && input.isNotBlank()) "#$input" else input
                            }
                        },
                        label = { Text("Hex Code") },
                        placeholder = { Text("#3B82F6") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetColors.forEach { (hex, _) ->
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            Color.Blue
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multiple Custom Expiry Reminders (Requirement 4 & User Request)
                Text(
                    text = "Custom Expiry Reminders",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Text(
                    text = "Set multiple reminder alerts before expiry. Toggle preset options or add custom days below.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Presets multi-select
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1 to "1 Day",
                        2 to "2 Days",
                        3 to "3 Days",
                        5 to "5 Days",
                        7 to "7 Days",
                        14 to "14 Days",
                        30 to "30 Days"
                    ).forEach { (days, label) ->
                        val isSelected = selectedReminders.contains(days)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedReminders = if (isSelected) {
                                    selectedReminders - days
                                } else {
                                    selectedReminders + days
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add Custom Reminder Day - Placed VERTICALLY for ample horizontal space
                Text(
                    text = "Add Custom Day Reminder:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = customDaysInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            customDaysInput = input
                        }
                    },
                    label = { Text("Days before expiry (e.g. 10)") },
                    placeholder = { Text("Enter number of days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val parsed = customDaysInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            selectedReminders = selectedReminders + parsed
                            customDaysInput = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Custom Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Selected Reminders list
                if (selectedReminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Active Reminders: (${selectedReminders.size} scheduled)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF2563EB)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedReminders.sorted().forEach { days ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "$days days before",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                    )
                                    IconButton(
                                        onClick = { selectedReminders = selectedReminders - days },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove reminder",
                                            tint = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No early reminders set. Select preset chips or add custom days above.",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, icon, colorHex, selectedReminders.sorted()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text(if (isEditing) "Save Changes" else "Add Category", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
