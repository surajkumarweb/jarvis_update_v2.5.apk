package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgentRepository
import com.example.data.local.AppDatabase
import android.net.Uri
import com.example.model.AgentCategory
import com.example.model.AgentProfile
import com.example.model.BrainResult
import com.example.model.DeviceState
import com.example.model.PipelineProgress
import com.example.model.PipelineState
import com.example.model.RoutingResult
import com.example.model.ToolExecutionRecord
import com.example.model.VisionAnalysisResult
import com.example.model.BridgePacket
import com.example.model.ChatMessage
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

enum class OrchestrationMode {
    AUTO_ROUTE,
    MANUAL_AGENT,
    COMPARE_ALL
}

data class CompareAgentResult(
    val agent: AgentProfile,
    val responseText: String,
    val durationMs: Long
)

data class AgentRouterUiState(
    val userQuery: String = "Android background service me battery optimization kaise handle karein?",
    val mode: OrchestrationMode = OrchestrationMode.AUTO_ROUTE,
    val pipelineProgress: PipelineProgress = PipelineProgress(),
    val currentResult: RoutingResult? = null,
    val compareResults: List<CompareAgentResult> = emptyList(),
    val isComparing: Boolean = false,
    val agents: List<AgentProfile> = emptyList(),
    val selectedManualAgent: AgentProfile? = null,
    val routerInstruction: String = AgentProfile.DEFAULT_ROUTER_INSTRUCTION,
    val history: List<RoutingResult> = emptyList(),
    val customApiKey: String = "",
    val hasApiKey: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showEditAgentDialog: Boolean = false,
    val editingAgent: AgentProfile? = null,
    // Central Brain (J.A.R.V.I.S CoT Reasoner)
    val brainProblem: String = "Mujhe ek automated background job chalana hai jo battery drain na kare aur network change hone par trigger ho.",
    val brainResult: BrainResult? = null,
    val isBrainThinking: Boolean = false,
    val brainInstruction: String = AgentProfile.DEFAULT_BRAIN_INSTRUCTION,
    val brainModel: String = "gemini-2.5-pro",
    // Step 5: Autonomous Task Engine & Tool Execution
    val actionCommand: String = "Phone ki battery 15% ho gayi hai, power saving mode on karo aur screen par alert dikhao.",
    val deviceState: DeviceState = DeviceState(),
    val toolExecutionLogs: List<ToolExecutionRecord> = emptyList(),
    val isActionExecuting: Boolean = false,
    // Step 6: J.A.R.V.I.S Vision Specialist
    val visionPrompt: String = "Describe the contents of this image and suggest on-screen actions.",
    val visionImageBytes: ByteArray? = null,
    val visionImageUri: Uri? = null,
    val visionPresetTitle: String = "15% Battery Warning Screen",
    val isVisionAnalyzing: Boolean = false,
    val visionResult: VisionAnalysisResult? = null,
    // Step 7: Unified Multi-modal Chat with Session Memory
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = "seed-1",
            sender = "JARVIS",
            message = "Online and listening, sir. All ecosystem nodes (Mobile, Workstation, Smart Hub) are synchronized."
        )
    ),
    val chatInput: String = "Mera project folder /home/suraj/jarvis me hai.",
    val isChatSending: Boolean = false,
    // Step 8: J.A.R.V.I.S Ecosystem Bridge (Port 8080)
    val bridgeServerRunning: Boolean = true,
    val bridgeSelectedDevice: String = "Laptop-Workstation",
    val bridgeCommand: String = "Build status check karo aur battery drain optimize karo",
    val bridgePackets: List<BridgePacket> = emptyList(),
    val isBridgeDispatching: Boolean = false
)

class AgentRouterViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = AgentRepository(database.routingDao())

    private val _uiState = MutableStateFlow(AgentRouterUiState())
    val uiState: StateFlow<AgentRouterUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    init {
        val agents = repository.getAllAgents()
        val apiKey = repository.getEffectiveApiKey()
        _uiState.update {
            it.copy(
                agents = agents,
                selectedManualAgent = agents.firstOrNull { a -> a.category == AgentCategory.TECHNICAL },
                customApiKey = apiKey,
                hasApiKey = apiKey.isNotBlank(),
                routerInstruction = repository.getRouterInstruction(),
                brainInstruction = repository.getBrainInstruction()
            )
        }

        viewModelScope.launch {
            repository.getHistory().collect { hist ->
                _uiState.update { it.copy(history = hist) }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(userQuery = newQuery) }
    }

    fun setMode(mode: OrchestrationMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun selectManualAgent(agent: AgentProfile) {
        _uiState.update { it.copy(selectedManualAgent = agent) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun saveApiKey(key: String) {
        repository.setCustomApiKey(key)
        val effective = repository.getEffectiveApiKey()
        _uiState.update {
            it.copy(
                customApiKey = effective,
                hasApiKey = effective.isNotBlank(),
                showSettingsDialog = false
            )
        }
    }

    fun updateRouterInstruction(instruction: String) {
        repository.setRouterInstruction(instruction)
        _uiState.update { it.copy(routerInstruction = instruction) }
    }

    fun openEditAgent(agent: AgentProfile?) {
        _uiState.update {
            it.copy(
                showEditAgentDialog = true,
                editingAgent = agent
            )
        }
    }

    fun closeEditAgent() {
        _uiState.update {
            it.copy(
                showEditAgentDialog = false,
                editingAgent = null
            )
        }
    }

    fun saveAgent(agent: AgentProfile) {
        repository.addCustomAgent(agent)
        _uiState.update {
            it.copy(
                agents = repository.getAllAgents(),
                showEditAgentDialog = false,
                editingAgent = null
            )
        }
    }

    fun deleteAgent(agentId: String) {
        repository.removeCustomAgent(agentId)
        _uiState.update {
            it.copy(agents = repository.getAllAgents())
        }
    }

    fun cancelExecution() {
        activeJob?.cancel()
        _uiState.update {
            it.copy(
                pipelineProgress = PipelineProgress(
                    state = PipelineState.IDLE,
                    currentStepText = "Execution stopped"
                ),
                isComparing = false
            )
        }
    }

    fun runWorkflow() {
        val query = _uiState.value.userQuery.trim()
        if (query.isBlank()) return

        when (_uiState.value.mode) {
            OrchestrationMode.COMPARE_ALL -> runCompareAll(query)
            OrchestrationMode.AUTO_ROUTE, OrchestrationMode.MANUAL_AGENT -> runSingleWorkflow(query)
        }
    }

    private fun runSingleWorkflow(query: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            try {
                // Step 1: Ingesting query
                _uiState.update {
                    it.copy(
                        pipelineProgress = PipelineProgress(
                            state = PipelineState.INGESTING_QUERY,
                            currentStepText = "Ingesting prompt & preparing payload..."
                        ),
                        currentResult = null
                    )
                }

                val isAuto = _uiState.value.mode == OrchestrationMode.AUTO_ROUTE
                val targetCategory: AgentCategory
                val routerReasoning: String
                val routerTimeMs: Long

                if (isAuto) {
                    // Step 2: Routing Analysis
                    _uiState.update {
                        it.copy(
                            pipelineProgress = it.pipelineProgress.copy(
                                state = PipelineState.ROUTING_ANALYSIS,
                                currentStepText = "Router Agent analyzing intent & domain classification..."
                            )
                        )
                    }

                    val (routeCat, reasoning) = measureTimeMillis {
                        repository.routeQuery(query)
                    }.let { elapsed ->
                        val res = repository.routeQuery(query)
                        routerTimeMs = elapsed
                        res
                    }
                    targetCategory = routeCat
                    routerReasoning = reasoning
                } else {
                    val manualAgent = _uiState.value.selectedManualAgent ?: _uiState.value.agents.first()
                    targetCategory = manualAgent.category
                    routerReasoning = "Manual Override: User explicitly assigned query to ${manualAgent.name}."
                    routerTimeMs = 0L
                }

                // Step 3: Dispatching to Specialist
                val activeAgent = if (isAuto) {
                    _uiState.value.agents.firstOrNull { it.category == targetCategory }
                        ?: _uiState.value.agents.first()
                } else {
                    _uiState.value.selectedManualAgent ?: _uiState.value.agents.first()
                }

                _uiState.update {
                    it.copy(
                        pipelineProgress = PipelineProgress(
                            state = PipelineState.DISPATCHING_AGENT,
                            currentStepText = "Dispatching context to ${activeAgent.name}...",
                            detectedCategory = targetCategory,
                            routerReasoning = routerReasoning,
                            activeAgent = activeAgent,
                            routerDurationMs = routerTimeMs
                        )
                    )
                }

                // Step 4: Specialist Generating
                _uiState.update {
                    it.copy(
                        pipelineProgress = it.pipelineProgress.copy(
                            state = PipelineState.AGENT_GENERATING,
                            currentStepText = "${activeAgent.name} synthesizing specialized response..."
                        )
                    )
                }

                var agentResponse = ""
                val agentTimeMs = measureTimeMillis {
                    agentResponse = repository.executeSpecialist(query, activeAgent)
                }

                val finalResult = RoutingResult(
                    query = query,
                    detectedCategory = targetCategory,
                    confidence = if (isAuto) 0.94f else 1.0f,
                    routerReasoning = routerReasoning,
                    selectedAgentName = activeAgent.name,
                    responseText = agentResponse,
                    routerLatencyMs = routerTimeMs,
                    agentLatencyMs = agentTimeMs,
                    isLiveApi = _uiState.value.hasApiKey,
                    mode = _uiState.value.mode.name
                )

                repository.saveResult(finalResult)

                _uiState.update {
                    it.copy(
                        pipelineProgress = PipelineProgress(
                            state = PipelineState.COMPLETED,
                            currentStepText = "Execution finished successfully",
                            detectedCategory = targetCategory,
                            routerReasoning = routerReasoning,
                            activeAgent = activeAgent,
                            routerDurationMs = routerTimeMs,
                            agentDurationMs = agentTimeMs
                        ),
                        currentResult = finalResult
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        pipelineProgress = PipelineProgress(
                            state = PipelineState.ERROR,
                            currentStepText = "Workflow failed",
                            errorMessage = e.localizedMessage ?: "Unknown execution error"
                        )
                    )
                }
            }
        }
    }

    private fun runCompareAll(query: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isComparing = true,
                    compareResults = emptyList(),
                    currentResult = null
                )
            }

            val agentsToCompare = _uiState.value.agents.take(3)
            val results = mutableListOf<CompareAgentResult>()

            for (agent in agentsToCompare) {
                var response = ""
                val duration = measureTimeMillis {
                    response = repository.executeSpecialist(query, agent)
                }
                results.add(CompareAgentResult(agent, response, duration))
                _uiState.update { it.copy(compareResults = results.toList()) }
            }

            _uiState.update { it.copy(isComparing = false) }
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun deleteHistoryItem(item: RoutingResult) {
        viewModelScope.launch {
            repository.deleteHistoryItem(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Central Brain (J.A.R.V.I.S) Methods
    fun onBrainProblemChange(problem: String) {
        _uiState.update { it.copy(brainProblem = problem) }
    }

    fun updateBrainInstruction(instruction: String) {
        repository.setBrainInstruction(instruction)
        _uiState.update { it.copy(brainInstruction = instruction) }
    }

    fun updateBrainModel(model: String) {
        _uiState.update { it.copy(brainModel = model) }
    }

    fun runCentralBrain() {
        val problem = _uiState.value.brainProblem.trim()
        if (problem.isBlank() || _uiState.value.isBrainThinking) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBrainThinking = true) }
            val result = repository.executeCentralBrain(
                problem = problem,
                modelName = _uiState.value.brainModel
            )
            _uiState.update {
                it.copy(
                    isBrainThinking = false,
                    brainResult = result
                )
            }
        }
    }

    // Step 5: Autonomous Action Engine Methods
    fun onActionCommandChange(command: String) {
        _uiState.update { it.copy(actionCommand = command) }
    }

    fun runAutonomousAction() {
        val command = _uiState.value.actionCommand.trim()
        if (command.isBlank() || _uiState.value.isActionExecuting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isActionExecuting = true) }
            val (records, newState) = repository.executeAutonomousAction(
                command = command,
                currentDeviceState = _uiState.value.deviceState
            )
            _uiState.update {
                it.copy(
                    isActionExecuting = false,
                    deviceState = newState,
                    toolExecutionLogs = records + it.toolExecutionLogs
                )
            }
        }
    }

    fun dismissNotification(id: String) {
        _uiState.update {
            it.copy(
                deviceState = it.deviceState.copy(
                    notifications = it.deviceState.notifications.filterNot { n -> n.id == id }
                )
            )
        }
    }

    fun toggleDevicePowerSaving(enabled: Boolean) {
        _uiState.update {
            it.copy(
                deviceState = it.deviceState.copy(
                    powerSavingMode = enabled,
                    batteryPercent = if (enabled) 15 else 85
                )
            )
        }
    }

    fun clearActionLogs() {
        _uiState.update { it.copy(toolExecutionLogs = emptyList()) }
    }

    // Step 6: Vision Specialist Methods
    fun onVisionPromptChange(prompt: String) {
        _uiState.update { it.copy(visionPrompt = prompt) }
    }

    fun onSelectVisionPreset(presetTitle: String, prompt: String = "Describe the contents of this image and suggest on-screen actions.") {
        _uiState.update {
            it.copy(
                visionPresetTitle = presetTitle,
                visionPrompt = prompt,
                visionImageBytes = null,
                visionImageUri = null
            )
        }
    }

    fun setVisionCustomImage(bytes: ByteArray, uri: Uri?) {
        _uiState.update {
            it.copy(
                visionImageBytes = bytes,
                visionImageUri = uri,
                visionPresetTitle = "Custom Screen Image"
            )
        }
    }

    fun clearVisionCustomImage() {
        _uiState.update {
            it.copy(
                visionImageBytes = null,
                visionImageUri = null,
                visionPresetTitle = "15% Battery Warning Screen"
            )
        }
    }

    fun runVisionAnalysis() {
        if (_uiState.value.isVisionAnalyzing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVisionAnalyzing = true) }
            val imageBytes = _uiState.value.visionImageBytes ?: ByteArray(0)
            val result = repository.analyzeVisionImage(
                imageBytes = imageBytes,
                mimeType = "image/jpeg",
                prompt = _uiState.value.visionPrompt.ifBlank { "Describe the contents of this image and suggest on-screen actions." },
                sampleTitle = _uiState.value.visionPresetTitle
            )
            _uiState.update {
                it.copy(
                    isVisionAnalyzing = false,
                    visionResult = result
                )
            }
        }
    }

    // Step 7: Unified Session Memory Chat
    fun onChatInputChange(input: String) {
        _uiState.update { it.copy(chatInput = input) }
    }

    fun sendChatMessage() {
        val message = _uiState.value.chatInput.trim()
        if (message.isBlank() || _uiState.value.isChatSending) return

        val userChat = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            message = message
        )
        val currentHistory = _uiState.value.chatMessages + userChat
        _uiState.update {
            it.copy(
                chatMessages = currentHistory,
                chatInput = "",
                isChatSending = true
            )
        }

        viewModelScope.launch {
            val reply = repository.executeUnifiedChat(currentHistory, message)
            val jarvisChat = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "JARVIS",
                message = reply
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + jarvisChat,
                    isChatSending = false
                )
            }
        }
    }

    fun clearChatHistory() {
        _uiState.update {
            it.copy(
                chatMessages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = "JARVIS",
                        message = "Memory cleared. Ready for new context, sir."
                    )
                )
            )
        }
    }

    // Step 8: Ecosystem Bridge Methods
    fun onBridgeDeviceChange(deviceId: String) {
        _uiState.update { it.copy(bridgeSelectedDevice = deviceId) }
    }

    fun onBridgeCommandChange(command: String) {
        _uiState.update { it.copy(bridgeCommand = command) }
    }

    fun toggleBridgeServer(running: Boolean) {
        _uiState.update { it.copy(bridgeServerRunning = running) }
    }

    fun sendBridgeRequest() {
        val command = _uiState.value.bridgeCommand.trim()
        val deviceId = _uiState.value.bridgeSelectedDevice
        if (command.isBlank() || _uiState.value.isBridgeDispatching || !_uiState.value.bridgeServerRunning) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBridgeDispatching = true) }
            val packet = repository.executeBridgeCommand(deviceId, command)
            _uiState.update {
                it.copy(
                    isBridgeDispatching = false,
                    bridgePackets = listOf(packet) + it.bridgePackets
                )
            }
        }
    }

    fun clearBridgePackets() {
        _uiState.update { it.copy(bridgePackets = emptyList()) }
    }
}
