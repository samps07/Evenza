package com.example

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifications.EventNotificationManager
import com.example.ui.CounterViewModel
import com.example.ui.components.AiScanningDialog
import com.example.ui.components.EventCreationChoiceSheet
import com.example.ui.screens.AddEditEventScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CounterTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: CounterViewModel by viewModels()
    private val viewingImageUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        EventNotificationManager.createNotificationChannels(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        handleIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density * uiState.uiScale,
                fontScale = currentDensity.fontScale * uiState.uiScale
            )

            var cameraCaptureUri by remember { mutableStateOf<Uri?>(null) }

            // Launcher for taking photo with camera
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success ->
                if (success && cameraCaptureUri != null) {
                    viewModel.processExpiryImage(cameraCaptureUri!!, context)
                }
            }

            // Launcher for camera permission request
            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    try {
                        val file = File(cacheDir, "camera_label_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            this@MainActivity,
                            "${applicationContext.packageName}.fileprovider",
                            file
                        )
                        cameraCaptureUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Camera setup error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Camera permission needed to snap product labels", Toast.LENGTH_SHORT).show()
                }
            }

            // Launcher for picking photo from gallery
            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    viewModel.processExpiryImage(uri, context)
                }
            }

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                CounterTheme(themePreference = uiState.themePreference) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = when {
                                    uiState.isSettingsOpen -> ScreenMode.SETTINGS
                                    uiState.isAddEventOpen -> ScreenMode.ADD_EDIT
                                    uiState.selectedEventForDetail != null -> ScreenMode.DETAIL
                                    else -> ScreenMode.HOME
                                },
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screen_transition"
                            ) { mode ->
                                when (mode) {
                                    ScreenMode.SETTINGS -> {
                                        SettingsScreen(
                                            currentTheme = uiState.themePreference,
                                            onThemeChange = { viewModel.setThemePreference(it) },
                                            currentSort = uiState.sortMode,
                                            onSortChange = { viewModel.setSortMode(it) },
                                            currentTimeFormat = uiState.timeFormatPreference,
                                            onTimeFormatChange = { viewModel.setTimeFormatPreference(it) },
                                            uiScale = uiState.uiScale,
                                            onUiScaleChange = { viewModel.setUiScale(it) },
                                            openRouterApiKey = uiState.openRouterApiKey,
                                            onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                                            openRouterModel = uiState.openRouterModel,
                                            onModelChange = { viewModel.setOpenRouterModel(it) },
                                            openRouterBaseUrl = uiState.openRouterBaseUrl,
                                            onBaseUrlChange = { viewModel.setOpenRouterBaseUrl(it) },
                                            openRouterPrompt = uiState.openRouterPrompt,
                                            onPromptChange = { viewModel.setOpenRouterPrompt(it) },
                                            onResetPrompt = { viewModel.resetOpenRouterPromptToDefault() },
                                            ocrConfig = uiState.ocrConfig,
                                            onOcrModeChange = { viewModel.setOcrMode(it) },
                                            onOcrContrastBoostChange = { viewModel.setOcrContrastBoost(it) },
                                            onOcrSharpeningChange = { viewModel.setOcrSharpening(it) },
                                            onOcrMaxResolutionChange = { viewModel.setOcrMaxResolution(it) },
                                            onOcrRotationsChange = { viewModel.setOcrRotations(it) },
                                            onCategoriesChanged = { viewModel.refreshCategories() },
                                            onBack = { viewModel.closeSettings() }
                                        )
                                    }
                                    ScreenMode.ADD_EDIT -> {
                                        AddEditEventScreen(
                                            editingEvent = uiState.editingEvent,
                                            aiDraftResult = uiState.aiDraftResult,
                                            onSave = { id, title, description, startTimeMillis, targetTimeMillis, icon, colorHex, category, priority, notify1Day, notify6Hours, notify1Hour, notifyExpiry, linkUrl, imageUrl, customRemindersJson ->
                                                viewModel.saveEvent(
                                                    id = id,
                                                    title = title,
                                                    description = description,
                                                    startTimeMillis = startTimeMillis,
                                                    targetTimeMillis = targetTimeMillis,
                                                    icon = icon,
                                                    colorHex = colorHex,
                                                    category = category,
                                                    priority = priority,
                                                    notify1Day = notify1Day,
                                                    notify6Hours = notify6Hours,
                                                    notify1Hour = notify1Hour,
                                                    notifyExpiry = notifyExpiry,
                                                    linkUrl = linkUrl,
                                                    imageUrl = imageUrl,
                                                    customRemindersJson = customRemindersJson
                                                )
                                            },
                                            onDismiss = { viewModel.closeAddEdit() }
                                        )
                                    }
                                    ScreenMode.DETAIL -> {
                                        uiState.selectedEventForDetail?.let { event ->
                                            EventDetailScreen(
                                                event = event,
                                                nowMillis = uiState.nowMillis,
                                                onBack = { viewModel.closeDetail() },
                                                onEdit = { viewModel.openEdit(event) },
                                                onDuplicate = { viewModel.duplicateEvent(event.id) },
                                                onRestart = { viewModel.restartEvent(event.id) },
                                                onToggleCompleted = { viewModel.toggleCompleted(event.id) },
                                                onDelete = { viewModel.deleteEvent(event) },
                                                onPinNotification = { EventNotificationManager.showPinnedNotification(this@MainActivity, event) }
                                            )
                                        }
                                    }
                                    ScreenMode.HOME -> {
                                        HomeScreen(
                                            uiState = uiState,
                                            onOpenAdd = { viewModel.openCreationChoice() },
                                            onQuickAdd = { viewModel.quickAdd(it) },
                                            onOpenDetail = { viewModel.openDetail(it) },
                                            onEditEvent = { viewModel.openEdit(it) },
                                            onDuplicateEvent = { viewModel.duplicateEvent(it) },
                                            onRestartEvent = { viewModel.restartEvent(it) },
                                            onToggleCompleted = { viewModel.toggleCompleted(it) },
                                            onDeleteEvent = { viewModel.deleteEvent(it) },
                                            onCategorySelected = { viewModel.setSelectedCategory(it) },
                                            onToggleSortMode = {
                                                val newMode = if (uiState.sortMode == com.example.domain.SortMode.URGENCY) {
                                                    com.example.domain.SortMode.CUSTOM
                                                } else {
                                                    com.example.domain.SortMode.URGENCY
                                                }
                                                viewModel.setSortMode(newMode)
                                            },
                                            onMoveEvent = { from, to -> viewModel.moveEvent(from, to) },
                                            onOpenSettings = { viewModel.openSettings() },
                                            onThemeChange = { viewModel.setThemePreference(it) },
                                            onTimeFormatChange = { viewModel.setTimeFormatPreference(it) },
                                            onUiScaleChange = { viewModel.setUiScale(it) },
                                            openRouterApiKey = uiState.openRouterApiKey,
                                            onApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
                                            openRouterModel = uiState.openRouterModel,
                                            onModelChange = { viewModel.setOpenRouterModel(it) },
                                            openRouterBaseUrl = uiState.openRouterBaseUrl,
                                            onBaseUrlChange = { viewModel.setOpenRouterBaseUrl(it) },
                                            openRouterPrompt = uiState.openRouterPrompt,
                                            onPromptChange = { viewModel.setOpenRouterPrompt(it) },
                                            onResetPrompt = { viewModel.resetOpenRouterPromptToDefault() },
                                            ocrConfig = uiState.ocrConfig,
                                            onOcrModeChange = { viewModel.setOcrMode(it) },
                                            onOcrContrastBoostChange = { viewModel.setOcrContrastBoost(it) },
                                            onOcrSharpeningChange = { viewModel.setOcrSharpening(it) },
                                            onOcrMaxResolutionChange = { viewModel.setOcrMaxResolution(it) },
                                            onOcrRotationsChange = { viewModel.setOcrRotations(it) },
                                            onCategoriesChanged = { viewModel.refreshCategories() }
                                        )
                                    }
                                }
                            }

                            // Back handling for bottom sheet and scanning dialog
                            BackHandler(enabled = uiState.isCreationChoiceOpen) {
                                viewModel.closeCreationChoice()
                            }
                            BackHandler(enabled = uiState.aiAnalysisState !is com.example.ai.AiAnalysisState.Idle && uiState.aiAnalysisState !is com.example.ai.AiAnalysisState.Success) {
                                if (uiState.aiAnalysisState is com.example.ai.AiAnalysisState.CallingAi) {
                                    viewModel.cancelAiAnalysis()
                                } else {
                                    viewModel.resetAiAnalysisState()
                                }
                            }

                            // Event Creation Choice Bottom Sheet (Manual vs AI Tracker)
                            if (uiState.isCreationChoiceOpen) {
                                EventCreationChoiceSheet(
                                    isApiKeyConfigured = uiState.openRouterApiKey.isNotBlank(),
                                    onDismiss = { viewModel.closeCreationChoice() },
                                    onChooseManual = { viewModel.startManualAdd() },
                                    onChooseCamera = {
                                        val file = File(cacheDir, "camera_label_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(
                                            this@MainActivity,
                                            "${applicationContext.packageName}.fileprovider",
                                            file
                                        )
                                        cameraCaptureUri = uri
                                        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            cameraLauncher.launch(uri)
                                        } else {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    },
                                    onChooseGallery = {
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    onOpenSettings = {
                                        viewModel.closeCreationChoice()
                                        viewModel.openSettings()
                                    }
                                )
                            }

                            // AI OCR & Analysis Live Progress, Image Extraction & LLM Review Dialog
                            AiScanningDialog(
                                state = uiState.aiAnalysisState,
                                currentConfiguredModel = uiState.openRouterModel,
                                onDismiss = { viewModel.resetAiAnalysisState() },
                                onCancelAiAnalysis = { viewModel.cancelAiAnalysis() },
                                onAnalyzeWithAi = { imagePath, ocrText, model ->
                                    viewModel.analyzeExtractedText(imagePath, ocrText, model)
                                },
                                onApplyAiResult = { result ->
                                    viewModel.applyAiResult(result)
                                },
                                onUseOcrManually = { imagePath, ocrText ->
                                    viewModel.useOcrTextManually(imagePath, ocrText)
                                },
                                onOpenSettings = { viewModel.openSettings() }
                            )

                            val currentViewImage = viewingImageUrl.value
                            if (currentViewImage != null) {
                                com.example.ui.components.ImageViewerDialog(
                                    imageUrl = currentViewImage,
                                    onDismiss = { viewingImageUrl.value = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val viewImageUrl = intent.getStringExtra("EXTRA_IMAGE_URL")
        if (!viewImageUrl.isNullOrBlank()) {
            viewingImageUrl.value = viewImageUrl
        }
        val eventId = intent.getLongExtra("EXTRA_EVENT_ID", -1L)
        if (eventId != -1L) {
            viewModel.openEventById(eventId)
        } else if (intent.getBooleanExtra("EXTRA_OPEN_ADD", false) || intent.action == "com.example.counter.ACTION_ADD") {
            viewModel.openAdd()
        }
    }
}

enum class ScreenMode {
    HOME,
    DETAIL,
    ADD_EDIT,
    SETTINGS
}

// Kept for screenshot test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
