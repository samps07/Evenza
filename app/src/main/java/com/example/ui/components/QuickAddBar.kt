package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.NaturalLanguageParser
import com.example.domain.ParsedCounterDraft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuickAddBar(
    onQuickAdd: (ParsedCounterDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }
    val parsedDraft by remember(queryText) {
        derivedStateOf {
            if (queryText.isNotBlank()) NaturalLanguageParser.parse(queryText) else null
        }
    }

    val samplePhrases = remember {
        listOf(
            "Food coupon 5 days",
            "Movie tomorrow 8pm",
            "Library book due Friday",
            "Gift card expires Dec 31",
            "Train departure 2 hours"
        )
    }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()

    val cardBg = if (isDark) Color(0xFF1E1A18) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2724) else Color(0xFFE2E8F0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_add_bar"),
        shape = RoundedCornerShape(24.dp), // Bento rounded-3xl
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = {
                        Text(
                            "Quick add: e.g. Food coupon 5 days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (queryText.isNotEmpty()) {
                            IconButton(onClick = { queryText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            parsedDraft?.let {
                                onQuickAdd(it)
                                queryText = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0),
                        focusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_input")
                )

                if (parsedDraft != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            parsedDraft?.let {
                                onQuickAdd(it)
                                queryText = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD3E4FF),
                            contentColor = Color(0xFF1E3A8A)
                        ),
                        modifier = Modifier.testTag("quick_add_confirm_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live parsed preview banner
            AnimatedVisibility(
                visible = parsedDraft != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                parsedDraft?.let { draft ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = draft.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = draft.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFF5F2F0) else Color(0xFF1E293B)
                                    )
                                )
                                Text(
                                    text = "Expires: ${dateFormat.format(Date(draft.targetTimeMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2563EB)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = draft.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Horizontal suggestions when empty
            if (queryText.isEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(samplePhrases) { phrase ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF25201E) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF322C2A) else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable { queryText = phrase }
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
