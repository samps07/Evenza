package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiAnalysisState
import com.example.ai.AiExpiryResult
import com.example.ai.ExpiryCategoryDefaults
import com.example.ai.OcrConfig
import com.example.ai.OcrHelper
import com.example.ai.OcrMode
import com.example.ai.OpenRouterExpiryService
import com.example.data.AppDatabase
import com.example.data.EventEntity
import com.example.data.EventRepository
import com.example.domain.EventSorter
import com.example.domain.ParsedCounterDraft
import com.example.domain.SortMode
import com.example.notifications.NotificationScheduler
import com.example.util.ImageStorageHelper
import com.example.widget.CounterWidgetProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CounterUiState(
    val events: List<EventEntity> = emptyList(),
    val activeEvents: List<EventEntity> = emptyList(),
    val expiredEvents: List<EventEntity> = emptyList(),
    val completedEvents: List<EventEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val sortMode: SortMode = SortMode.URGENCY,
    val nowMillis: Long = System.currentTimeMillis(),
    val selectedEventForDetail: EventEntity? = null,
    val editingEvent: EventEntity? = null,
    val isAddEventOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isCreationChoiceOpen: Boolean = false,
    val themePreference: String = "System", // "System", "Light", "Dark"
    val timeFormatPreference: String = "12h", // "12h", "24h"
    val uiScale: Float = 1.0f,
    val openRouterApiKey: String = "",
    val openRouterModel: String = OpenRouterExpiryService.DEFAULT_MODEL,
    val openRouterBaseUrl: String = OpenRouterExpiryService.DEFAULT_BASE_URL,
    val openRouterPrompt: String = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT,
    val ocrConfig: OcrConfig = OcrConfig(),
    val aiAnalysisState: AiAnalysisState = AiAnalysisState.Idle,
    val aiDraftResult: AiExpiryResult? = null
)

class CounterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    private val prefs = application.getSharedPreferences("counter_app_prefs", Context.MODE_PRIVATE)

    private val _selectedCategory = MutableStateFlow("All")
    private val _sortMode = MutableStateFlow(
        if (prefs.getString("sort_mode", "URGENCY") == "CUSTOM") SortMode.CUSTOM else SortMode.URGENCY
    )
    private val _themePreference = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    private val _timeFormatPreference = MutableStateFlow(prefs.getString("time_format", "12h") ?: "12h")
    private val _uiScalePreference = MutableStateFlow(prefs.getFloat("ui_scale", 1.0f))

    private val _selectedEventForDetail = MutableStateFlow<EventEntity?>(null)
    private val _editingEvent = MutableStateFlow<EventEntity?>(null)
    private val _isAddEventOpen = MutableStateFlow(false)
    private val _isSettingsOpen = MutableStateFlow(false)
    private val _isCreationChoiceOpen = MutableStateFlow(false)

    // AI Configuration & State
    private val _openRouterApiKey = MutableStateFlow(prefs.getString("openrouter_api_key", "") ?: "")
    private val _openRouterModel = MutableStateFlow(
        prefs.getString("openrouter_model", OpenRouterExpiryService.DEFAULT_MODEL) ?: OpenRouterExpiryService.DEFAULT_MODEL
    )
    private val _openRouterBaseUrl = MutableStateFlow(
        prefs.getString("openrouter_base_url", OpenRouterExpiryService.DEFAULT_BASE_URL) ?: OpenRouterExpiryService.DEFAULT_BASE_URL
    )
    private val _openRouterPrompt = MutableStateFlow(
        prefs.getString("openrouter_system_prompt", OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT)
            ?: OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT
    )
    private val _ocrConfig = MutableStateFlow(
        OcrConfig(
            mode = try {
                OcrMode.valueOf(prefs.getString("ocr_mode", OcrMode.DEEP_SCAN.name) ?: OcrMode.DEEP_SCAN.name)
            } catch (_: Exception) {
                OcrMode.DEEP_SCAN
            },
            maxResolution = prefs.getInt("ocr_max_res", 1800),
            contrastBoost = prefs.getBoolean("ocr_contrast_boost", true),
            sharpening = prefs.getBoolean("ocr_sharpening", true),
            checkRotations = prefs.getBoolean("ocr_rotations", false)
        )
    )

    private val _aiAnalysisState = MutableStateFlow<AiAnalysisState>(AiAnalysisState.Idle)
    private val _aiDraftResult = MutableStateFlow<AiExpiryResult?>(null)
    private val _categoriesVersion = MutableStateFlow(0)

    private var aiJob: Job? = null

    // Ticker flow emitting current time periodically (every 30 seconds) to conserve resources and avoid high-frequency redraws
    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30000L)
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = EventRepository(application, db.eventDao())
    }

    val uiState: StateFlow<CounterUiState> = combine(
        listOf(
            repository.allEventsFlow,
            _selectedCategory,
            _sortMode,
            _themePreference,
            _timeFormatPreference,
            _uiScalePreference,
            tickerFlow,
            _selectedEventForDetail,
            _editingEvent,
            _isAddEventOpen,
            _isSettingsOpen,
            _isCreationChoiceOpen,
            _openRouterApiKey,
            _openRouterModel,
            _openRouterBaseUrl,
            _openRouterPrompt,
            _ocrConfig,
            _aiAnalysisState,
            _aiDraftResult,
            _categoriesVersion
        )
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allEvents = params[0] as List<EventEntity>
        val category = params[1] as String
        val sort = params[2] as SortMode
        val theme = params[3] as String
        val timeFormat = params[4] as String
        val uiScale = params[5] as Float
        val now = params[6] as Long
        val detail = params[7] as EventEntity?
        val editing = params[8] as EventEntity?
        val addOpen = params[9] as Boolean
        val settingsOpen = params[10] as Boolean
        val choiceOpen = params[11] as Boolean
        val apiKey = params[12] as String
        val model = params[13] as String
        val baseUrl = params[14] as String
        val prompt = params[15] as String
        val ocrConf = params[16] as OcrConfig
        val aiState = params[17] as AiAnalysisState
        val aiDraft = params[18] as AiExpiryResult?

        // Extract distinct categories from CategoryManager and events
        val distinctCats = mutableListOf("All")
        val managedCats = com.example.data.CategoryManager.getCategories(getApplication()).map { it.name }
        distinctCats.addAll(managedCats)
        val catsFromEvents = allEvents.map { it.category }.filter { it.isNotBlank() && !distinctCats.contains(it) }.distinct()
        distinctCats.addAll(catsFromEvents)

        // Filter by category
        val filtered = if (category == "All") {
            allEvents
        } else {
            allEvents.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Apply sorting
        val sorted = EventSorter.sort(filtered, sort, now)

        val active = mutableListOf<EventEntity>()
        val expired = mutableListOf<EventEntity>()
        val completed = mutableListOf<EventEntity>()

        for (item in sorted) {
            when {
                item.isCompleted -> completed.add(item)
                item.targetTimeMillis <= now -> expired.add(item)
                else -> active.add(item)
            }
        }

        // Refresh currently opened detail item if its data changed in DB
        val updatedDetail = if (detail != null) {
            allEvents.find { it.id == detail.id } ?: detail
        } else null

        CounterUiState(
            events = sorted,
            activeEvents = active,
            expiredEvents = expired,
            completedEvents = completed,
            categories = distinctCats,
            selectedCategory = category,
            sortMode = sort,
            nowMillis = now,
            selectedEventForDetail = updatedDetail,
            editingEvent = editing,
            isAddEventOpen = addOpen,
            isSettingsOpen = settingsOpen,
            isCreationChoiceOpen = choiceOpen,
            themePreference = theme,
            timeFormatPreference = timeFormat,
            uiScale = uiScale,
            openRouterApiKey = apiKey,
            openRouterModel = model,
            openRouterBaseUrl = baseUrl,
            openRouterPrompt = prompt,
            ocrConfig = ocrConf,
            aiAnalysisState = aiState,
            aiDraftResult = aiDraft
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CounterUiState()
    )

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        prefs.edit().putString("sort_mode", if (mode == SortMode.CUSTOM) "CUSTOM" else "URGENCY").apply()
    }

    fun setThemePreference(theme: String) {
        _themePreference.value = theme
        prefs.edit().putString("theme_mode", theme).apply()
    }

    fun setTimeFormatPreference(format: String) {
        _timeFormatPreference.value = format
        prefs.edit().putString("time_format", format).apply()
    }

    fun setUiScale(scale: Float) {
        _uiScalePreference.value = scale
        prefs.edit().putFloat("ui_scale", scale).apply()
    }

    fun refreshCategories() {
        _categoriesVersion.value += 1
        try {
            CounterWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {}
    }

    fun updateCategoryNameInEvents(oldCategory: String, newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategoryName(oldCategory, newCategory)
            _categoriesVersion.value += 1
        }
    }

    fun setOpenRouterApiKey(key: String) {
        _openRouterApiKey.value = key
        prefs.edit().putString("openrouter_api_key", key).apply()
    }

    fun setOpenRouterModel(model: String) {
        _openRouterModel.value = model
        prefs.edit().putString("openrouter_model", model).apply()
    }

    fun setOpenRouterBaseUrl(url: String) {
        _openRouterBaseUrl.value = url
        prefs.edit().putString("openrouter_base_url", url).apply()
    }

    fun setOpenRouterPrompt(prompt: String) {
        _openRouterPrompt.value = prompt
        prefs.edit().putString("openrouter_system_prompt", prompt).apply()
    }

    fun resetOpenRouterPromptToDefault() {
        val defaultPrompt = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT
        _openRouterPrompt.value = defaultPrompt
        prefs.edit().putString("openrouter_system_prompt", defaultPrompt).apply()
    }

    fun setOcrMode(mode: OcrMode) {
        val updated = _ocrConfig.value.copy(mode = mode)
        _ocrConfig.value = updated
        prefs.edit().putString("ocr_mode", mode.name).apply()
    }

    fun setOcrContrastBoost(enabled: Boolean) {
        val updated = _ocrConfig.value.copy(contrastBoost = enabled)
        _ocrConfig.value = updated
        prefs.edit().putBoolean("ocr_contrast_boost", enabled).apply()
    }

    fun setOcrSharpening(enabled: Boolean) {
        val updated = _ocrConfig.value.copy(sharpening = enabled)
        _ocrConfig.value = updated
        prefs.edit().putBoolean("ocr_sharpening", enabled).apply()
    }

    fun setOcrMaxResolution(resolution: Int) {
        val updated = _ocrConfig.value.copy(maxResolution = resolution)
        _ocrConfig.value = updated
        prefs.edit().putInt("ocr_max_res", resolution).apply()
    }

    fun setOcrRotations(enabled: Boolean) {
        val updated = _ocrConfig.value.copy(checkRotations = enabled)
        _ocrConfig.value = updated
        prefs.edit().putBoolean("ocr_rotations", enabled).apply()
    }

    fun resetAiAnalysisState() {
        aiJob?.cancel()
        aiJob = null
        _aiAnalysisState.value = AiAnalysisState.Idle
    }

    fun cancelAiAnalysis() {
        aiJob?.cancel()
        aiJob = null
        val current = _aiAnalysisState.value
        if (current is AiAnalysisState.CallingAi) {
            _aiAnalysisState.value = AiAnalysisState.OcrExtracted(
                imagePath = current.imagePath,
                extractedText = current.ocrText,
                isCancelled = true
            )
        } else {
            _aiAnalysisState.value = AiAnalysisState.Idle
        }
    }

    fun openCreationChoice() {
        _isCreationChoiceOpen.value = true
    }

    fun closeCreationChoice() {
        _isCreationChoiceOpen.value = false
    }

    fun startManualAdd(initialImagePath: String = "") {
        _isCreationChoiceOpen.value = false
        _aiDraftResult.value = null
        if (initialImagePath.isNotBlank()) {
            val now = System.currentTimeMillis()
            _editingEvent.value = EventEntity(
                title = "",
                startTimeMillis = now,
                targetTimeMillis = now + 7 * 86400_000L,
                imageUrl = initialImagePath
            )
        } else {
            _editingEvent.value = null
        }
        _isAddEventOpen.value = true
    }

    fun openDetail(event: EventEntity) {
        _selectedEventForDetail.value = event
    }

    fun closeDetail() {
        _selectedEventForDetail.value = null
    }

    fun openAdd() {
        _editingEvent.value = null
        _aiDraftResult.value = null
        _isAddEventOpen.value = true
    }

    fun openEdit(event: EventEntity) {
        _selectedEventForDetail.value = null
        _aiDraftResult.value = null
        _editingEvent.value = event
        _isAddEventOpen.value = true
    }

    fun closeAddEdit() {
        _editingEvent.value = null
        _aiDraftResult.value = null
        _isAddEventOpen.value = false
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun openEventById(id: Long) {
        viewModelScope.launch {
            val event = repository.getEventById(id)
            if (event != null) {
                _selectedEventForDetail.value = event
            }
        }
    }

    /**
     * Automated Expiry Flow:
     * 1. Extracts text locally using the configured OCR profile (Fast, Enhanced, Deep Scan, or Max Precision).
     * 2. Shows extracted text in front and AUTOMATICALLY launches Step 2 (AI Analysis) without requiring
     *    the user to press another button!
     * 3. Provides a prominent Cancel button during AI analysis to pause or edit manually.
     */
    fun processExpiryImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _isCreationChoiceOpen.value = false

            val savedPath = ImageStorageHelper.saveUriToInternalStorage(context, imageUri)
            val path = savedPath ?: imageUri.toString()
            val currentOcrConfig = _ocrConfig.value
            _aiAnalysisState.value = AiAnalysisState.ReadingOcr(
                imagePath = path,
                ocrModeTitle = currentOcrConfig.mode.title
            )

            val ocrText = try {
                OcrHelper.extractTextFromPath(path, currentOcrConfig)
            } catch (e: Exception) {
                _aiAnalysisState.value = AiAnalysisState.Error(
                    message = "Failed to run OCR on image: ${e.localizedMessage ?: "Unknown error"}",
                    imagePath = path
                )
                return@launch
            }

            if (ocrText.isBlank()) {
                _aiAnalysisState.value = AiAnalysisState.Error(
                    message = "No readable text was detected from this image. Try choosing 'Clarity Boost' or 'Deep Multi-Pass' OCR mode in Settings, or ensure the label is well-lit and in focus.",
                    imagePath = path
                )
                return@launch
            }

            // Check if API key is present
            val key = _openRouterApiKey.value.trim()
            if (key.isBlank()) {
                // If key is missing, show extracted OCR text with clear notification to configure key
                _aiAnalysisState.value = AiAnalysisState.OcrExtracted(
                    imagePath = path,
                    extractedText = ocrText,
                    warningMessage = "Please configure your OpenRouter / OpenAI API key in Settings to analyze automatically."
                )
            } else {
                // AUTOMATED STEP 2: Show extracted text and immediately trigger AI analysis!
                analyzeExtractedText(imagePath = path, ocrText = ocrText)
            }
        }
    }

    fun analyzeExtractedText(imagePath: String, ocrText: String, overrideModel: String? = null) {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            val key = _openRouterApiKey.value.trim()
            if (key.isBlank()) {
                _aiAnalysisState.value = AiAnalysisState.Error(
                    message = "OpenRouter / OpenAI API key is missing. Please configure your API key in Settings -> AI Expiry Tracker.",
                    rawOcr = ocrText,
                    imagePath = imagePath
                )
                return@launch
            }

            val effectiveModel = (overrideModel ?: _openRouterModel.value).trim()
            val config = _ocrConfig.value

            _aiAnalysisState.value = AiAnalysisState.CallingAi(
                imagePath = imagePath,
                ocrText = ocrText,
                model = effectiveModel,
                ocrModeTitle = config.mode.title
            )

            try {
                val service = OpenRouterExpiryService()
                val (aiResult, rawResponse) = service.analyzeOcrText(
                    ocrText = ocrText,
                    apiKey = key,
                    baseUrl = _openRouterBaseUrl.value,
                    model = effectiveModel,
                    customPrompt = _openRouterPrompt.value,
                    availableCategories = com.example.data.CategoryManager.getCategories(getApplication())
                )
                val finalResult = aiResult.copy(imagePath = imagePath)

                // Show the LLM result for debugging and user review
                _aiAnalysisState.value = AiAnalysisState.ReviewResult(
                    imagePath = imagePath,
                    ocrText = ocrText,
                    result = finalResult,
                    rawAiResponse = rawResponse
                )
            } catch (ce: CancellationException) {
                // AI call was explicitly cancelled by the user - state was already transitioned to OcrExtracted
                throw ce
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: e.message ?: "Failed to analyze label with AI"
                _aiAnalysisState.value = AiAnalysisState.Error(
                    message = errMsg,
                    rawOcr = ocrText,
                    imagePath = imagePath
                )
            }
        }
    }

    fun applyAiResult(result: AiExpiryResult) {
        val categoryDefaults = ExpiryCategoryDefaults.getDefaultsForCategory(result.category)
        val now = System.currentTimeMillis()
        val targetTime = result.expiryEpochMillis ?: (now + 7 * 86400_000L)

        val noteBody = buildString {
            if (result.quantity.isNotBlank()) append("Quantity: ${result.quantity}\n")
            if (result.manufacturingDate.isNotBlank()) append("MFG Date: ${result.manufacturingDate}\n")
            if (result.notes.isNotBlank()) append("Notes: ${result.notes}\n")
            if (result.needsVerification && result.verificationReason.isNotBlank()) {
                append("\n[AI Note: ${result.verificationReason}]\n")
            }
            if (result.rawOcrText.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append("Extracted Text:\n${result.rawOcrText}")
            }
        }.trim()

        val draft = EventEntity(
            title = result.productName.ifBlank { "Scanned Product" },
            description = noteBody,
            startTimeMillis = now,
            targetTimeMillis = targetTime,
            icon = categoryDefaults.defaultIcon,
            colorHex = categoryDefaults.defaultColorHex,
            category = result.category.ifBlank { "General" },
            priority = 1,
            notify1DayBefore = categoryDefaults.notify1DayBefore,
            notify6HoursBefore = categoryDefaults.notify6HoursBefore,
            notify1HourBefore = categoryDefaults.notify1HourBefore,
            notifyAtExpiry = categoryDefaults.notifyAtExpiry,
            imageUrl = result.imagePath
        )

        _aiDraftResult.value = result
        _editingEvent.value = draft
        _aiAnalysisState.value = AiAnalysisState.Success(result)
        _isAddEventOpen.value = true
    }

    fun useOcrTextManually(imagePath: String, ocrText: String) {
        val now = System.currentTimeMillis()
        val firstLine = ocrText.lines().firstOrNull { it.isNotBlank() }?.trim() ?: "Scanned Product"
        val draft = EventEntity(
            title = if (firstLine.length > 50) firstLine.take(47) + "..." else firstLine,
            description = ocrText,
            startTimeMillis = now,
            targetTimeMillis = now + 7 * 86400_000L,
            icon = "🏷️",
            colorHex = "#3B82F6",
            category = "General",
            priority = 0,
            imageUrl = imagePath
        )
        _editingEvent.value = draft
        _aiDraftResult.value = null
        _aiAnalysisState.value = AiAnalysisState.Idle
        _isAddEventOpen.value = true
    }

    fun testAiConnection(onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val service = OpenRouterExpiryService()
                val msg = service.testConnection(
                    apiKey = _openRouterApiKey.value,
                    baseUrl = _openRouterBaseUrl.value,
                    model = _openRouterModel.value
                )
                onResult(msg)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: e.message ?: "Connection failed")
            }
        }
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        description: String = "",
        startTimeMillis: Long,
        targetTimeMillis: Long,
        icon: String = "⏳",
        colorHex: String = "#6366F1",
        category: String = "General",
        priority: Int = 0,
        notify1Day: Boolean = false,
        notify6Hours: Boolean = false,
        notify1Hour: Boolean = false,
        notifyExpiry: Boolean = true,
        linkUrl: String = "",
        imageUrl: String = "",
        customRemindersJson: String = ""
    ) {
        viewModelScope.launch {
            val event = EventEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                startTimeMillis = startTimeMillis,
                targetTimeMillis = targetTimeMillis,
                icon = icon.ifEmpty { "⏳" },
                colorHex = colorHex.ifEmpty { "#6366F1" },
                category = category.trim().ifEmpty { "General" },
                priority = priority,
                notify1DayBefore = notify1Day,
                notify6HoursBefore = notify6Hours,
                notify1HourBefore = notify1Hour,
                notifyAtExpiry = notifyExpiry,
                linkUrl = linkUrl.trim(),
                imageUrl = imageUrl.trim(),
                customRemindersJson = customRemindersJson
            )

            if (id == 0L) {
                val newId = repository.insert(event)
                NotificationScheduler.scheduleReminders(getApplication(), event.copy(id = newId))
            } else {
                repository.update(event)
                NotificationScheduler.scheduleReminders(getApplication(), event)
            }
            closeAddEdit()
        }
    }

    fun quickAdd(draft: ParsedCounterDraft) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val event = EventEntity(
                title = draft.title,
                description = "",
                startTimeMillis = now,
                targetTimeMillis = draft.targetTimeMillis,
                icon = draft.icon,
                colorHex = draft.colorHex,
                category = draft.category,
                priority = 1,
                notifyAtExpiry = true
            )
            val newId = repository.insert(event)
            NotificationScheduler.scheduleReminders(getApplication(), event.copy(id = newId))
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            NotificationScheduler.cancelAllReminders(getApplication(), event)
            repository.delete(event)
            if (_selectedEventForDetail.value?.id == event.id) {
                closeDetail()
            }
        }
    }

    fun duplicateEvent(id: Long) {
        viewModelScope.launch {
            repository.duplicate(id)
        }
    }

    fun restartEvent(id: Long) {
        viewModelScope.launch {
            repository.restart(id)
        }
    }

    fun toggleCompleted(id: Long) {
        viewModelScope.launch {
            repository.toggleCompleted(id)
        }
    }

    fun moveEvent(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.events.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val moved = currentList.removeAt(fromIndex)
            currentList.add(toIndex, moved)
            viewModelScope.launch {
                repository.updateOrder(currentList)
            }
        }
    }
}
