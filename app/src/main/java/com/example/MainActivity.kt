package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ApkInstallerDialog
import com.example.ui.components.VoiceTranscriptionDialog
import com.example.ui.screens.ActionEngineScreen
import com.example.ui.screens.AgentsStudioScreen
import com.example.ui.screens.CentralBrainScreen
import com.example.ui.screens.CodeArchitectureScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.EcosystemBridgeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OrchestratorScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.VisionSpecialistScreen
import com.example.ui.theme.CyanPrimaryLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AgentRouterViewModel

enum class MainNavDestination(
    val title: String,
    val icon: ImageVector,
    val showInBottomNav: Boolean = true
) {
    BRAIN("J.A.R.V.I.S", Icons.Default.Psychology, true),
    ACTIONS("Actions", Icons.Default.Bolt, true),
    VISION("Vision", Icons.Default.Visibility, true),
    ROUTER("Router", Icons.Default.AltRoute, true),
    CODE("Code", Icons.Default.Code, true),
    BRIDGE("Bridge", Icons.Default.Router, false),
    COMPARE("Compare", Icons.Default.CompareArrows, false),
    STUDIO("Studio", Icons.Default.AutoAwesome, false)
}

class MainActivity : ComponentActivity() {
    private val viewModel: AgentRouterViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsState()
                var currentDestination by remember { mutableStateOf(MainNavDestination.BRAIN) }
                var showHistorySheet by remember { mutableStateOf(false) }
                var showVoiceDialog by remember { mutableStateOf(false) }
                var showApkInstallerDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "J.A.R.V.I.S Brain",
                                        tint = CyanPrimaryLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "J.A.R.V.I.S",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (uiState.hasApiKey) Color(0xFF10B981).copy(alpha = 0.2f)
                                                else Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (uiState.hasApiKey) "LIVE" else "DEMO",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.hasApiKey) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { showVoiceDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Speech-to-Text Voice Input",
                                        tint = CyanPrimaryLight
                                    )
                                }
                                IconButton(onClick = { showApkInstallerDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = "APK Installer & System Updater",
                                        tint = CyanPrimaryLight
                                    )
                                }
                                IconButton(onClick = { currentDestination = MainNavDestination.BRIDGE }) {
                                    Icon(
                                        imageVector = Icons.Default.Router,
                                        contentDescription = "Ecosystem Bridge (:8080)",
                                        tint = if (currentDestination == MainNavDestination.BRIDGE) CyanPrimaryLight else TextSecondary
                                    )
                                }
                                IconButton(onClick = { currentDestination = MainNavDestination.COMPARE }) {
                                    Icon(
                                        imageVector = Icons.Default.CompareArrows,
                                        contentDescription = "Compare Models",
                                        tint = if (currentDestination == MainNavDestination.COMPARE) CyanPrimaryLight else TextSecondary
                                    )
                                }
                                IconButton(onClick = { currentDestination = MainNavDestination.STUDIO }) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Custom Agents Studio",
                                        tint = if (currentDestination == MainNavDestination.STUDIO) CyanPrimaryLight else TextSecondary
                                    )
                                }
                                IconButton(onClick = { showHistorySheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Logs",
                                        tint = TextSecondary
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleSettingsDialog(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = DarkSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface,
                            contentColor = TextPrimary
                        ) {
                            MainNavDestination.values().filter { it.showInBottomNav }.forEach { destination ->
                                val selected = currentDestination == destination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.title,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkBackground)
                    ) {
                        when (currentDestination) {
                            MainNavDestination.BRAIN -> {
                                CentralBrainScreen(
                                    uiState = uiState,
                                    onProblemChange = viewModel::onBrainProblemChange,
                                    onRunCentralBrain = viewModel::runCentralBrain,
                                    onUpdateBrainInstruction = viewModel::updateBrainInstruction,
                                    onOpenVoiceTranscriber = { showVoiceDialog = true },
                                    onOpenSettings = { viewModel.toggleSettingsDialog(true) }
                                )
                            }
                            MainNavDestination.ACTIONS -> {
                                ActionEngineScreen(
                                    uiState = uiState,
                                    onCommandChange = viewModel::onActionCommandChange,
                                    onRunAction = viewModel::runAutonomousAction,
                                    onDismissNotification = viewModel::dismissNotification,
                                    onTogglePowerSaving = viewModel::toggleDevicePowerSaving,
                                    onClearLogs = viewModel::clearActionLogs,
                                    onOpenVoiceTranscriber = { showVoiceDialog = true }
                                )
                            }
                            MainNavDestination.VISION -> {
                                VisionSpecialistScreen(
                                    uiState = uiState,
                                    onPromptChange = viewModel::onVisionPromptChange,
                                    onSelectPreset = viewModel::onSelectVisionPreset,
                                    onSetCustomImage = viewModel::setVisionCustomImage,
                                    onClearCustomImage = viewModel::clearVisionCustomImage,
                                    onRunVisionAnalysis = viewModel::runVisionAnalysis,
                                    onTriggerAction = {
                                        val suggested = uiState.visionResult?.suggestedAction ?: ""
                                        if (suggested.contains("power_saving", ignoreCase = true) || suggested.contains("battery", ignoreCase = true)) {
                                            viewModel.onActionCommandChange("Phone ki battery 15% ho gayi hai, power saving mode on karo aur screen par alert dikhao.")
                                        } else if (suggested.contains("brightness", ignoreCase = true)) {
                                            viewModel.onActionCommandChange("Raat ho gayi hai, brightness 10% karo aur do not disturb mode on karo.")
                                        }
                                        currentDestination = MainNavDestination.ACTIONS
                                        viewModel.runAutonomousAction()
                                    }
                                )
                            }
                            MainNavDestination.BRIDGE -> {
                                EcosystemBridgeScreen(
                                    uiState = uiState,
                                    onDeviceChange = viewModel::onBridgeDeviceChange,
                                    onCommandChange = viewModel::onBridgeCommandChange,
                                    onToggleServer = viewModel::toggleBridgeServer,
                                    onSendBridgeRequest = viewModel::sendBridgeRequest,
                                    onClearBridgePackets = viewModel::clearBridgePackets,
                                    onChatInputChange = viewModel::onChatInputChange,
                                    onSendChatMessage = viewModel::sendChatMessage,
                                    onClearChat = viewModel::clearChatHistory
                                )
                            }
                            MainNavDestination.ROUTER -> {
                                OrchestratorScreen(
                                    uiState = uiState,
                                    onQueryChange = viewModel::onQueryChange,
                                    onModeChange = viewModel::setMode,
                                    onSelectManualAgent = viewModel::selectManualAgent,
                                    onRunWorkflow = viewModel::runWorkflow,
                                    onCancelExecution = viewModel::cancelExecution,
                                    onOpenVoiceTranscriber = { showVoiceDialog = true },
                                    onOpenSettings = { viewModel.toggleSettingsDialog(true) }
                                )
                            }
                            MainNavDestination.COMPARE -> {
                                CompareScreen(
                                    uiState = uiState,
                                    onQueryChange = viewModel::onQueryChange,
                                    onRunCompare = viewModel::runWorkflow
                                )
                            }
                            MainNavDestination.STUDIO -> {
                                AgentsStudioScreen(
                                    uiState = uiState,
                                    onOpenEditAgent = viewModel::openEditAgent,
                                    onCloseEditAgent = viewModel::closeEditAgent,
                                    onSaveAgent = viewModel::saveAgent,
                                    onDeleteAgent = viewModel::deleteAgent,
                                    onUpdateRouterInstruction = viewModel::updateRouterInstruction
                                )
                            }
                            MainNavDestination.CODE -> {
                                CodeArchitectureScreen()
                            }
                        }

                        // Speech-to-Text Multimodal Voice Dialog (Step 4)
                        if (showVoiceDialog) {
                            VoiceTranscriptionDialog(
                                onDismiss = { showVoiceDialog = false },
                                onTranscribed = { text, target ->
                                    if (target == "BRAIN") {
                                        viewModel.onBrainProblemChange(text)
                                        currentDestination = MainNavDestination.BRAIN
                                        viewModel.runCentralBrain()
                                    } else {
                                        viewModel.onQueryChange(text)
                                        currentDestination = MainNavDestination.ROUTER
                                        viewModel.runWorkflow()
                                    }
                                }
                            )
                        }

                        // History Bottom Sheet
                        if (showHistorySheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showHistorySheet = false },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = DarkBackground,
                                dragHandle = null
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                                    HistoryScreen(
                                        history = uiState.history,
                                        onDeleteItem = viewModel::deleteHistoryItem,
                                        onClearAll = viewModel::clearAllHistory
                                    )
                                }
                            }
                        }

                        // Settings Dialog
                        if (uiState.showSettingsDialog) {
                            SettingsDialog(
                                currentApiKey = uiState.customApiKey,
                                onDismiss = { viewModel.toggleSettingsDialog(false) },
                                onSaveApiKey = viewModel::saveApiKey
                            )
                        }

                        // Autonomous APK Installer & System Updater Dialog (Step 9)
                        if (showApkInstallerDialog) {
                            ApkInstallerDialog(
                                onDismissRequest = { showApkInstallerDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
