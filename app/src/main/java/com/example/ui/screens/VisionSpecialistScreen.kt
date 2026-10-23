package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VisionAnalysisResult
import com.example.ui.theme.AmberCreative
import com.example.ui.theme.AmberCreativeLight
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
fun VisionSpecialistScreen(
    uiState: AgentRouterUiState,
    onPromptChange: (String) -> Unit,
    onSelectPreset: (String, String) -> Unit,
    onSetCustomImage: (ByteArray, android.net.Uri?) -> Unit,
    onClearCustomImage: () -> Unit,
    onRunVisionAnalysis: () -> Unit,
    onTriggerAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()
                if (bytes.isNotEmpty()) {
                    onSetCustomImage(bytes, uri)
                }
            } catch (_: Exception) { }
        }
    }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "visionLaser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    val samplePresets = remember {
        listOf(
            Triple(
                "15% Battery Warning Screen",
                "System battery dialog showing critical 15% alert and power saving option.",
                "Describe the contents of this image and suggest on-screen actions."
            ),
            Triple(
                "Server 500 Network Disconnect",
                "App network disconnected screen with retry button and gateway error.",
                "Screen par network error dikh raha hai, diagnose karein aur solution dein."
            ),
            Triple(
                "Compiler Syntax Error Screen",
                "Android Studio terminal showing compilation error at line 367.",
                "Analyze terminal log and tell which file and line number needs fix."
            ),
            Triple(
                "Device Settings Screen",
                "Android settings page with brightness slider, power saver and DND.",
                "Suggest optimal night mode settings based on current screen."
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card: Step 6 Vision Specialist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(CyanPrimaryLight.copy(alpha = 0.6f), AmberCreativeLight.copy(alpha = 0.4f))
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
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .border(1.5.dp, CyanPrimaryLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Vision Specialist",
                                tint = CyanPrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "J.A.R.V.I.S VISION SPECIALIST",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Step 6: Multimodal Screen & Image Inspection",
                                fontSize = 11.5.sp,
                                color = CyanPrimaryLight
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = "gemini-2.5-flash",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimaryLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Analyzes camera photos or device screenshots, extracts key UI alerts, and predicts the next optimal action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Visual Input HUD Preview
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
                        text = "VISUAL INPUT INSPECTOR (HUD)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (uiState.visionImageBytes != null) AmberCreative.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (uiState.visionImageBytes != null) "CUSTOM IMAGE LOADED" else "PRESET: ${uiState.visionPresetTitle.uppercase()}",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.visionImageBytes != null) AmberCreativeLight else CyanPrimaryLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Screen Frame with Scan Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .border(1.dp, CyanPrimaryLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    // Check if custom image exists
                    val customBitmap = remember(uiState.visionImageBytes) {
                        uiState.visionImageBytes?.let {
                            try {
                                BitmapFactory.decodeByteArray(it, 0, it.size)
                            } catch (_: Exception) { null }
                        }
                    }

                    if (customBitmap != null) {
                        Image(
                            bitmap = customBitmap.asImageBitmap(),
                            contentDescription = "Custom Screen Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        // Render Styled Screen Preset Simulator
                        SimulatedScreenView(presetTitle = uiState.visionPresetTitle)
                    }

                    // Scanner HUD Overlay: Laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = laserOffset.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, CyanPrimaryLight, AmberCreativeLight, Color.Transparent)
                                )
                            )
                    )

                    // Target Reticle Corner Badges
                    Text(
                        text = "SYS_SCAN::LIVE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimaryLight.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )

                    Text(
                        text = "300x300 RGB",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Buttons: Choose Custom Photo or Revert to Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_photo_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimaryLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Image", fontSize = 12.sp)
                    }

                    if (uiState.visionImageBytes != null) {
                        OutlinedButton(
                            onClick = onClearCustomImage,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Preset", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Preset Selector Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECT SAMPLE SCREEN INPUT:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePresets.forEach { (title, _, defaultPrompt) ->
                        val isSelected = uiState.visionPresetTitle == title && uiState.visionImageBytes == null
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyanPrimaryLight else Color.Transparent
                            ),
                            modifier = Modifier.clickable {
                                onSelectPreset(title, defaultPrompt)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when {
                                    title.contains("Battery") -> Icons.Default.BatteryAlert
                                    title.contains("Network") -> Icons.Default.WifiOff
                                    title.contains("Compiler") -> Icons.Default.Terminal
                                    else -> Icons.Default.Settings
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) CyanPrimaryLight else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanPrimaryLight else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Vision Prompt Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "VISION INSTRUCTION & PROMPT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.visionPrompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vision_prompt_input"),
                    placeholder = {
                        Text(
                            "Describe the contents of this image and suggest on-screen actions...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    maxLines = 3,
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = CyanPrimaryLight,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRunVisionAnalysis,
                    enabled = !uiState.isVisionAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_vision_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    if (uiState.isVisionAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing Screen Input...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Analyze",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Execute Vision Analysis (Step 6)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Vision Analysis Results Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Analysis Result",
                            tint = CyanPrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "--- Step 6: Vision Analysis ---",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimaryLight
                        )
                    }

                    if (uiState.visionResult != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (uiState.visionResult.isLiveApi) Color(0xFF10B981).copy(alpha = 0.2f) else DarkSurfaceVariant
                        ) {
                            Text(
                                text = if (uiState.visionResult.isLiveApi) "LIVE GEMINI" else "DEMO HEURISTIC",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.visionResult.isLiveApi) Color(0xFF10B981) else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val result = uiState.visionResult
                if (result == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No vision analysis performed yet.\nTap 'Execute Vision Analysis' to inspect screen input.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Section 1: Screen/Camera Analysis
                        VisionSectionCard(
                            number = "1",
                            title = "SCREEN / CAMERA CONTENT",
                            content = result.screenContent,
                            accentColor = CyanPrimaryLight
                        )

                        // Section 2: Important UI Elements & Alerts
                        VisionSectionCard(
                            number = "2",
                            title = "KEY UI ELEMENTS & CRITICAL ALERTS",
                            content = result.detectedElements,
                            accentColor = AmberCreativeLight
                        )

                        // Section 3: Next Best Visual Action
                        VisionSectionCard(
                            number = "3",
                            title = "NEXT BEST VISUAL ACTION",
                            content = result.suggestedAction,
                            accentColor = Color(0xFF10B981)
                        )

                        // Direct Action Trigger Button
                        Button(
                            onClick = onTriggerAction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("apply_action_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberCreative,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Trigger Action",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Execute Recommended Action in Step 5 Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisionSectionCard(
    number: String,
    title: String,
    content: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SimulatedScreenView(presetTitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            presetTitle.contains("Battery") -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = "Battery Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Battery Level Critical (15%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Remaining battery is low. Turn on Power Saving Mode to preserve device operation.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "Turn On Saver",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = "Dismiss",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            presetTitle.contains("Network") -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberCreativeLight),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Network Disconnected",
                            tint = AmberCreativeLight,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "HTTP 500: Server Disconnected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Unable to connect to remote gateway. No internet connection detected.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberCreative
                        ) {
                            Text(
                                text = "Retry Connection",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            presetTitle.contains("Compiler") -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "> Task :app:compileDebugKotlin FAILED",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "e: CodeArchitectureScreen.kt:367:2 Expecting '}'",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            text = "e: Unresolved reference 'sttSnippet'",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
            else -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CyanPrimaryLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Display & Battery Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Brightness Level: 80% [High]", fontSize = 10.sp, color = TextMuted)
                        Text("• Dark Theme: Enabled", fontSize = 10.sp, color = TextMuted)
                        Text("• Power Saving Mode: Disabled", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}
