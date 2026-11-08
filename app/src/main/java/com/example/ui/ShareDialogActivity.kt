package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.ai.AiExpiryResult
import com.example.ai.OcrConfig
import com.example.ai.OcrHelper
import com.example.ai.OpenRouterExpiryService
import com.example.data.AppDatabase
import com.example.data.CategoryManager
import com.example.data.EventEntity
import com.example.notifications.NotificationScheduler
import com.example.ui.components.ImageViewerDialog
import com.example.ui.theme.EvenzaTheme
import com.example.util.ImageStorageHelper
import com.example.widget.CounterWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

sealed class ShareAiProcessState {
    object Idle : ShareAiProcessState()
    data class Processing(val stepText: String) : ShareAiProcessState()
    data class Success(val result: AiExpiryResult) : ShareAiProcessState()
    data class Error(val message: String) : ShareAiProcessState()
    object Cancelled : ShareAiProcessState()
}

class ShareDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var initialTitle = ""
        var initialDescription = ""
        var initialLinkUrl = ""
        var initialImagePath = ""

        // Extract shared content
        if (intent?.action == Intent.ACTION_SEND) {
            val type = intent.type

            // Shared plain text or URL
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

            if (!sharedText.isNullOrBlank()) {
                val urlPattern = Pattern.compile("https?://\\S+")
                val matcher = urlPattern.matcher(sharedText)
                if (matcher.find()) {
                    initialLinkUrl = matcher.group()
                    val textWithoutUrl = sharedText.replace(initialLinkUrl, "").trim()
                    initialTitle = when {
                        !sharedSubject.isNullOrBlank() -> sharedSubject
                        textWithoutUrl.isNotBlank() -> textWithoutUrl.take(60)
                        else -> {
                            val host = Uri.parse(initialLinkUrl).host?.removePrefix("www.") ?: "Shared Link"
                            "Link from $host"
                        }
                    }
                    if (textWithoutUrl.length > 60) {
                        initialDescription = textWithoutUrl
                    }
                } else {
                    if (!sharedSubject.isNullOrBlank()) {
                        initialTitle = sharedSubject
                        initialDescription = sharedText
                    } else if (sharedText.length > 50) {
                        initialTitle = sharedText.take(50).trim()
                        initialDescription = sharedText
                    } else {
                        initialTitle = sharedText.trim()
                    }
                }
            }

            // Shared image stream
            val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }

            if (imageUri != null && type?.startsWith("image/") == true) {
                val savedPath = ImageStorageHelper.saveImage(this, imageUri)
                if (savedPath != null) {
                    initialImagePath = savedPath
                    if (initialTitle.isBlank()) {
                        initialTitle = "Shared Label Photo"
                    }
                }
            }
        }

        val prefs = getSharedPreferences("counter_app_prefs", Context.MODE_PRIVATE)
        val uiScale = prefs.getFloat("ui_scale", 1.0f)

        setContent {
            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density * uiScale,
                fontScale = currentDensity.fontScale * uiScale
            )

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                EvenzaTheme {
                    ShareDialogScreen(
                        initialTitle = initialTitle,
                        initialDescription = initialDescription,
                        initialLinkUrl = initialLinkUrl,
                        initialImagePath = initialImagePath,
                        onDismiss = { finish() },
                        onSave = { newEvent ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val id = AppDatabase.getDatabase(this@ShareDialogActivity).eventDao().insertEvent(newEvent)
                                val inserted = newEvent.copy(id = id)
                                NotificationScheduler.scheduleReminders(this@ShareDialogActivity, inserted)
                                withContext(Dispatchers.Main) {
                                    CounterWidgetProvider.updateAllWidgets(this@ShareDialogActivity)
                                    Toast.makeText(this@ShareDialogActivity, "Saved to Evenza!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareDialogScreen(
    initialTitle: String,
    initialDescription: String,
    initialLinkUrl: String,
    initialImagePath: String,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val now = System.currentTimeMillis()
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var linkUrl by remember { mutableStateOf(initialLinkUrl) }
    var imagePath by remember { mutableStateOf(initialImagePath) }
    var category by remember { mutableStateOf("General") }
    var selectedColor by remember { mutableStateOf("#38BDF8") } // Sky blue
    var targetTimeMillis by remember { mutableLongStateOf(now + (7L * 24 * 3600 * 1000)) } // default +7 days
    var isImageEnlarged by remember { mutableStateOf(false) }

    // Load user configured categories
    val categoryItems = remember { CategoryManager.getCategories(context) }
    val categories = remember(categoryItems) { categoryItems.map { it.name } }
    val colors = listOf("#38BDF8", "#10B981", "#8B5CF6", "#F97316", "#EC4899", "#EAB308", "#6366F1")

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    // AI Processing state (Requirement 11)
    var aiProcessState by remember {
        mutableStateOf<ShareAiProcessState>(
            if (initialImagePath.isNotBlank()) ShareAiProcessState.Processing("Extracting label text...")
            else ShareAiProcessState.Idle
        )
    }
    var aiJob by remember { mutableStateOf<Job?>(null) }

    fun startAiProcessing() {
        if (imagePath.isBlank()) return
        aiJob?.cancel()
        aiJob = coroutineScope.launch {
            try {
                aiProcessState = ShareAiProcessState.Processing("Extracting label text with OCR...")
                val prefs = context.getSharedPreferences("counter_app_prefs", Context.MODE_PRIVATE)
                val apiKey = prefs.getString("openrouter_api_key", "")?.ifBlank {
                    context.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE).getString("openrouter_api_key", "") ?: ""
                } ?: ""
                val model = prefs.getString("openrouter_model", OpenRouterExpiryService.DEFAULT_MODEL) ?: OpenRouterExpiryService.DEFAULT_MODEL
                val baseUrl = prefs.getString("openrouter_base_url", OpenRouterExpiryService.DEFAULT_BASE_URL) ?: OpenRouterExpiryService.DEFAULT_BASE_URL
                val customPrompt = prefs.getString("openrouter_system_prompt", prefs.getString("openrouter_prompt", OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT))

                val ocrConfig = OcrConfig()
                val ocrText = OcrHelper.extractTextFromPath(imagePath, ocrConfig)

                if (ocrText.isBlank()) {
                    aiProcessState = ShareAiProcessState.Error("No text detected on the shared image.")
                    return@launch
                }

                if (apiKey.isBlank()) {
                    // No API key configured
                    aiProcessState = ShareAiProcessState.Error("API key not configured in Settings. Using raw text.")
                    if (title == "Shared Label Photo" || title.isBlank()) {
                        title = ocrText.lines().firstOrNull { it.isNotBlank() }?.take(40) ?: "Shared Item"
                    }
                    description = ocrText.take(200)
                    return@launch
                }

                aiProcessState = ShareAiProcessState.Processing("Analyzing product & expiry with AI...")
                val (aiResult, _) = OpenRouterExpiryService().analyzeOcrText(
                    ocrText = ocrText,
                    apiKey = apiKey,
                    baseUrl = baseUrl,
                    model = model,
                    customPrompt = customPrompt,
                    availableCategories = CategoryManager.getCategories(context)
                )

                // Apply AI Result
                if (aiResult.productName.isNotBlank()) {
                    title = aiResult.productName
                }
                if (aiResult.expiryEpochMillis != null) {
                    targetTimeMillis = aiResult.expiryEpochMillis
                }
                if (aiResult.notes.isNotBlank() || aiResult.quantity.isNotBlank()) {
                    val combinedNotes = listOf(aiResult.quantity, aiResult.notes).filter { it.isNotBlank() }.joinToString(" • ")
                    description = if (description.isNotBlank()) "$description\n$combinedNotes" else combinedNotes
                }
                if (aiResult.category.isNotBlank() && categories.any { it.equals(aiResult.category, ignoreCase = true) }) {
                    category = categories.first { it.equals(aiResult.category, ignoreCase = true) }
                    val matchedCat = CategoryManager.getCategoryByName(context, category)
                    selectedColor = matchedCat.colorHex
                }

                aiProcessState = ShareAiProcessState.Success(aiResult)
            } catch (e: Exception) {
                aiProcessState = ShareAiProcessState.Error(e.localizedMessage ?: "AI analysis failed")
            }
        }
    }

    // Auto-start AI analysis if shared with an image (Requirement 11)
    LaunchedEffect(initialImagePath) {
        if (initialImagePath.isNotBlank()) {
            startAiProcessing()
        }
    }

    // Floating Scrim Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Dialog Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Prevent clicks inside from closing */ }
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
            ),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Evenza",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        )
                        Text(
                            text = " • Quick Add",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Image preview if attached (clickable to enlarge)
                if (imagePath.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF141211) else Color(0xFFF1F5F9))
                            .clickable { isImageEnlarged = true }
                    ) {
                        AsyncImage(
                            model = File(imagePath),
                            contentDescription = "Attached image (tap to enlarge)",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xCC000000),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clickable {
                                    imagePath = ""
                                    aiJob?.cancel()
                                    aiProcessState = ShareAiProcessState.Idle
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // AI Processing Status & Cancel/Restart Banner (Requirement 11)
                if (imagePath.isNotBlank() && aiProcessState !is ShareAiProcessState.Idle) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when (aiProcessState) {
                            is ShareAiProcessState.Processing -> if (isDark) Color(0xFF1E3A5F) else Color(0xFFEFF6FF)
                            is ShareAiProcessState.Success -> if (isDark) Color(0xFF132E20) else Color(0xFFECFDF5)
                            is ShareAiProcessState.Error -> if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED)
                            ShareAiProcessState.Cancelled -> if (isDark) Color(0xFF242220) else Color(0xFFF1F5F9)
                            ShareAiProcessState.Idle -> Color.Transparent
                        },
                        border = BorderStroke(
                            1.dp,
                            when (aiProcessState) {
                                is ShareAiProcessState.Processing -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                                is ShareAiProcessState.Success -> Color(0xFF10B981).copy(alpha = 0.5f)
                                is ShareAiProcessState.Error -> Color(0xFFF97316).copy(alpha = 0.5f)
                                else -> Color(0xFF94A3B8).copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (val state = aiProcessState) {
                                    is ShareAiProcessState.Processing -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF2563EB)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = state.stepText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = Color(0xFF1D4ED8),
                                            maxLines = 1
                                        )
                                    }
                                    is ShareAiProcessState.Success -> {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI Extracted details applied!",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF047857)
                                        )
                                    }
                                    is ShareAiProcessState.Error -> {
                                        Text(
                                            text = "AI: ${state.message}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFFC2410C),
                                            maxLines = 1
                                        )
                                    }
                                    ShareAiProcessState.Cancelled -> {
                                        Text(
                                            text = "AI processing cancelled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    ShareAiProcessState.Idle -> {}
                                }
                            }

                            // Action button: Cancel or Restart (Requirement 11)
                            when (aiProcessState) {
                                is ShareAiProcessState.Processing -> {
                                    TextButton(
                                        onClick = {
                                            aiJob?.cancel()
                                            aiProcessState = ShareAiProcessState.Cancelled
                                        }
                                    ) {
                                        Text("Cancel", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                }
                                is ShareAiProcessState.Success, is ShareAiProcessState.Error, ShareAiProcessState.Cancelled -> {
                                    TextButton(
                                        onClick = { startAiProcessing() }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF2563EB))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Restart AI", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                ShareAiProcessState.Idle -> {}
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("What are you tracking?") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF38312E) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description field (Permanently visible, Requirement 11)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    placeholder = { Text("Add notes, batch number, or reminders") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF38312E) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Link URL field
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("Link URL (optional)") },
                    placeholder = { Text("https://...") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF38312E) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Date Presets
                Text(
                    text = "Due In",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val presets = listOf(
                    "Tomorrow" to (24L * 3600 * 1000),
                    "3 Days" to (3L * 24 * 3600 * 1000),
                    "1 Week" to (7L * 24 * 3600 * 1000),
                    "1 Month" to (30L * 24 * 3600 * 1000)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (label, duration) ->
                        val isPresetSelected = targetTimeMillis == now + duration
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPresetSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF282321) else Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetTimeMillis = now + duration }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isPresetSelected) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Target: ${dateFormat.format(Date(targetTimeMillis))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection (Using dynamic CategoryManager list)
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = category.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                category = cat
                                val matched = CategoryManager.getCategoryByName(context, cat)
                                selectedColor = matched.colorHex
                            },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Translucent Color Picker dots
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { hex ->
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                    )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifEmpty { "Shared Event" }
                            val newEvent = EventEntity(
                                title = finalTitle,
                                description = description.trim(),
                                startTimeMillis = now,
                                targetTimeMillis = targetTimeMillis,
                                icon = CategoryManager.getCategoryByName(context, category).icon,
                                colorHex = selectedColor,
                                category = category,
                                linkUrl = linkUrl.trim(),
                                imageUrl = imagePath,
                                notify1DayBefore = true,
                                notifyAtExpiry = true
                            )
                            onSave(newEvent)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Save Event", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (isImageEnlarged && imagePath.isNotBlank()) {
        ImageViewerDialog(
            imageUrl = imagePath,
            onDismiss = { isImageEnlarged = false }
        )
    }
}
