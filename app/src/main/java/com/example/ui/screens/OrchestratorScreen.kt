package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentCategory
import com.example.model.AgentProfile
import com.example.model.PipelineState
import com.example.model.RoutingResult
import com.example.ui.components.MarkdownOutputView
import com.example.ui.components.PipelineVisualizer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentRouterUiState
import com.example.ui.viewmodel.OrchestrationMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrchestratorScreen(
    uiState: AgentRouterUiState,
    onQueryChange: (String) -> Unit,
    onModeChange: (OrchestrationMode) -> Unit,
    onSelectManualAgent: (AgentProfile) -> Unit,
    onRunWorkflow: () -> Unit,
    onCancelExecution: () -> Unit,
    onOpenVoiceTranscriber: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val presetQueries = remember {
        listOf(
            "Android background service me battery optimization kaise handle karein?",
            "Quantum computing vs Classical computing trade-offs",
            "Write a sci-fi haiku about an AI discovering silence",
            "Mujhe healthy daily routine maintain karne ke practical tips do"
        )
    }

    val isRunning = uiState.pipelineProgress.state in listOf(
        PipelineState.INGESTING_QUERY,
        PipelineState.ROUTING_ANALYSIS,
        PipelineState.DISPATCHING_AGENT,
        PipelineState.AGENT_GENERATING
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Mode Selector (Auto-Route vs Manual vs Compare)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ORCHESTRATION MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModePill(
                        title = "Auto-Route",
                        subtitle = "Router Agent",
                        isSelected = uiState.mode == OrchestrationMode.AUTO_ROUTE,
                        onClick = { onModeChange(OrchestrationMode.AUTO_ROUTE) },
                        modifier = Modifier.weight(1f)
                    )
                    ModePill(
                        title = "Manual Override",
                        subtitle = "Pick Persona",
                        isSelected = uiState.mode == OrchestrationMode.MANUAL_AGENT,
                        onClick = { onModeChange(OrchestrationMode.MANUAL_AGENT) },
                        modifier = Modifier.weight(1f)
                    )
                    ModePill(
                        title = "Compare All",
                        subtitle = "Round Table",
                        isSelected = uiState.mode == OrchestrationMode.COMPARE_ALL,
                        onClick = { onModeChange(OrchestrationMode.COMPARE_ALL) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // If Manual Agent mode is selected, show agent selection chips
                if (uiState.mode == OrchestrationMode.MANUAL_AGENT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Target Specialist Agent:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.agents.forEach { agent ->
                            val isSelected = uiState.selectedManualAgent?.id == agent.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectManualAgent(agent) },
                                label = { Text(agent.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    val icon = when (agent.category) {
                                        AgentCategory.TECHNICAL -> Icons.Default.Code
                                        AgentCategory.RESEARCH -> Icons.Default.Science
                                        AgentCategory.CASUAL -> Icons.Default.Forum
                                        AgentCategory.CREATIVE -> Icons.Default.AutoAwesome
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = agent.name,
                                        tint = if (isSelected) agent.category.color else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = agent.category.color.copy(alpha = 0.2f),
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Query Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "USER PROMPT / TASK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    if (uiState.hasApiKey) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Gemini API",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenSettings() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Heuristic Demo (Tap for API Key)",
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.userQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("query_input_field"),
                    placeholder = {
                        Text(
                            "Enter code question, research query, or conversation prompt...",
                            color = TextMuted,
                            fontSize = 13.5.sp
                        )
                    },
                    maxLines = 4,
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.userQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Prompt",
                                        tint = TextMuted
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenVoiceTranscriber,
                                modifier = Modifier.testTag("router_voice_input_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speech-to-Text Voice Input",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Chips
                Text(
                    text = "Try Sample Prompts:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetQueries.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier.clickable { onQueryChange(preset) }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRunWorkflow,
                        enabled = uiState.userQuery.isNotBlank() && !isRunning,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("run_workflow_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Orchestrating..." else "Run Multi-Agent Workflow",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isRunning) {
                        Button(
                            onClick = onCancelExecution,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("cancel_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Pipeline Component
        PipelineVisualizer(progress = uiState.pipelineProgress)

        Spacer(modifier = Modifier.height(14.dp))

        // Result Section
        if (uiState.currentResult != null) {
            AgentResultCard(result = uiState.currentResult)
        }
    }
}

@Composable
private fun ModePill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else DarkSurfaceVariant
    val border = if (isSelected) MaterialTheme.colorScheme.primary else DarkCardBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.2.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}

@Composable
fun AgentResultCard(
    result: RoutingResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    result.detectedCategory.color.copy(alpha = 0.5f),
                    DarkCardBorder
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Agent badge and Timing badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(result.detectedCategory.color.copy(alpha = 0.2f))
                            .border(1.dp, result.detectedCategory.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (result.detectedCategory) {
                            AgentCategory.TECHNICAL -> Icons.Default.Code
                            AgentCategory.RESEARCH -> Icons.Default.Science
                            AgentCategory.CASUAL -> Icons.Default.Forum
                            AgentCategory.CREATIVE -> Icons.Default.AutoAwesome
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = result.detectedCategory.name,
                            tint = result.detectedCategory.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = result.selectedAgentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Category: [${result.detectedCategory.shortName}]",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = result.detectedCategory.color
                        )
                    }
                }

                // Copy & Share buttons
                Row {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Agent Response", result.responseText))
                            Toast.makeText(context, "Response copied!", Toast.LENGTH_SHORT).show()
                            copied = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Response",
                            tint = if (copied) Color(0xFF10B981) else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, result.responseText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Agent Response"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Router Decision Reasoning Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Router Reasoning",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Router Agent Decision Logic",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${result.routerLatencyMs}ms router | ${result.agentLatencyMs}ms agent",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.routerReasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specialist Response Content
            MarkdownOutputView(content = result.responseText)
        }
    }
}
