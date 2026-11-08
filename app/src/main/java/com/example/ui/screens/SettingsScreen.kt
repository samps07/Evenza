package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.CategorySettingsSection
import com.example.ui.components.SettingsAccordionCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.OcrConfig
import com.example.ai.OcrMode
import com.example.ai.OpenRouterExpiryService
import com.example.domain.SortMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentSort: SortMode,
    onSortChange: (SortMode) -> Unit,
    currentTimeFormat: String,
    onTimeFormatChange: (String) -> Unit,
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    openRouterApiKey: String = "",
    onApiKeyChange: (String) -> Unit = {},
    openRouterModel: String = OpenRouterExpiryService.DEFAULT_MODEL,
    onModelChange: (String) -> Unit = {},
    openRouterBaseUrl: String = OpenRouterExpiryService.DEFAULT_BASE_URL,
    onBaseUrlChange: (String) -> Unit = {},
    openRouterPrompt: String = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT,
    onPromptChange: (String) -> Unit = {},
    onResetPrompt: () -> Unit = {},
    ocrConfig: OcrConfig = OcrConfig(),
    onOcrModeChange: (OcrMode) -> Unit = {},
    onOcrContrastBoostChange: (Boolean) -> Unit = {},
    onOcrSharpeningChange: (Boolean) -> Unit = {},
    onOcrMaxResolutionChange: (Int) -> Unit = {},
    onOcrRotationsChange: (Boolean) -> Unit = {},
    onCategoriesChanged: () -> Unit = {},
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF141211) else Color(0xFFFDF8F6)

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        SettingsContent(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            currentSort = currentSort,
            onSortChange = onSortChange,
            currentTimeFormat = currentTimeFormat,
            onTimeFormatChange = onTimeFormatChange,
            uiScale = uiScale,
            onUiScaleChange = onUiScaleChange,
            openRouterApiKey = openRouterApiKey,
            onApiKeyChange = onApiKeyChange,
            openRouterModel = openRouterModel,
            onModelChange = onModelChange,
            openRouterBaseUrl = openRouterBaseUrl,
            onBaseUrlChange = onBaseUrlChange,
            openRouterPrompt = openRouterPrompt,
            onPromptChange = onPromptChange,
            onResetPrompt = onResetPrompt,
            ocrConfig = ocrConfig,
            onOcrModeChange = onOcrModeChange,
            onOcrContrastBoostChange = onOcrContrastBoostChange,
            onOcrSharpeningChange = onOcrSharpeningChange,
            onOcrMaxResolutionChange = onOcrMaxResolutionChange,
            onOcrRotationsChange = onOcrRotationsChange,
            onCategoriesChanged = onCategoriesChanged,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsContent(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentSort: SortMode,
    onSortChange: (SortMode) -> Unit,
    currentTimeFormat: String,
    onTimeFormatChange: (String) -> Unit,
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    openRouterApiKey: String = "",
    onApiKeyChange: (String) -> Unit = {},
    openRouterModel: String = OpenRouterExpiryService.DEFAULT_MODEL,
    onModelChange: (String) -> Unit = {},
    openRouterBaseUrl: String = OpenRouterExpiryService.DEFAULT_BASE_URL,
    onBaseUrlChange: (String) -> Unit = {},
    openRouterPrompt: String = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT,
    onPromptChange: (String) -> Unit = {},
    onResetPrompt: () -> Unit = {},
    ocrConfig: OcrConfig = OcrConfig(),
    onOcrModeChange: (OcrMode) -> Unit = {},
    onOcrContrastBoostChange: (Boolean) -> Unit = {},
    onOcrSharpeningChange: (Boolean) -> Unit = {},
    onOcrMaxResolutionChange: (Int) -> Unit = {},
    onOcrRotationsChange: (Boolean) -> Unit = {},
    onCategoriesChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cardBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    var apiKeyInput by remember(openRouterApiKey) { mutableStateOf(openRouterApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var customModelInput by remember(openRouterModel) { mutableStateOf(openRouterModel) }
    var customUrlInput by remember(openRouterBaseUrl) { mutableStateOf(openRouterBaseUrl) }
    var promptInput by remember(openRouterPrompt) { mutableStateOf(openRouterPrompt) }
    var showPromptEditor by remember { mutableStateOf(false) }
    var showAdvancedAi by remember { mutableStateOf(false) }

    var expandedAi by remember { mutableStateOf(false) }
    var expandedOcr by remember { mutableStateOf(false) }
    var expandedCategories by remember { mutableStateOf(false) }
    var expandedUiScale by remember { mutableStateOf(false) }
    var expandedAppearance by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }
    var expandedTimeFormat by remember { mutableStateOf(false) }
    var expandedWidgetGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. AI Expiry Tracker Accordion Card (Requirement 5)
        val hasKey = apiKeyInput.isNotBlank()
        SettingsAccordionCard(
            title = "AI Settings",
            subtitle = if (hasKey) "Ready (${customModelInput.substringAfterLast("/")})" else "Configure OpenRouter / OpenAI Key",
            icon = Icons.Default.AutoAwesome,
            iconBgColor = if (isDark) Color(0xFF3B1E38) else Color(0xFFFCE7F3),
            iconTintColor = Color(0xFFEC4899),
            isExpanded = expandedAi,
            onToggleExpand = { expandedAi = !expandedAi },
            badgeText = if (hasKey) "Ready ✓" else "Key Missing",
            badgeColor = if (hasKey) Color(0xFF047857) else Color(0xFFC2410C),
            badgeBgColor = if (hasKey) (if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5)) else (if (isDark) Color(0xFF2C1911) else Color(0xFFFFF7ED))
        ) {
            // API Key Text Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-or-v1-... or sk-...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide key" else "Show key",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF2E2724) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your API key is never hardcoded and is stored strictly on your device. Only extracted OCR text is sent to the model.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Model Selection Chips
                Text(
                    text = "AI Model",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val models = listOf(
                    "openai/gpt-4o-mini" to "gpt-4o-mini",
                    "~openai/gpt-sol-latest" to "gpt-sol-latest",
                    "meta-llama/llama-3.3-70b-instruct:free" to "Free Llama 70B",
                    "google/gemini-2.0-flash-001" to "gemini-flash"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    models.forEach { (modelId, displayLabel) ->
                        val isSelected = customModelInput == modelId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                customModelInput = modelId
                                onModelChange(modelId)
                            },
                            label = { Text(displayLabel, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFEC4899) else (if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0))
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEC4899),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // ==========================================
                // Custom AI Prompt Editor (Request 2)
                // ==========================================
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPromptEditor = !showPromptEditor },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "AI Extraction Prompt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (showPromptEditor) "Tap to collapse" else "Customize system prompt instructions",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            Text(
                                text = if (showPromptEditor) "▲" else "▼",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (showPromptEditor) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E2B3D) else Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2563EB) else Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "💡 Compatibility Rule: Whatever instructions you give, the output must be valid JSON with keys (productName, expiryDate, category, notes, quantity) to populate your event cards. Use {{TODAY}} to inject the reference date.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                label = { Text("System Extraction Prompt") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 280.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = cardBorder,
                                    focusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val defaultPrompt = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT
                                        promptInput = defaultPrompt
                                        onResetPrompt()
                                        Toast.makeText(context, "Prompt reset to default", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset Default", fontSize = 11.sp, color = textPrimary)
                                }

                                Button(
                                    onClick = {
                                        onPromptChange(promptInput)
                                        Toast.makeText(context, "Custom prompt saved and active", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Advanced endpoint URL toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { showAdvancedAi = !showAdvancedAi },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAdvancedAi) "Hide Advanced Settings ▲" else "Custom Model / Endpoint URL ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2563EB)
                    )
                }

                if (showAdvancedAi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customModelInput,
                        onValueChange = { customModelInput = it },
                        label = { Text("Custom Model ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("Base URL") },
                        placeholder = { Text(OpenRouterExpiryService.DEFAULT_BASE_URL) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Live Connection Test
                var isTestingConnection by remember { mutableStateOf(false) }
                var testConnectionResult by remember { mutableStateOf<String?>(null) }
                var testConnectionIsError by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (apiKeyInput.isBlank()) {
                                testConnectionResult = "Please enter an API key first"
                                testConnectionIsError = true
                                return@OutlinedButton
                            }
                            isTestingConnection = true
                            testConnectionResult = null
                            coroutineScope.launch {
                                try {
                                    val service = OpenRouterExpiryService()
                                    val msg = service.testConnection(
                                        apiKey = apiKeyInput.trim(),
                                        baseUrl = customUrlInput.trim(),
                                        model = customModelInput.trim()
                                    )
                                    testConnectionResult = msg
                                    testConnectionIsError = false
                                } catch (e: Exception) {
                                    testConnectionResult = e.localizedMessage ?: e.message ?: "Failed"
                                    testConnectionIsError = true
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, cardBorder),
                        enabled = !isTestingConnection
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 12.sp)
                        } else {
                            Text("Test Connection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            onApiKeyChange(apiKeyInput.trim())
                            onModelChange(customModelInput.trim())
                            onBaseUrlChange(customUrlInput.trim())
                            onPromptChange(promptInput)
                            Toast.makeText(context, "AI configuration saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("Save Config", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (testConnectionResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (testConnectionIsError) (if (isDark) Color(0xFF331E19) else Color(0xFFFEF2F2))
                        else (if (isDark) Color(0xFF1E2621) else Color(0xFFECFDF5)),
                        border = BorderStroke(
                            1.dp,
                            if (testConnectionIsError) (if (isDark) Color(0xFF552E25) else Color(0xFFFCA5A5))
                            else (if (isDark) Color(0xFF29382E) else Color(0xFFA7F3D0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testConnectionResult!!,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (testConnectionIsError) (if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B))
                            else (if (isDark) Color(0xFF34D399) else Color(0xFF047857)),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. OCR Engine Accordion Card (Requirement 5)
        SettingsAccordionCard(
            title = "OCR",
            subtitle = "${ocrConfig.mode.title} • ${ocrConfig.maxResolution}px",
            icon = Icons.Outlined.DocumentScanner,
            iconBgColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
            iconTintColor = Color(0xFF2563EB),
            isExpanded = expandedOcr,
            onToggleExpand = { expandedOcr = !expandedOcr },
            badgeText = ocrConfig.mode.title
        ) {
            Text(
                text = "Fine-tune local text recognition for difficult fonts, curved bottles & stamps.",
                fontSize = 11.sp,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Engine mode cards
            OcrMode.values().forEach { mode ->
                    val isSelected = ocrConfig.mode == mode
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) (if (isDark) Color(0xFF1E2B3D) else Color(0xFFEFF6FF))
                        else (if (isDark) Color(0xFF282321) else Color(0xFFF8FAFC)),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onOcrModeChange(mode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onOcrModeChange(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = mode.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) (if (isDark) Color(0xFF1D4ED8) else Color(0xFFBFDBFE))
                                        else (if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = mode.estimatedTime,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) (if (isDark) Color(0xFFEFF6FF) else Color(0xFF1E40AF))
                                            else textSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.subtitle,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Parameter Toggles
                Text(
                    text = "Image Pre-processing Filters",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Sharpening Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sharpen Dot-Matrix Stamps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Unsharp mask filter to clarify laser-stamped or dotted dates",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = ocrConfig.sharpening,
                        onCheckedChange = onOcrSharpeningChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2563EB))
                    )
                }

                // Contrast Boost Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High-Contrast & Glare Reduction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Luminance normalization for glossy cans & dark packaging",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = ocrConfig.contrastBoost,
                        onCheckedChange = onOcrContrastBoostChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2563EB))
                    )
                }

                // Rotations Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Check 90° & 270° Orientation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Searches for dates stamped vertically along bottle rims or flaps",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = ocrConfig.checkRotations,
                        onCheckedChange = onOcrRotationsChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2563EB))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resolution Limit Chips
                Text(
                    text = "Scan Resolution Limit",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1200 to "1200px",
                        1800 to "1800px",
                        2400 to "2400px"
                    ).forEach { (res, label) ->
                        val isSelected = ocrConfig.maxResolution == res
                        FilterChip(
                            selected = isSelected,
                            onClick = { onOcrMaxResolutionChange(res) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1200px (Fast) • 1800px (Balanced) • 2400px (High-Res)",
                    fontSize = 10.sp,
                    color = textSecondary
                )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Categories & Due-In Alerts Accordion Card (Requirement 10 & 5)
        SettingsAccordionCard(
            title = "Categories",
            subtitle = "Custom categories and notification timelines",
            icon = Icons.Outlined.Category,
            iconBgColor = if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF),
            iconTintColor = Color(0xFF6366F1),
            isExpanded = expandedCategories,
            onToggleExpand = { expandedCategories = !expandedCategories },
            badgeText = "Categories"
        ) {
            CategorySettingsSection(onCategoriesChanged = onCategoriesChanged)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Display & UI Scale Accordion Card (Requirement 5 & 7)
        SettingsAccordionCard(
            title = "UI",
            subtitle = "Scale interface size for all screens",
            icon = Icons.Outlined.Speed,
            iconBgColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
            iconTintColor = Color(0xFF2563EB),
            isExpanded = expandedUiScale,
            onToggleExpand = { expandedUiScale = !expandedUiScale },
            badgeText = "${(uiScale * 100).toInt()}%"
        ) {
            Text(
                text = "Scale all layouts, cards, dialogs, and text.",
                fontSize = 11.sp,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

                var sliderValue by remember(uiScale) { mutableFloatStateOf(uiScale) }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onUiScaleChange(sliderValue) },
                    valueRange = 0.8f..1.3f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF2563EB),
                        activeTrackColor = Color(0xFF2563EB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Compact (85%)" to 0.85f,
                        "Default (100%)" to 1.0f,
                        "Large (115%)" to 1.15f
                    ).forEach { (label, value) ->
                        SuggestionChip(
                            onClick = {
                                sliderValue = value
                                onUiScaleChange(value)
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (uiScale == value) Color(0xFF2563EB) else (if (isDark) Color(0xFF382F2C) else Color(0xFFE2E8F0))
                            ),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (uiScale == value) (if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)) else Color.Transparent
                            )
                        )
                    }
                }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Theme & Appearance Accordion Card (Requirement 5)
        SettingsAccordionCard(
            title = "Theme",
            subtitle = "Match system, light, or dark mode",
            icon = Icons.Outlined.Palette,
            iconBgColor = if (isDark) Color(0xFF27272A) else Color(0xFFF4F4F5),
            iconTintColor = Color(0xFF71717A),
            isExpanded = expandedAppearance,
            onToggleExpand = { expandedAppearance = !expandedAppearance },
            badgeText = currentTheme
        ) {
            listOf("System" to "Match System", "Light" to "Light Mode", "Dark" to "Dark Mode").forEach { (key, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(key) }
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    RadioButton(
                        selected = currentTheme == key,
                        onClick = { onThemeChange(key) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (currentTheme == key) FontWeight.Bold else FontWeight.Normal,
                            color = textPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Default Sort Mode Accordion Card (Requirement 5)
        SettingsAccordionCard(
            title = "Sorting",
            subtitle = "Urgency vs Custom order",
            icon = Icons.Outlined.Sort,
            iconBgColor = if (isDark) Color(0xFF1C2833) else Color(0xFFE8F8F5),
            iconTintColor = Color(0xFF0D9488),
            isExpanded = expandedSort,
            onToggleExpand = { expandedSort = !expandedSort },
            badgeText = if (currentSort == SortMode.URGENCY) "Urgency" else "Custom"
        ) {
            listOf(
                SortMode.URGENCY to "Urgency (Expiring first)",
                SortMode.CUSTOM to "Custom Order (Drag / arrow buttons)"
            ).forEach { (mode, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortChange(mode) }
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    RadioButton(
                        selected = currentSort == mode,
                        onClick = { onSortChange(mode) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (currentSort == mode) FontWeight.Bold else FontWeight.Normal,
                            color = textPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Time Format Accordion Card (Requirement 5)
        SettingsAccordionCard(
            title = "Time Format",
            subtitle = "12-hour (AM/PM) or 24-hour clock",
            icon = Icons.Outlined.Schedule,
            iconBgColor = if (isDark) Color(0xFF332014) else Color(0xFFFFF7ED),
            iconTintColor = Color(0xFFEA580C),
            isExpanded = expandedTimeFormat,
            onToggleExpand = { expandedTimeFormat = !expandedTimeFormat },
            badgeText = currentTimeFormat
        ) {
            listOf("12h" to "12 hour (e.g. 7:30 PM)", "24h" to "24 hour (e.g. 19:30)").forEach { (key, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTimeFormatChange(key) }
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    RadioButton(
                        selected = currentTimeFormat == key,
                        onClick = { onTimeFormatChange(key) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (currentTimeFormat == key) FontWeight.Bold else FontWeight.Normal,
                            color = textPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Home-Screen Widget Guide Accordion Card (Requirement 5)
        SettingsAccordionCard(
            title = "Widgets",
            subtitle = "How to add and configure widgets",
            icon = Icons.Default.Widgets,
            iconBgColor = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF),
            iconTintColor = Color(0xFF2563EB),
            isExpanded = expandedWidgetGuide,
            onToggleExpand = { expandedWidgetGuide = !expandedWidgetGuide },
            badgeText = "Tips"
        ) {
            Text(
                text = "1. Long press any empty space on your Android home screen.\n" +
                        "2. Tap 'Widgets' and scroll to 'Counter'.\n" +
                        "3. Drag the Counter widget onto your home screen.\n" +
                        "4. Choose specific categories (e.g. Food, Medicine) or All events for that widget instance.\n" +
                        "5. You can add multiple widgets with different category combinations!\n" +
                        "6. Tap anywhere in widget background to reconfigure it anytime.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Non-functional squircle buttons: Guide (book icon) & Donate (coffee icon)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable {
                        Toast.makeText(context, "Guide feature coming soon", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = "Guide",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guide",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = textPrimary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable {
                        Toast.makeText(context, "Thank you for considering support! ☕", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = "Donate",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Donate",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Made with ❤️ by Sam",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))
    }
}
