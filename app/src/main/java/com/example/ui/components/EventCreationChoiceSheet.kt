package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CreationSheetStep {
    CHOOSE_TYPE,
    AI_IMAGE_SOURCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreationChoiceSheet(
    isApiKeyConfigured: Boolean,
    onDismiss: () -> Unit,
    onChooseManual: () -> Unit,
    onChooseCamera: () -> Unit,
    onChooseGallery: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentStep by remember { mutableStateOf(CreationSheetStep.CHOOSE_TYPE) }
    val isDark = isSystemInDarkTheme()

    val sheetBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0)

    val currentDensity = androidx.compose.ui.platform.LocalDensity.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = if (isDark) Color(0xFF4A403B) else Color(0xFFCBD5E1),
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalDensity provides currentDensity
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step_transition"
            ) { step ->
                when (step) {
                    CreationSheetStep.CHOOSE_TYPE -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Create Counter",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                            )
                            Text(
                                text = "Choose how you would like to track this event or product",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Manual and AI options side-by-side without descriptions (Requirement 4)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Manual Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = onChooseManual),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.EditCalendar,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Manual",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                                        )
                                    }
                                }

                                // AI Expiry Tracker Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = { currentStep = CreationSheetStep.AI_IMAGE_SOURCE }),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(if (isDark) Color(0xFF3B1E38) else Color(0xFFFCE7F3)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFFF472B6) else Color(0xFFBE185D),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "AI Expiry",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CreationSheetStep.AI_IMAGE_SOURCE -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = { currentStep = CreationSheetStep.CHOOSE_TYPE },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "AI Expiry Scanner",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Select product label photo to analyze",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            if (!isApiKeyConfigured) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED)
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF43281C) else Color(0xFFFFEDD5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Key,
                                            contentDescription = null,
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "API Key Required",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C)
                                            )
                                            Text(
                                                text = "Please set your OpenRouter or OpenAI API key in Settings first.",
                                                fontSize = 11.sp,
                                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                            )
                                        }
                                        TextButton(onClick = onOpenSettings) {
                                            Text("Settings", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Take Photo
                            ChoiceItemCard(
                                icon = Icons.Outlined.PhotoCamera,
                                iconBg = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF),
                                iconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                title = "Take Photo",
                                description = "Snap a picture of the product label and expiry stamp",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                isDark = isDark,
                                onClick = onChooseCamera
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Choose from Gallery
                            ChoiceItemCard(
                                icon = Icons.Outlined.PhotoLibrary,
                                iconBg = if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5),
                                iconTint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                title = "Choose from Gallery",
                                description = "Select an existing photo or screenshot from your library",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                isDark = isDark,
                                onClick = onChooseGallery
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ChoiceItemCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFF2563EB),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                    )

                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
