package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrainResult
import com.example.ui.components.MarkdownOutputView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentRouterUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CentralBrainScreen(
    uiState: AgentRouterUiState,
    onProblemChange: (String) -> Unit,
    onRunCentralBrain: () -> Unit,
    onUpdateBrainInstruction: (String) -> Unit,
    onOpenVoiceTranscriber: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showEditBrainPromptDialog by remember { mutableStateOf(false) }

    val presetProblems = remember {
        listOf(
            "Mujhe ek Python background service banani hai jo phone ki battery check kare aur kam hone par alert bheje.",
            "Mujhe ek automated background job chalana hai jo battery drain na kare aur network change hone par trigger ho.",
            "User credentials aur API keys ko leak-proof secure storage me kaise store karein?",
            "High-frequency sensor data streaming me memory leak aur UI thread stuttering kaise rokein?"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "arcPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // J.A.R.V.I.S Central Brain Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        CyanPrimaryLight.copy(alpha = 0.6f),
                        Color(0xFF8B5CF6).copy(alpha = 0.4f)
                    )
                ),
                width = 1.2.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Arc-Reactor Brain Emblem
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(if (uiState.isBrainThinking) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .border(1.5.dp, CyanPrimaryLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Central Brain",
                                tint = CyanPrimaryLight,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "J.A.R.V.I.S CENTRAL BRAIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = "Chain-of-Thought (CoT) Master Reasoner",
                                fontSize = 11.5.sp,
                                color = CyanPrimaryLight
                            )
                        }
                    }

                    IconButton(onClick = { showEditBrainPromptDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Brain Prompt",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model specs badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgeChip(label = "MODEL: ${uiState.brainModel.uppercase()}")
                    BadgeChip(label = "TEMP: 0.2 (DETERMINISTIC)")
                    BadgeChip(label = if (uiState.hasApiKey) "LIVE GEMINI" else "SMART HEURISTIC")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Problem Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "COMPLEX PROBLEM / OBJECTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.brainProblem,
                    onValueChange = onProblemChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brain_problem_input"),
                    placeholder = {
                        Text(
                            "Describe complex engineering problem or architectural challenge...",
                            color = TextMuted,
                            fontSize = 13.5.sp
                        )
                    },
                    maxLines = 4,
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = CyanPrimaryLight,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.brainProblem.isNotEmpty()) {
                                IconButton(onClick = { onProblemChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenVoiceTranscriber,
                                modifier = Modifier.testTag("voice_input_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input STT",
                                    tint = CyanPrimaryLight
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Problems
                Text(
                    text = "Sample Architectural Challenges:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetProblems.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier.clickable { onProblemChange(preset) }
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

                Button(
                    onClick = onRunCentralBrain,
                    enabled = uiState.brainProblem.isNotBlank() && !uiState.isBrainThinking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_brain_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isBrainThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Central Brain Deconstructing Problem...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Execute",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deconstruct Problem (Chain-of-Thought)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Brain Result Presentation
        if (uiState.brainResult != null) {
            CentralBrainResultView(
                result = uiState.brainResult,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("J.A.R.V.I.S Plan", uiState.brainResult.rawResponse))
                    Toast.makeText(context, "Full CoT blueprint copied!", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, uiState.brainResult.rawResponse)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share J.A.R.V.I.S Blueprint"))
                }
            )
        }
    }

    // Edit Brain Prompt Dialog
    if (showEditBrainPromptDialog) {
        var tempPrompt by remember { mutableStateOf(uiState.brainInstruction) }
        AlertDialog(
            onDismissRequest = { showEditBrainPromptDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Edit Central Brain System Instruction", color = TextPrimary)
            },
            text = {
                OutlinedTextField(
                    value = tempPrompt,
                    onValueChange = { tempPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateBrainInstruction(tempPrompt)
                        showEditBrainPromptDialog = false
                    }
                ) {
                    Text("Save Instruction")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBrainPromptDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun CentralBrainResultView(
    result: BrainResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Blueprint Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAIN-OF-THOUGHT BLUEPRINT (${result.latencyMs}ms)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanPrimaryLight,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Row {
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Plan",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Section 1: THOUGHT PROCESS
        CoTSectionCard(
            phaseNumber = "1",
            phaseTitle = "[THOUGHT PROCESS]: Problem Breakdown",
            icon = Icons.Default.Lightbulb,
            accentColor = CyanPrimaryLight,
            content = result.analysis
        )

        // Section 2: DECISION
        CoTSectionCard(
            phaseNumber = "2",
            phaseTitle = "[DECISION]: Action & Architectural Choice",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFFA855F7),
            content = result.decision
        )

        // Section 3: TASK SEQUENCE
        CoTSectionCard(
            phaseNumber = "3",
            phaseTitle = "[TASK SEQUENCE]: Step-by-Step Execution Blueprint",
            icon = Icons.Default.Code,
            accentColor = Color(0xFF10B981),
            content = result.executionPlan
        )
    }
}

@Composable
fun CoTSectionCard(
    phaseNumber: String,
    phaseTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(accentColor.copy(alpha = 0.45f), DarkCardBorder)
            ),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phaseNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = phaseTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            MarkdownOutputView(content = content)
        }
    }
}

@Composable
private fun BadgeChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceVariant
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
