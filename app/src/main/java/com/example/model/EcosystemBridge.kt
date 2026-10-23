package com.example.model

data class BridgePacket(
    val id: String,
    val deviceId: String,
    val command: String,
    val status: String = "success", // "success" or "error"
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val port: Int = 8080,
    val httpMethod: String = "POST",
    val latencyMs: Long = 0L
)

data class ConnectedDeviceNode(
    val id: String,
    val name: String,
    val type: String, // "Mobile", "Workstation", "IoT Hub", "Wearable"
    val ipAddress: String,
    val isOnline: Boolean = true
)

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "JARVIS"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
