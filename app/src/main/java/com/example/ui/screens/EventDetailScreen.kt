package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.EventEntity
import com.example.domain.ProgressCalculator
import com.example.domain.UrgencyLevel
import com.example.notifications.EventNotificationManager
import com.example.ui.components.VideoPlayerSeekBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventDetailScreen(
    event: EventEntity,
    nowMillis: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onRestart: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onPinNotification: (() -> Unit)? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isImageEnlarged by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    val calc = remember(event, nowMillis) {
        ProgressCalculator.calculate(event, nowMillis)
    }

    val accentColor = remember(event.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(event.colorHex))
        } catch (_: Exception) {
            Color(0xFF2563EB)
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    val canvasBg = if (isDark) Color(0xFF141211) else Color(0xFFFDF8F6)
    val cardBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Counter Details",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (onPinNotification != null) onPinNotification()
                            else EventNotificationManager.showPinnedNotification(context, event)
                        },
                        modifier = Modifier.testTag("pin_notif_button_detail")
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Pin to notification",
                            tint = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                        )
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bento Main Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Emoji Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.icon.ifEmpty { "⏳" },
                            fontSize = 40.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    SelectionContainer {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Category badge
                    if (event.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF282321) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF382F2C) else Color(0xFFDBEAFE)),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = event.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big Bento Countdown Headline
                    Text(
                        text = calc.formattedRemaining,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = when (calc.urgencyLevel) {
                                UrgencyLevel.EXPIRED -> Color(0xFF94A3B8)
                                UrgencyLevel.CRITICAL -> Color(0xFFEA580C)
                                UrgencyLevel.WARNING -> Color(0xFFF59E0B)
                                UrgencyLevel.COMPLETED -> Color(0xFF059669)
                                UrgencyLevel.YET_TO_START -> Color(0xFF6366F1)
                                UrgencyLevel.NORMAL -> Color(0xFF2563EB)
                            }
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = calc.exactRemainingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Video Player Seek Bar
                    VideoPlayerSeekBar(
                        progress = calc.progress,
                        accentColor = accentColor,
                        urgencyLevel = calc.urgencyLevel,
                        height = 20.dp,
                        trackHeight = 8.dp,
                        showThumb = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${calc.percentText} elapsed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Started and Expires Bento Boxes placed vertically for ample horizontal space
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STARTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateFormat.format(Date(event.startTimeMillis)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EXPIRES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateFormat.format(Date(event.targetTimeMillis)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }

            // Scheduled Alerts & Custom Period Reminders Bento Box
            val parsedCustomReminders = remember(event.customRemindersJson, event.category) {
                val list = com.example.notifications.NotificationScheduler.parseCustomReminders(event.customRemindersJson)
                if (list.isNotEmpty()) list
                else {
                    val cat = com.example.data.CategoryManager.getCategoryByName(context, event.category)
                    cat.customReminders
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SCHEDULED ALERTS & REMINDERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF2563EB)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (parsedCustomReminders.isNotEmpty() || event.notify6HoursBefore || event.notify1HourBefore || event.notifyAtExpiry) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedCustomReminders.sorted().forEach { days ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6))
                                ) {
                                    Text(
                                        text = "⏰ $days days before",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (event.notify6HoursBefore) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF2C2420) else Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = "⏰ 6h before",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (event.notify1HourBefore) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF2C2420) else Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = "⏰ 1h before",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (event.notifyAtExpiry) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF132D24) else Color(0xFFD1FAE5),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF065F46) else Color(0xFFA7F3D0))
                                ) {
                                    Text(
                                        text = "🔔 At expiry",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No custom notification alerts enabled for this event.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Description Bento Box if present
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SelectionContainer {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                                )
                            )
                        }
                    }
                }
            }

            // Attached Link Bento Box
            if (event.linkUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val url = if (!event.linkUrl.startsWith("http://") && !event.linkUrl.startsWith("https://")) {
                                    "https://${event.linkUrl}"
                                } else {
                                    event.linkUrl
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ATTACHED LINK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            SelectionContainer {
                                Text(
                                    text = event.linkUrl,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open link",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Attached Image Bento Box
            if (event.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "ATTACHED IMAGE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isImageEnlarged = true }
                        ) {
                            AsyncImage(
                                model = if (event.imageUrl.startsWith("/")) java.io.File(event.imageUrl) else event.imageUrl,
                                contentDescription = "Attached photo or receipt (tap to enlarge)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggleCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (event.isCompleted) Color(0xFF2563EB) else Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (event.isCompleted) Icons.Outlined.CheckCircleOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (event.isCompleted) "Mark Active" else "Mark Complete",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pin to persistent notification shade button
                FilledTonalButton(
                    onClick = {
                        if (onPinNotification != null) onPinNotification()
                        else EventNotificationManager.showPinnedNotification(context, event)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_notif_button_detail_main"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                        contentColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pin Note to Notifications", fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDark) Color(0xFF282321) else Color(0xFFE2E8F0),
                        contentColor = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart Counter", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onDuplicate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDark) Color(0xFF282321) else Color(0xFFE2E8F0),
                        contentColor = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Duplicate Counter", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_counter_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete counter", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Counter", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${event.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = cardBg
        )
    }

    if (isImageEnlarged && event.imageUrl.isNotBlank()) {
        com.example.ui.components.ImageViewerDialog(
            imageUrl = event.imageUrl,
            onDismiss = { isImageEnlarged = false }
        )
    }
}
