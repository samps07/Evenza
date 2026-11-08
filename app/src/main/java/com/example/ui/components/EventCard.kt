package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.EventEntity
import com.example.domain.EventProgress
import com.example.domain.UrgencyLevel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventCard(
    event: EventEntity,
    progress: EventProgress,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onRestart: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onPinNotification: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    isCustomOrder: Boolean = false,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val hasContent = remember(event.description, event.imageUrl, event.linkUrl) {
        event.description.isNotBlank() || event.imageUrl.isNotBlank() || event.linkUrl.isNotBlank()
    }
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val customAccentColor = remember(event.colorHex) {
        try {
            if (event.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(event.colorHex)) else null
        } catch (_: Exception) {
            null
        }
    }

    // Bento Grid Card Color System
    val (cardBg, cardBorder, subtextColor, bigNumberColor, trackColor, activeProgressColor) = when {
        progress.isCompleted -> {
            HexBundle(
                bg = if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5),
                border = if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0),
                subtext = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                bigNumber = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                track = if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0).copy(alpha = 0.6f),
                active = Color(0xFF10B981)
            )
        }
        progress.isExpired -> {
            HexBundle(
                bg = if (isDark) Color(0xFF211E1D) else Color(0xFFF1F5F9),
                border = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                subtext = Color(0xFF94A3B8),
                bigNumber = Color(0xFF94A3B8),
                track = Color(0xFFCBD5E1),
                active = Color(0xFF94A3B8)
            )
        }
        customAccentColor != null -> {
            // User-chosen accent color: stylish translucent background tint
            val translucentBg = customAccentColor.copy(alpha = if (isDark) 0.16f else 0.10f)
            val translucentBorder = customAccentColor.copy(alpha = if (isDark) 0.38f else 0.28f)
            HexBundle(
                bg = translucentBg,
                border = translucentBorder,
                subtext = customAccentColor,
                bigNumber = customAccentColor,
                track = customAccentColor.copy(alpha = if (isDark) 0.25f else 0.18f),
                active = customAccentColor
            )
        }
        progress.urgencyLevel == UrgencyLevel.CRITICAL || (progress.remainingMillis in 1..86400_000L) -> {
            // Orange Bento Box (Urgent / Expiring Very Soon)
            HexBundle(
                bg = if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED), // bg-orange-50
                border = if (isDark) Color(0xFF43281C) else Color(0xFFFFEDD5), // border-orange-100
                subtext = if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C), // text-orange-700
                bigNumber = if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C), // text-orange-600
                track = if (isDark) Color(0xFF43281C) else Color(0xFFFED7AA).copy(alpha = 0.6f),
                active = Color(0xFFF97316) // bg-orange-500
            )
        }
        progress.urgencyLevel == UrgencyLevel.WARNING || (progress.remainingMillis in 86400_001L..(7 * 86400_000L)) -> {
            // Blue Bento Box (Medium / Due Soon)
            HexBundle(
                bg = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF), // bg-blue-50
                border = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE), // border-blue-100
                subtext = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8), // text-blue-700
                bigNumber = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB), // text-blue-600
                track = if (isDark) Color(0xFF1E3A5F) else Color(0xFFBFDBFE).copy(alpha = 0.6f),
                active = Color(0xFF3B82F6) // bg-blue-500
            )
        }
        else -> {
            // Neutral White Bento Box (Calm / Long Term)
            HexBundle(
                bg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                border = if (isDark) Color(0xFF2E2724) else Color(0xFFF1F5F9),
                subtext = Color(0xFF64748B),
                bigNumber = Color(0xFF64748B),
                track = if (isDark) Color(0xFF2E2724) else Color(0xFFF1F5F9),
                active = Color(0xFF94A3B8)
            )
        }
    }

    // Short big number text (e.g. "4h", "3d", "18d", "2m")
    val bigNumberText = remember(progress.remainingMillis, progress.isExpired, progress.isCompleted) {
        when {
            progress.isCompleted -> "✓"
            progress.isExpired -> "0d"
            progress.remainingMillis < 3600_000L -> "${(progress.remainingMillis / 60_000L).coerceAtLeast(1)}m"
            progress.remainingMillis < 86400_000L -> "${progress.remainingMillis / 3600_000L}h"
            else -> "${progress.remainingMillis / 86400_000L}d"
        }
    }

    // Uppercase status label (e.g. "EXPIRING VERY SOON", "DUE IN 3 DAYS", "18 DAYS LEFT")
    val statusLabel = remember(progress.urgencyLevel, progress.remainingMillis, progress.isExpired, progress.isCompleted, event.category) {
        when {
            progress.isCompleted -> "COMPLETED"
            progress.isExpired -> "EXPIRED"
            progress.urgencyLevel == UrgencyLevel.CRITICAL || progress.remainingMillis < 86400_000L -> "EXPIRING VERY SOON"
            progress.remainingMillis in 86400_000L..(7 * 86400_000L) -> "DUE IN ${progress.remainingMillis / 86400_000L} DAYS"
            else -> "${progress.remainingMillis / 86400_000L} DAYS LEFT"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
            .then(if (progress.isExpired) Modifier.alpha(0.65f) else Modifier)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp), // rounded-3xl
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark || progress.isExpired) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // p-5
        ) {
            // Top Section: Title & Icon on Left, Options Menu (+ reorder if custom order) on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Icon taking full available horizontal space
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.icon.ifEmpty { "⏳" },
                        fontSize = 22.sp
                    )
                    SelectionContainer(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                            ),
                            maxLines = if (isExpanded) 4 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Custom order move buttons & More Menu at the right edge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (isCustomOrder) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = canMoveUp,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move up",
                                tint = if (canMoveUp) subtextColor else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = canMoveDown,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move down",
                                tint = if (canMoveDown) subtextColor else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pin to Notification") },
                                leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                                onClick = { showMenu = false; onPinNotification() }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text(if (event.isCompleted) "Mark Active" else "Mark Completed") },
                                leadingIcon = {
                                    Icon(
                                        if (event.isCompleted) Icons.Outlined.CheckCircleOutline else Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                },
                                onClick = { showMenu = false; onToggleCompleted() }
                            )
                            DropdownMenuItem(
                                text = { Text("Restart counter") },
                                leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                                onClick = { showMenu = false; onRestart() }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                onClick = { showMenu = false; onDuplicate() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-row: Days Left statement with Push Notification & Dropdown Toggle placed beside it
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 11.sp
                    ),
                    color = subtextColor
                )

                // Push/Pin to persistent notification button
                IconButton(
                    onClick = onPinNotification,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("pin_notif_button_${event.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Pin to notification",
                        tint = subtextColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Dropdown Toggle (shown when event has description, image, or url)
                if (hasContent) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse preview" else "Expand preview",
                            tint = subtextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Dropdown Content Preview (Image, URL, Selectable Text) shown when expanded
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    // Selectable description for easy copying (coupon codes, notes)
                    if (event.description.isNotBlank()) {
                        SelectionContainer {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }
                    }

                    // Attached Image preview if present (clickable to enlarge)
                    if (event.imageUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onImageClick(event.imageUrl) }
                        ) {
                            AsyncImage(
                                model = if (event.imageUrl.startsWith("/")) File(event.imageUrl) else event.imageUrl,
                                contentDescription = "Event image (tap to enlarge)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Attached URL preview (tappable directly & selectable)
                    if (event.linkUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF132338).copy(alpha = 0.85f) else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)),
                            modifier = Modifier.clickable {
                                try {
                                    val url = if (!event.linkUrl.startsWith("http://") && !event.linkUrl.startsWith("https://")) {
                                        "https://${event.linkUrl}"
                                    } else {
                                        event.linkUrl
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                SelectionContainer(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = event.linkUrl,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bento Video-Player Seek Bar
            VideoPlayerSeekBar(
                progress = progress.progress,
                accentColor = activeProgressColor,
                urgencyLevel = progress.urgencyLevel,
                trackColorOverride = trackColor,
                activeColorOverride = activeProgressColor,
                height = 18.dp,
                trackHeight = 8.dp,
                showThumb = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Row: Sept 1 ... 88% ... Sept 10
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(event.startTimeMillis)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = progress.percentText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = subtextColor
                )

                Text(
                    text = dateFormat.format(Date(event.targetTimeMillis)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private data class HexBundle(
    val bg: Color,
    val border: Color,
    val subtext: Color,
    val bigNumber: Color,
    val track: Color,
    val active: Color
)
