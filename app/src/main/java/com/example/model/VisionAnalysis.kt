package com.example.model

data class VisionAnalysisResult(
    val id: String,
    val rawResponse: String,
    val screenContent: String,
    val detectedElements: String,
    val suggestedAction: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveApi: Boolean = false,
    val latencyMs: Long = 0L,
    val sampleTitle: String? = null
)

data class SampleVisionImage(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val defaultPrompt: String = "Describe the contents of this image and suggest on-screen actions."
)
