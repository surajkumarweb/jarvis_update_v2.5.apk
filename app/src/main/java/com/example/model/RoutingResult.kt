package com.example.model

data class RoutingResult(
    val id: Long = 0L,
    val query: String,
    val detectedCategory: AgentCategory,
    val confidence: Float,
    val routerReasoning: String,
    val selectedAgentName: String,
    val responseText: String,
    val routerLatencyMs: Long,
    val agentLatencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveApi: Boolean = false,
    val mode: String = "AUTO_ROUTE"
)

enum class PipelineState {
    IDLE,
    INGESTING_QUERY,
    ROUTING_ANALYSIS,
    DISPATCHING_AGENT,
    AGENT_GENERATING,
    COMPLETED,
    ERROR
}

data class PipelineProgress(
    val state: PipelineState = PipelineState.IDLE,
    val currentStepText: String = "Ready for input",
    val detectedCategory: AgentCategory? = null,
    val routerReasoning: String? = null,
    val activeAgent: AgentProfile? = null,
    val routerDurationMs: Long = 0L,
    val agentDurationMs: Long = 0L,
    val errorMessage: String? = null
)
