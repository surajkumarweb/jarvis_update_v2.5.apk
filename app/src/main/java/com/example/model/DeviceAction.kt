package com.example.model

data class ToolCall(
    val name: String,
    val args: Map<String, String>
)

data class ToolExecutionRecord(
    val id: String,
    val toolName: String,
    val args: Map<String, String>,
    val output: String,
    val status: ExecutionStatus = ExecutionStatus.SUCCESS,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ExecutionStatus {
    PENDING,
    EXECUTING,
    SUCCESS,
    FAILED
}

data class DeviceState(
    val batteryPercent: Int = 15,
    val powerSavingMode: Boolean = false,
    val brightnessPercent: Int = 80,
    val doNotDisturb: Boolean = false,
    val notifications: List<DeviceNotification> = emptyList()
)

data class DeviceNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
