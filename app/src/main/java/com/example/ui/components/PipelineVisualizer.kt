package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentCategory
import com.example.model.PipelineProgress
import com.example.model.PipelineState
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PipelineVisualizer(
    progress: PipelineProgress,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Pipeline Title & Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = "Routing Pipeline",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTI-AGENT ORCHESTRATION PIPELINE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                StatusPill(state = progress.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Pipeline Diagram Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Node 1: Input Query Ingestion
                val isInputActive = progress.state != PipelineState.IDLE
                val isInputDone = progress.state != PipelineState.IDLE && progress.state != PipelineState.INGESTING_QUERY
                PipelineNode(
                    icon = Icons.Default.Forum,
                    title = "Input",
                    subtitle = "Query",
                    isActive = progress.state == PipelineState.INGESTING_QUERY,
                    isDone = isInputDone,
                    pulseScale = if (progress.state == PipelineState.INGESTING_QUERY) pulseScale else 1f,
                    activeColor = MaterialTheme.colorScheme.primary
                )

                ConnectingArrow(isLit = isInputActive)

                // Node 2: Router Agent
                val isRouterActive = progress.state == PipelineState.ROUTING_ANALYSIS
                val isRouterDone = progress.state in listOf(
                    PipelineState.DISPATCHING_AGENT,
                    PipelineState.AGENT_GENERATING,
                    PipelineState.COMPLETED
                )
                PipelineNode(
                    icon = Icons.Default.Psychology,
                    title = "Router",
                    subtitle = if (progress.routerDurationMs > 0) "${progress.routerDurationMs}ms" else "Gemini",
                    isActive = isRouterActive,
                    isDone = isRouterDone,
                    pulseScale = if (isRouterActive) pulseScale else 1f,
                    activeColor = MaterialTheme.colorScheme.primary
                )

                ConnectingArrow(isLit = isRouterDone)

                // Node 3: Specialist Agent (Dynamic Category)
                val targetCategory = progress.detectedCategory ?: AgentCategory.TECHNICAL
                val isSpecialistActive = progress.state == PipelineState.AGENT_GENERATING ||
                        progress.state == PipelineState.DISPATCHING_AGENT
                val isSpecialistDone = progress.state == PipelineState.COMPLETED

                val specialistIcon: ImageVector = when (targetCategory) {
                    AgentCategory.TECHNICAL -> Icons.Default.Code
                    AgentCategory.RESEARCH -> Icons.Default.Science
                    AgentCategory.CASUAL -> Icons.Default.Forum
                    AgentCategory.CREATIVE -> Icons.Default.AutoAwesome
                }

                PipelineNode(
                    icon = specialistIcon,
                    title = targetCategory.shortName,
                    subtitle = if (progress.agentDurationMs > 0) "${progress.agentDurationMs}ms" else "Specialist",
                    isActive = isSpecialistActive,
                    isDone = isSpecialistDone,
                    pulseScale = if (isSpecialistActive) pulseScale else 1f,
                    activeColor = targetCategory.color
                )

                ConnectingArrow(isLit = isSpecialistDone)

                // Node 4: Synthesized Output
                PipelineNode(
                    icon = Icons.Default.Check,
                    title = "Output",
                    subtitle = "Complete",
                    isActive = false,
                    isDone = progress.state == PipelineState.COMPLETED,
                    pulseScale = 1f,
                    activeColor = Color(0xFF10B981)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-status caption
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (progress.state in listOf(
                            PipelineState.INGESTING_QUERY,
                            PipelineState.ROUTING_ANALYSIS,
                            PipelineState.DISPATCHING_AGENT,
                            PipelineState.AGENT_GENERATING
                        )
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = progress.errorMessage ?: progress.currentStepText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (progress.state == PipelineState.ERROR) MaterialTheme.colorScheme.error else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(state: PipelineState) {
    val (label, bg, fg) = when (state) {
        PipelineState.IDLE -> Triple("IDLE", DarkSurfaceVariant, TextMuted)
        PipelineState.INGESTING_QUERY -> Triple("INGESTING", MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
        PipelineState.ROUTING_ANALYSIS -> Triple("ROUTING", MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), MaterialTheme.colorScheme.primary)
        PipelineState.DISPATCHING_AGENT -> Triple("DISPATCHING", MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), MaterialTheme.colorScheme.secondary)
        PipelineState.AGENT_GENERATING -> Triple("GENERATING", MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), MaterialTheme.colorScheme.secondary)
        PipelineState.COMPLETED -> Triple("READY", Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF10B981))
        PipelineState.ERROR -> Triple("ERROR", MaterialTheme.colorScheme.error.copy(alpha = 0.2f), MaterialTheme.colorScheme.error)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PipelineNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isDone: Boolean,
    pulseScale: Float,
    activeColor: Color
) {
    val nodeBg by animateColorAsState(
        targetValue = when {
            isActive -> activeColor.copy(alpha = 0.25f)
            isDone -> activeColor.copy(alpha = 0.15f)
            else -> DarkSurfaceVariant
        },
        label = "nodeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> activeColor
            isDone -> activeColor.copy(alpha = 0.8f)
            else -> DarkCardBorder
        },
        label = "borderColor"
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            isActive || isDone -> activeColor
            else -> TextMuted
        },
        label = "iconTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(nodeBg)
                .border(1.5.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive || isDone) TextPrimary else TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = if (isActive || isDone) activeColor else TextMuted,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConnectingArrow(isLit: Boolean) {
    val color = if (isLit) MaterialTheme.colorScheme.primary else DarkCardBorder
    Box(
        modifier = Modifier
            .height(2.dp)
            .width(20.dp)
            .background(color)
            .alpha(if (isLit) 0.9f else 0.4f)
    )
}
