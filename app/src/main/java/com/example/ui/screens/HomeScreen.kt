package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.domain.ParsedCounterDraft
import com.example.domain.ProgressCalculator
import com.example.domain.SortMode
import com.example.notifications.EventNotificationManager
import com.example.ui.CounterUiState
import com.example.ui.components.EventCard
import com.example.ui.components.QuickAddBar

enum class HomeBentoTab {
    OVERVIEW,
    HISTORY,
    SETTINGS
}

@Composable
fun HomeScreen(
    uiState: CounterUiState,
    onOpenAdd: () -> Unit,
    onQuickAdd: (ParsedCounterDraft) -> Unit,
    onOpenDetail: (EventEntity) -> Unit,
    onEditEvent: (EventEntity) -> Unit,
    onDuplicateEvent: (Long) -> Unit,
    onRestartEvent: (Long) -> Unit,
    onToggleCompleted: (Long) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onCategorySelected: (String) -> Unit,
    onToggleSortMode: () -> Unit,
    onMoveEvent: (Int, Int) -> Unit,
    onOpenSettings: () -> Unit,
    onThemeChange: (String) -> Unit = {},
    onTimeFormatChange: (String) -> Unit = {},
    onUiScaleChange: (Float) -> Unit = {},
    openRouterApiKey: String = "",
    onApiKeyChange: (String) -> Unit = {},
    openRouterModel: String = com.example.ai.OpenRouterExpiryService.DEFAULT_MODEL,
    onModelChange: (String) -> Unit = {},
    openRouterBaseUrl: String = com.example.ai.OpenRouterExpiryService.DEFAULT_BASE_URL,
    onBaseUrlChange: (String) -> Unit = {},
    openRouterPrompt: String = com.example.ai.OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT,
    onPromptChange: (String) -> Unit = {},
    onResetPrompt: () -> Unit = {},
    ocrConfig: com.example.ai.OcrConfig = com.example.ai.OcrConfig(),
    onOcrModeChange: (com.example.ai.OcrMode) -> Unit = {},
    onOcrContrastBoostChange: (Boolean) -> Unit = {},
    onOcrSharpeningChange: (Boolean) -> Unit = {},
    onOcrMaxResolutionChange: (Int) -> Unit = {},
    onOcrRotationsChange: (Boolean) -> Unit = {},
    onCategoriesChanged: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(HomeBentoTab.OVERVIEW) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var enlargedImageUrl by remember { mutableStateOf<String?>(null) }
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val filteredActiveEvents = remember(uiState.activeEvents, searchQuery) {
        if (searchQuery.isBlank()) uiState.activeEvents
        else uiState.activeEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.linkUrl.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredExpiredEvents = remember(uiState.expiredEvents, searchQuery) {
        if (searchQuery.isBlank()) uiState.expiredEvents
        else uiState.expiredEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.linkUrl.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredCompletedEvents = remember(uiState.completedEvents, searchQuery) {
        if (searchQuery.isBlank()) uiState.completedEvents
        else uiState.completedEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.linkUrl.contains(searchQuery, ignoreCase = true)
        }
    }

    val nextUpText = remember(filteredActiveEvents) {
        val next = filteredActiveEvents.firstOrNull()
        if (next != null) "Next up: ${next.title}" else "All caught up"
    }

    val canvasBg = if (isDark) Color(0xFF141211) else Color(0xFFFDF8F6)
    val navBg = if (isDark) Color(0xFF1A1615) else Color(0xFFF7F2F1)
    val navBorder = if (isDark) Color(0xFF282321) else Color(0xFFE2E8F0)

    Scaffold(
        containerColor = canvasBg,
        floatingActionButton = {
            if (activeTab == HomeBentoTab.OVERVIEW) {
                // Bento Blue FAB (HTML: w-16 h-16 bg-[#D3E4FF] text-blue-900 rounded-2xl shadow-xl)
                FloatingActionButton(
                    onClick = onOpenAdd,
                    shape = RoundedCornerShape(20.dp), // rounded-2xl
                    containerColor = Color(0xFFD3E4FF),
                    contentColor = Color(0xFF1E3A8A),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("fab_add_counter")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add counter",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Bento Bottom Navigation Drawer/Bar
            Surface(
                color = navBg,
                border = BorderStroke(1.dp, navBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Overview Tab
                    val isOverview = activeTab == HomeBentoTab.OVERVIEW
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeTab = HomeBentoTab.OVERVIEW }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isOverview) (if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE))
                                    else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Overview",
                                tint = if (isOverview) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                                else Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isOverview) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                            else Color(0xFF64748B)
                        )
                    }

                    // History Tab
                    val isHistory = activeTab == HomeBentoTab.HISTORY
                    val historyCount = uiState.expiredEvents.size + uiState.completedEvents.size
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeTab = HomeBentoTab.HISTORY }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isHistory) (if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE))
                                    else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "History",
                                tint = if (isHistory) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                                else Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = if (historyCount > 0) "History ($historyCount)" else "History",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isHistory) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                            else Color(0xFF64748B)
                        )
                    }

                    // Settings Tab (in bottom navigation drawer beside History)
                    val isSettings = activeTab == HomeBentoTab.SETTINGS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeTab = HomeBentoTab.SETTINGS }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSettings) (if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE))
                                    else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isSettings) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                                else Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isSettings) (if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8))
                            else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Bento Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (activeTab) {
                            HomeBentoTab.OVERVIEW -> "Evenza"
                            HomeBentoTab.HISTORY -> "History"
                            HomeBentoTab.SETTINGS -> "Settings"
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-0.5).sp,
                            color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                        )
                    )
                    Text(
                        text = when (activeTab) {
                            HomeBentoTab.OVERVIEW -> nextUpText
                            HomeBentoTab.HISTORY -> "${filteredExpiredEvents.size + filteredCompletedEvents.size} past counters"
                            HomeBentoTab.SETTINGS -> "Display scale, theme & preferences"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }

                if (activeTab == HomeBentoTab.OVERVIEW) {
                    // Only keep search button at top right (Requirement 6)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSearchActive) {
                            if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)
                        } else {
                            if (isDark) Color(0xFF282321) else Color(0xFFE2E8F0)
                        },
                        modifier = Modifier.clickable {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(18.dp),
                                tint = if (isSearchActive) Color(0xFF2563EB) else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                            )
                        }
                    }
                }
            }

            // Expandable Search Bar
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search events by title, tag, link...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0),
                        focusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                        unfocusedContainerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Category & Sort Filter Chips (Overview only, Requirement 6)
            if (activeTab == HomeBentoTab.OVERVIEW) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sorting toggle chip placed at the beginning of category row (Requirement 6)
                    item(key = "sort_mode_chip") {
                        val isCustom = uiState.sortMode == SortMode.CUSTOM
                        FilterChip(
                            selected = isCustom,
                            onClick = { onToggleSortMode() },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Sort order",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCustom) Color.White else Color(0xFF2563EB)
                                )
                            },
                            label = {
                                Text(
                                    text = if (isCustom) "Custom" else "Urgency",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isCustom) Color(0xFF2563EB) else (if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0))),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                containerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                labelColor = Color(0xFF2563EB)
                            )
                        )
                    }

                    items(uiState.categories) { category ->
                        val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                        val catItem = if (category != "All") com.example.data.CategoryManager.getCategoryByName(context, category) else null
                        val displayLabel = if (catItem != null && catItem.icon.isNotBlank()) "${catItem.icon} $category" else category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(category) },
                            label = { Text(displayLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0))),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                containerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF),
                                labelColor = Color(0xFF475569)
                            )
                        )
                    }
                }
            }

            // Main Content Area (Overview vs History vs Settings)
            when (activeTab) {
                HomeBentoTab.OVERVIEW -> {
                    if (filteredActiveEvents.isEmpty()) {
                        // Empty State Bento
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            QuickAddBar(
                                onQuickAdd = onQuickAdd,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(bottom = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD3E4FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.HourglassEmpty,
                                                contentDescription = null,
                                                tint = Color(0xFF1E3A8A),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No events found" else "Nothing to keep track of yet.",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = if (searchQuery.isNotBlank()) "Try another search term" else "Add something you don't want to forget.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        TextButton(
                                            onClick = if (searchQuery.isNotBlank()) { { searchQuery = "" } } else onOpenAdd,
                                            modifier = Modifier.testTag("empty_state_add_button")
                                        ) {
                                            Icon(
                                                imageVector = if (searchQuery.isNotBlank()) Icons.Default.Close else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank()) "Clear search" else "Add counter",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp), // Bento space-y-4
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("events_list")
                        ) {
                            // Quick-Add Bar (scrolls with events to give maximum screen space)
                            item(key = "quick_add_header") {
                                QuickAddBar(
                                    onQuickAdd = onQuickAdd,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                                )
                            }

                            // Custom sort guidance banner (scrolls with list)
                            if (uiState.sortMode == SortMode.CUSTOM && filteredActiveEvents.isNotEmpty()) {
                                item(key = "custom_sort_guidance") {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 2.dp)
                                    ) {
                                        Text(
                                            text = "Custom order: Use the ↑ and ↓ arrows on cards to reorder",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = Color(0xFF1D4ED8),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            // Active Bento Cards
                            itemsIndexed(filteredActiveEvents, key = { _, item -> item.id }) { index, event ->
                                val progress = remember(event, uiState.nowMillis) {
                                    ProgressCalculator.calculate(event, uiState.nowMillis)
                                }
                                EventCard(
                                    event = event,
                                    progress = progress,
                                    onClick = { onOpenDetail(event) },
                                    onEdit = { onEditEvent(event) },
                                    onDuplicate = { onDuplicateEvent(event.id) },
                                    onRestart = { onRestartEvent(event.id) },
                                    onToggleCompleted = { onToggleCompleted(event.id) },
                                    onDelete = { onDeleteEvent(event) },
                                    onPinNotification = { EventNotificationManager.showPinnedNotification(context, event) },
                                    onImageClick = { enlargedImageUrl = it },
                                    isCustomOrder = uiState.sortMode == SortMode.CUSTOM,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < filteredActiveEvents.size - 1,
                                    onMoveUp = { onMoveEvent(index, index - 1) },
                                    onMoveDown = { onMoveEvent(index, index + 1) }
                                )
                            }
                        }
                    }
                }

                HomeBentoTab.HISTORY -> {
                    val allHistory = filteredExpiredEvents + filteredCompletedEvents
                    if (allHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No past counters found matching '$searchQuery'." else "No past counters yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allHistory, key = { it.id }) { event ->
                                com.example.ui.components.CompactHistoryCard(
                                    event = event,
                                    onClick = { onOpenDetail(event) }
                                )
                            }
                        }
                    }
                }

                HomeBentoTab.SETTINGS -> {
                    SettingsContent(
                        currentTheme = uiState.themePreference,
                        onThemeChange = onThemeChange,
                        currentSort = uiState.sortMode,
                        onSortChange = { onToggleSortMode() },
                        currentTimeFormat = uiState.timeFormatPreference,
                        onTimeFormatChange = onTimeFormatChange,
                        uiScale = uiState.uiScale,
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
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (enlargedImageUrl != null) {
        com.example.ui.components.ImageViewerDialog(
            imageUrl = enlargedImageUrl,
            onDismiss = { enlargedImageUrl = null }
        )
    }
}
