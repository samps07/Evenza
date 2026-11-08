package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.AiAnalysisState
import com.example.ai.AiExpiryResult
import com.example.ai.OpenRouterExpiryService
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiScanningDialog(
    state: AiAnalysisState,
    currentConfiguredModel: String = OpenRouterExpiryService.DEFAULT_MODEL,
    onDismiss: () -> Unit,
    onCancelAiAnalysis: () -> Unit,
    onAnalyzeWithAi: (imagePath: String, ocrText: String, model: String) -> Unit,
    onApplyAiResult: (AiExpiryResult) -> Unit,
    onUseOcrManually: (imagePath: String, ocrText: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    if (state is AiAnalysisState.Idle || state is AiAnalysisState.Success) return

    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val currentDensity = androidx.compose.ui.platform.LocalDensity.current

    BasicAlertDialog(onDismissRequest = {
        if (state is AiAnalysisState.Error || state is AiAnalysisState.OcrExtracted || state is AiAnalysisState.ReviewResult) {
            onDismiss()
        } else if (state is AiAnalysisState.CallingAi) {
            onCancelAiAnalysis()
        }
    }) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalDensity provides currentDensity
        ) {
            Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                when (state) {
                    // ==========================================
                    // 1. Reading OCR on-device
                    // ==========================================
                    is AiAnalysisState.ReadingOcr -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF3B1E38) else Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Reading Product Label",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (state.ocrModeTitle.isNotBlank()) {
                                "Running ${state.ocrModeTitle} OCR locally on your device..."
                            } else {
                                "Extracting text locally on your device with Google ML Kit..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Show image thumbnail being processed
                        if (state.imagePath.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier.size(100.dp)
                            ) {
                                AsyncImage(
                                    model = File(state.imagePath),
                                    contentDescription = "Selected product image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFEC4899),
                            trackColor = if (isDark) Color(0xFF382F2C) else Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = textSecondary)
                        }
                    }

                    // ==========================================
                    // 2. Calling AI (Step 2 - Automated)
                    // Extracted text & image shown in front with live Cancel button!
                    // ==========================================
                    is AiAnalysisState.CallingAi -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "STEP 1: TEXT EXTRACTED ✓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF3B1E38) else Color(0xFFFCE7F3)
                            ) {
                                Text(
                                    text = "STEP 2: AI RUNNING...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEC4899),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Analyzing Expiry with AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Text was extracted and automatically sent to ${state.model.ifBlank { "AI model" }}. You can cancel anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Extracted Image & Text Card shown in front!
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, cardBorder),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        AsyncImage(
                                            model = File(state.imagePath),
                                            contentDescription = "Uploaded photo",
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Extracted Label Text (${state.charCount} chars)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "On-device OCR completed • Image not uploaded",
                                                fontSize = 10.sp,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Scrollable Extracted Text box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 60.dp, max = 180.dp)
                                            .verticalScroll(rememberScrollState())
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = state.ocrText,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            color = textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFEC4899),
                            trackColor = if (isDark) Color(0xFF382F2C) else Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Prominent Cancel AI Analysis Button!
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onCancelAiAnalysis,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2),
                                    contentColor = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancel AI Analysis", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onUseOcrManually(state.imagePath, state.ocrText) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Skip AI & Use Extracted Text", color = textPrimary)
                            }
                        }
                    }

                    // ==========================================
                    // 3. OCR Extracted: Shown if user cancelled AI analysis or if API key was missing
                    // ==========================================
                    is AiAnalysisState.OcrExtracted -> {
                        var editableText by remember(state.extractedText) { mutableStateOf(state.extractedText) }
                        var selectedModel by remember {
                            mutableStateOf(currentConfiguredModel.ifBlank { OpenRouterExpiryService.DEFAULT_MODEL })
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (state.isCancelled) (if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED))
                                else (if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5)),
                                border = BorderStroke(
                                    1.dp,
                                    if (state.isCancelled) (if (isDark) Color(0xFF43281C) else Color(0xFFFFEDD5))
                                    else (if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0))
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isCancelled) Icons.Outlined.Info else Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = if (state.isCancelled) Color(0xFFEA580C) else Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (state.isCancelled) "AI ANALYSIS CANCELLED" else "TEXT EXTRACTED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (state.isCancelled) (if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C))
                                        else (if (isDark) Color(0xFF34D399) else Color(0xFF047857))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (state.isCancelled) "Review Extracted Text" else "Label Text Extracted",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
                            color = textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (state.isCancelled) {
                                "AI analysis was cancelled. You can review or edit the text below, start AI analysis, or use the text directly."
                            } else {
                                state.warningMessage.ifBlank { "Confirm the extracted text below before proceeding." }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Image Preview Banner
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    AsyncImage(
                                        model = File(state.imagePath),
                                        contentDescription = "Uploaded photo",
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Photo Extracted (${state.charCount} chars, ${state.lineCount} lines)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "On-device OCR • Image is NOT uploaded",
                                            fontSize = 10.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Extracted Text Field (Editable)
                        Text(
                            text = "Extracted OCR Text:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = editableText,
                            onValueChange = { editableText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 150.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                unfocusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Model Selector Chips
                        Text(
                            text = "AI Model for Analysis:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val availableModels = listOf(
                            "openai/gpt-4o-mini" to "gpt-4o-mini",
                            "~openai/gpt-sol-latest" to "gpt-sol-latest",
                            "meta-llama/llama-3.3-70b-instruct:free" to "Free Llama 70B",
                            "google/gemini-2.0-flash-001" to "gemini-flash"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableModels.forEach { (modelId, label) ->
                                val isSelected = selectedModel == modelId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedModel = modelId },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFEC4899),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    onAnalyzeWithAi(state.imagePath, editableText, selectedModel)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze with AI (Step 2)", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onUseOcrManually(state.imagePath, editableText)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Skip AI & Use Extracted Text", fontWeight = FontWeight.SemiBold, color = textPrimary)
                            }

                            if (state.warningMessage.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onOpenSettings()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open AI Settings", color = Color(0xFF2563EB))
                                }
                            }

                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = textSecondary)
                            }
                        }
                    }

                    // ==========================================
                    // 4. Review Result (LLM Result Preview & Debug)
                    // ==========================================
                    is AiAnalysisState.ReviewResult -> {
                        var isRawJsonVisible by remember { mutableStateOf(false) }
                        var isExtractedTextVisible by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "STEP 2: AI RESULT READY",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "AI Structured Expiry Result",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
                            color = textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Verify the analyzed product details before adding to your counters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Parsed fields preview card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, cardBorder),
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        AsyncImage(
                                            model = File(state.imagePath),
                                            contentDescription = "Product image",
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = state.result.productName.ifBlank { "(Uncertain product name)" },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = textPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Category: ${state.result.category}",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Expiry Date:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textSecondary
                                    )
                                    Text(
                                        text = state.result.expiryDate.ifBlank { "Not detected" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.result.expiryDate.isNotBlank()) Color(0xFF2563EB) else Color(0xFFEA580C)
                                    )
                                }

                                if (state.result.quantity.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Quantity / Size:",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = state.result.quantity,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                }

                                if (state.result.manufacturingDate.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "MFG Date:",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = state.result.manufacturingDate,
                                            fontSize = 12.sp,
                                            color = textPrimary
                                        )
                                    }
                                }

                                if (state.result.needsVerification) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) Color(0xFF43281C) else Color(0xFFFFF7ED),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5))
                                    ) {
                                        Text(
                                            text = "⚠️ Notice: " + if (state.result.verificationReason.isNotBlank()) state.result.verificationReason else "Please verify details before saving.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Extracted Label Text accordion (fully scrollable)
                        if (state.ocrText.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF241F1D) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExtractedTextVisible = !isExtractedTextVisible }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "📄 Extracted Label Text (${state.ocrText.length} chars)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF2563EB)
                                        )
                                        Icon(
                                            imageVector = if (isExtractedTextVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    AnimatedVisibility(visible = isExtractedTextVisible) {
                                        Column(modifier = Modifier.padding(top = 8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 160.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Text(
                                                    text = state.ocrText,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp,
                                                    color = textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Debug: Raw AI response accordion
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF241F1D) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isRawJsonVisible = !isRawJsonVisible }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🔍 Debug: Raw LLM Response",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2563EB)
                                    )
                                    Icon(
                                        imageVector = if (isRawJsonVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isRawJsonVisible) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Text(
                                            text = state.rawAiResponse.ifBlank { "(Empty raw response)" },
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            color = textPrimary,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 140.dp)
                                                .verticalScroll(rememberScrollState())
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onApplyAiResult(state.result) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply to Event Form", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onAnalyzeWithAi(state.imagePath, state.ocrText, currentConfiguredModel)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Re-analyze / Try Another Model", color = textPrimary)
                            }
                        }
                    }

                    // ==========================================
                    // 5. Error State (With OCR Text & Debug info)
                    // ==========================================
                    is AiAnalysisState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (state.isTokenError) "AI Token / Credit Notice" else "AI Analysis Notice",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Error message card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF321E19) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF552E25) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                )

                                if (state.isTokenError) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tip: Your OpenRouter account may have zero credit. You can switch to the FREE model (meta-llama/llama-3.3-70b-instruct:free) or continue manually.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFFFDBA74) else Color(0xFFC2410C)
                                    )
                                }
                            }
                        }

                        // Show extracted OCR text if available to prove text WAS extracted
                        if (state.rawOcr.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF241F1D) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "On-Device OCR Succeeded (${state.rawOcr.length} chars)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 140.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = state.rawOcr,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.rawOcr.isNotBlank()) {
                                Button(
                                    onClick = {
                                        onUseOcrManually(state.imagePath, state.rawOcr)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Use Extracted Text Manually", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (state.canRetry && state.rawOcr.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onAnalyzeWithAi(state.imagePath, state.rawOcr, currentConfiguredModel)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry AI Analysis", color = textPrimary)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenSettings()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Icon(imageVector = Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open AI Settings", color = Color(0xFF2563EB))
                            }

                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dismiss", color = textSecondary)
                            }
                        }
                    }

                    AiAnalysisState.Idle, is AiAnalysisState.Success -> {
                        // Handled by early return
                    }
                }
            }
        }
    }
}
}
