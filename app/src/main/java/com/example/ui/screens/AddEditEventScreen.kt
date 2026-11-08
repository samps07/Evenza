package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.AiExpiryResult
import com.example.data.EventEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEventScreen(
    editingEvent: EventEntity?,
    aiDraftResult: AiExpiryResult? = null,
    onSave: (
        id: Long,
        title: String,
        description: String,
        startTimeMillis: Long,
        targetTimeMillis: Long,
        icon: String,
        colorHex: String,
        category: String,
        priority: Int,
        notify1Day: Boolean,
        notify6Hours: Boolean,
        notify1Hour: Boolean,
        notifyExpiry: Boolean,
        linkUrl: String,
        imageUrl: String,
        customRemindersJson: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }
    val isDark = isSystemInDarkTheme()

    BackHandler(onBack = onDismiss)

    var title by remember { mutableStateOf(editingEvent?.title ?: (aiDraftResult?.productName ?: "")) }
    var description by remember {
        mutableStateOf(
            editingEvent?.description ?: (
                buildString {
                    val mainNotes = listOf(aiDraftResult?.quantity, aiDraftResult?.notes).filter { !it.isNullOrBlank() }.joinToString(" • ")
                    if (mainNotes.isNotBlank()) append(mainNotes)
                    if (!aiDraftResult?.rawOcrText.isNullOrBlank()) {
                        if (isNotEmpty()) append("\n\n")
                        append("Extracted Text:\n${aiDraftResult?.rawOcrText}")
                    }
                }
            )
        )
    }
    var category by remember { mutableStateOf(editingEvent?.category ?: (aiDraftResult?.category ?: "General")) }
    val initialCat = remember(category) { com.example.data.CategoryManager.getCategoryByName(context, category) }
    var icon by remember { mutableStateOf(editingEvent?.icon ?: initialCat.icon) }
    var colorHex by remember { mutableStateOf(editingEvent?.colorHex ?: initialCat.colorHex) }
    var linkUrl by remember { mutableStateOf(editingEvent?.linkUrl ?: "") }
    var imageUrl by remember { mutableStateOf(editingEvent?.imageUrl ?: (aiDraftResult?.imagePath ?: "")) }

    var startTimeMillis by remember {
        mutableLongStateOf(editingEvent?.startTimeMillis ?: now)
    }
    var targetTimeMillis by remember {
        mutableLongStateOf(editingEvent?.targetTimeMillis ?: (aiDraftResult?.expiryEpochMillis ?: (now + 3 * 24 * 3600 * 1000L)))
    }

    val initialEventReminders = remember {
        if (editingEvent != null && editingEvent.customRemindersJson.isNotBlank()) {
            com.example.notifications.NotificationScheduler.parseCustomReminders(editingEvent.customRemindersJson).toSet()
        } else {
            initialCat.customReminders.toSet()
        }
    }
    var selectedReminders by remember { mutableStateOf(initialEventReminders) }
    var customDaysInput by remember { mutableStateOf("") }

    var notify1Day by remember { mutableStateOf(editingEvent?.notify1DayBefore ?: (initialCat.notifyDaysBefore > 0)) }
    var notify6Hours by remember { mutableStateOf(editingEvent?.notify6HoursBefore ?: false) }
    var notify1Hour by remember { mutableStateOf(editingEvent?.notify1HourBefore ?: false) }
    var notifyExpiry by remember { mutableStateOf(editingEvent?.notifyAtExpiry ?: true) }

    var titleError by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    // Photo picker launcher (zero permission, Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val savedPath = com.example.util.ImageStorageHelper.saveUriToInternalStorage(context, uri)
                imageUrl = savedPath ?: uri.toString()
            }
        }
    )

    val emojiPresets = remember {
        listOf("🍔", "🎬", "📚", "🎁", "🚆", "🚗", "💳", "🏷️", "💊", "🛡️", "✈️", "☕", "🎟️", "🔑", "🏠", "🎂", "⏰", "💼")
    }

    val colorPresets = remember {
        listOf("#F97316", "#EC4899", "#3B82F6", "#0EA5E9", "#10B981", "#EAB308", "#8B5CF6", "#6366F1", "#EF4444", "#14B8A6", "#F43F5E", "#64748B")
    }

    val categoryItems = remember { com.example.data.CategoryManager.getCategories(context) }
    val categoryPresets = remember(categoryItems) { categoryItems.map { it.name } }

    val canvasBg = if (isDark) Color(0xFF141211) else Color(0xFFFDF8F6)
    val cardBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingEvent == null) "Create counter" else "Edit counter",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(
                                    editingEvent?.id ?: 0L,
                                    title,
                                    description,
                                    startTimeMillis,
                                    targetTimeMillis,
                                    icon,
                                    colorHex,
                                    category,
                                    editingEvent?.priority ?: 1,
                                    notify1Day,
                                    notify6Hours,
                                    notify1Hour,
                                    notifyExpiry,
                                    linkUrl,
                                    imageUrl,
                                    org.json.JSONArray(selectedReminders.sorted()).toString()
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD3E4FF),
                            contentColor = Color(0xFF1E3A8A)
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_counter_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // AI-Extracted Expiry Details Review Banner
            if (aiDraftResult != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF3B1E38) else Color(0xFFFDF2F8)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (aiDraftResult.needsVerification) Color(0xFFFB923C)
                        else if (isDark) Color(0xFF6B21A8).copy(alpha = 0.5f) else Color(0xFFFBCFE8)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFEC4899),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Expiry Analysis",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFFFCE7F3) else Color(0xFF831843)
                                )
                            }

                            val confColor = when (aiDraftResult.confidence.lowercase()) {
                                "high" -> Color(0xFF10B981)
                                "low" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = confColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, confColor.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "${aiDraftResult.confidence.uppercase()} CONFIDENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = confColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (aiDraftResult.needsVerification) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF43281C) else Color(0xFFFFF7ED),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5))
                            ) {
                                Text(
                                    text = "⚠️ Verification Needed: " + (if (aiDraftResult.verificationReason.isNotBlank()) aiDraftResult.verificationReason else "Please verify product name and expiry date before saving."),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                    color = if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Product details extracted from your photo. You can verify and modify any field before saving.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        )

                        if (aiDraftResult.rawOcrText.isNotBlank()) {
                            var showOcrText by remember { mutableStateOf(false) }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showOcrText = !showOcrText }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "📄 View Extracted Text (${aiDraftResult.rawOcrText.length} chars)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                        Icon(
                                            imageVector = if (showOcrText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    AnimatedVisibility(visible = showOcrText) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 160.dp)
                                                .verticalScroll(rememberScrollState())
                                                .padding(top = 6.dp)
                                        ) {
                                            Text(
                                                text = aiDraftResult.rawOcrText,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main Details Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TITLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError && it.isNotBlank()) titleError = false
                        },
                        placeholder = { Text("e.g. Food Coupon, Movie, Library Book") },
                        isError = titleError,
                        supportingText = if (titleError) {
                            { Text("Title cannot be empty") }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                            focusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("counter_title_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "WHEN DOES IT EXPIRE?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = targetTimeMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        cal.set(Calendar.YEAR, y)
                                        cal.set(Calendar.MONTH, m)
                                        cal.set(Calendar.DAY_OF_MONTH, d)
                                        targetTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_expiry_date_button")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(Date(targetTimeMillis)))
                        }

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = targetTimeMillis }
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        cal.set(Calendar.HOUR_OF_DAY, h)
                                        cal.set(Calendar.MINUTE, min)
                                        cal.set(Calendar.SECOND, 0)
                                        targetTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_expiry_time_button")
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeFormat.format(Date(targetTimeMillis)))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "START DATE & TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                        TextButton(
                            onClick = { startTimeMillis = System.currentTimeMillis() },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Set to now", fontSize = 12.sp, color = Color(0xFF2563EB))
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        cal.set(Calendar.YEAR, y)
                                        cal.set(Calendar.MONTH, m)
                                        cal.set(Calendar.DAY_OF_MONTH, d)
                                        startTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_start_date_button")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(Date(startTimeMillis)))
                        }

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        cal.set(Calendar.HOUR_OF_DAY, h)
                                        cal.set(Calendar.MINUTE, min)
                                        cal.set(Calendar.SECOND, 0)
                                        startTimeMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_start_time_button")
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeFormat.format(Date(startTimeMillis)))
                        }
                    }

                    // Notice if start time is in the future
                    if (startTimeMillis > System.currentTimeMillis()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF2E2416) else Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("⏳", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Starts in future: will show 'Yet to start' in widget until then",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style & Categorization Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CHOOSE ICON",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Custom keyboard emoji input
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
                                Text(text = icon.ifBlank { "⏳" }, fontSize = 24.sp)
                            }
                        }

                        OutlinedTextField(
                            value = icon,
                            onValueChange = { input ->
                                icon = input.take(4).ifBlank { "⏳" }
                            },
                            label = { Text("Type keyboard emoji") },
                            placeholder = { Text("e.g. 🎯, 🚀, 💡, 🍣") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("custom_emoji_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Or choose a preset:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        emojiPresets.forEach { emoji ->
                            val isSelected = icon == emoji
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) (if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE))
                                else (if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0))
                                ),
                                modifier = Modifier
                                    .clickable { icon = emoji }
                                    .testTag("emoji_preset_$emoji")
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoryItems.forEach { catItem ->
                            val isSelected = category.equals(catItem.name, ignoreCase = true)
                            val catThemeColor = remember(catItem.colorHex) {
                                try { Color(android.graphics.Color.parseColor(catItem.colorHex)) } catch (_: Exception) { Color(0xFF3B82F6) }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF2563EB)
                                else (if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0))
                                ),
                                modifier = Modifier.clickable {
                                    category = catItem.name
                                    colorHex = catItem.colorHex
                                    icon = catItem.icon
                                    notify1Day = catItem.notifyDaysBefore > 0
                                    selectedReminders = catItem.customReminders.toSet()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = catItem.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = catItem.name,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else catThemeColor)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "THEME COLOR (Auto from $category)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val activePresets = remember(colorHex, colorPresets) {
                        if (colorPresets.any { it.equals(colorHex, ignoreCase = true) }) colorPresets
                        else listOf(colorHex) + colorPresets
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activePresets.forEach { hex ->
                            val c = remember(hex) {
                                try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFF3B82F6) }
                            }
                            val isSelected = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, if (isDark) Color.White else Color(0xFF1E293B), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { colorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val hasAiOcrText = remember(aiDraftResult?.rawOcrText) {
                        !aiDraftResult?.rawOcrText.isNullOrBlank()
                    }
                    var extractedTextValue by remember(aiDraftResult?.rawOcrText) {
                        mutableStateOf(aiDraftResult?.rawOcrText ?: "")
                    }

                    Text(
                        text = "DESCRIPTION (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Details or notes about this deadline") },
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                            focusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Extracted Text field from AI analysis
                    if (hasAiOcrText) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF1F242D) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2B384E) else Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EXTRACTED TEXT (IMAGE OCR)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = Color(0xFF2563EB)
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            if (extractedTextValue.isNotBlank()) {
                                                val marker = "Extracted Text:\n"
                                                val updatedDesc = if (description.contains(marker)) {
                                                    description.substringBefore(marker).trimEnd() + "\n\n" + marker + extractedTextValue
                                                } else if (description.isNotBlank()) {
                                                    description.trimEnd() + "\n\n" + marker + extractedTextValue
                                                } else {
                                                    marker + extractedTextValue
                                                }
                                                description = updatedDesc.trim()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "Sync to Description",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                }

                                Text(
                                    text = "Raw text detected on the product photo. Saved automatically inside description to preserve any details AI missed.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                OutlinedTextField(
                                    value = extractedTextValue,
                                    onValueChange = { newText ->
                                        extractedTextValue = newText
                                        val marker = "Extracted Text:\n"
                                        val updatedDesc = if (description.contains(marker)) {
                                            description.substringBefore(marker).trimEnd() + "\n\n" + marker + newText
                                        } else if (description.isNotBlank()) {
                                            description.trimEnd() + "\n\n" + marker + newText
                                        } else {
                                            marker + newText
                                        }
                                        description = updatedDesc.trim()
                                    },
                                    placeholder = { Text("Extracted text from image") },
                                    maxLines = 6,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                        focusedContainerColor = if (isDark) Color(0xFF141A23) else Color(0xFFFFFFFF),
                                        unfocusedContainerColor = if (isDark) Color(0xFF141A23) else Color(0xFFFFFFFF)
                                    ),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attachments Bento Card (Link & Image)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ATTACHMENTS (IN-APP ONLY)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Links and images are stored for detail viewing and won't crowd the home-screen widget.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Link URL
                    Text(
                        text = "LINK / WEB ADDRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        placeholder = { Text("https://example.com/receipt-or-tickets") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (linkUrl.isNotBlank()) {
                                IconButton(onClick = { linkUrl = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                            focusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("event_link_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Image Attachment
                    Text(
                        text = "PHOTO / RECEIPT / IMAGE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (imageUrl.isNotBlank()) {
                        // Image Preview Card with Delete
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Attached image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f).testTag("pick_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (imageUrl.isBlank()) "Choose Photo" else "Change Photo")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        placeholder = { Text("Or paste image web URL (https://...)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (imageUrl.isNotBlank()) {
                                IconButton(onClick = { imageUrl = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                            focusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("event_image_url_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Bento Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NOTIFICATIONS & DUE-IN ALERTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CUSTOM DAYS BEFORE EXPIRY (${category.uppercase()})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set multiple reminder alerts before expiry. Toggle preset options or add custom days below:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chips multi-select
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
                                    if (days == 1) {
                                        notify1Day = !isSelected
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

                    // Add Custom Days Reminder - Placed VERTICALLY for ample horizontal space
                    Text(
                        text = "Add Custom Day Reminder:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = Color(0xFF64748B)
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_custom_days_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val parsed = customDaysInput.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                selectedReminders = selectedReminders + parsed
                                if (parsed == 1) notify1Day = true
                                customDaysInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth().testTag("add_custom_days_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Custom Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Active scheduled reminders
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
                                            onClick = {
                                                selectedReminders = selectedReminders - days
                                                if (days == 1) notify1Day = false
                                            },
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
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ADDITIONAL TIMED ALERTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notify6Hours = !notify6Hours }
                    ) {
                        Checkbox(
                            checked = notify6Hours,
                            onCheckedChange = { notify6Hours = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                        Text("6 hours before", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notify1Hour = !notify1Hour }
                    ) {
                        Checkbox(
                            checked = notify1Hour,
                            onCheckedChange = { notify1Hour = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                        Text("1 hour before", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyExpiry = !notifyExpiry }
                    ) {
                        Checkbox(
                            checked = notifyExpiry,
                            onCheckedChange = { notifyExpiry = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                        Text("At expiry", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
