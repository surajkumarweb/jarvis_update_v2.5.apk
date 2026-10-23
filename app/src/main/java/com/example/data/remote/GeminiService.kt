package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<ContentItem>,
    @Json(name = "systemInstruction") val systemInstruction: ContentItem? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    val tools: List<ToolDeclaration>? = null
)

@JsonClass(generateAdapter = true)
data class ToolDeclaration(
    val functionDeclarations: List<FunctionDeclarationItem>? = null
)

@JsonClass(generateAdapter = true)
data class FunctionDeclarationItem(
    val name: String,
    val description: String,
    val parameters: FunctionParametersItem? = null
)

@JsonClass(generateAdapter = true)
data class FunctionParametersItem(
    val type: String = "OBJECT",
    val properties: Map<String, PropertySchemaItem>? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PropertySchemaItem(
    val type: String,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    val role: String? = null,
    val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    val text: String? = null,
    val functionCall: FunctionCallItem? = null,
    @Json(name = "inline_data") val inlineData: InlineDataItem? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataItem(
    @Json(name = "mime_type") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class FunctionCallItem(
    val name: String? = null,
    val args: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<CandidateItem>? = null,
    val error: GeminiErrorItem? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    val content: ContentItem? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorItem(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

interface GeminiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
