package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AgentCategory
import com.example.model.RoutingResult

@Entity(tableName = "routing_history")
data class RoutingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val query: String,
    val category: String,
    val confidence: Float,
    val routerReasoning: String,
    val selectedAgentName: String,
    val responseText: String,
    val routerLatencyMs: Long,
    val agentLatencyMs: Long,
    val timestamp: Long,
    val isLiveApi: Boolean,
    val isFavorite: Boolean = false
) {
    fun toDomain(): RoutingResult = RoutingResult(
        id = id,
        query = query,
        detectedCategory = AgentCategory.fromString(category),
        confidence = confidence,
        routerReasoning = routerReasoning,
        selectedAgentName = selectedAgentName,
        responseText = responseText,
        routerLatencyMs = routerLatencyMs,
        agentLatencyMs = agentLatencyMs,
        timestamp = timestamp,
        isLiveApi = isLiveApi
    )

    companion object {
        fun fromDomain(domain: RoutingResult, isFavorite: Boolean = false): RoutingEntity = RoutingEntity(
            id = domain.id,
            query = domain.query,
            category = domain.detectedCategory.name,
            confidence = domain.confidence,
            routerReasoning = domain.routerReasoning,
            selectedAgentName = domain.selectedAgentName,
            responseText = domain.responseText,
            routerLatencyMs = domain.routerLatencyMs,
            agentLatencyMs = domain.agentLatencyMs,
            timestamp = domain.timestamp,
            isLiveApi = domain.isLiveApi,
            isFavorite = isFavorite
        )
    }
}
