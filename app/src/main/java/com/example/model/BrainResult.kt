package com.example.model

data class BrainResult(
    val problem: String,
    val analysis: String,
    val decision: String,
    val executionPlan: String,
    val rawResponse: String,
    val model: String = "gemini-2.5-pro",
    val latencyMs: Long = 0L,
    val isLiveApi: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
