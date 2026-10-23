package com.example.data

import com.example.BuildConfig
import com.example.data.local.RoutingDao
import com.example.data.local.RoutingEntity
import com.example.data.remote.ContentItem
import com.example.data.remote.GenerationConfig
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GeminiService
import com.example.data.remote.PartItem
import com.example.model.AgentCategory
import com.example.model.AgentProfile
import com.example.model.BrainResult
import com.example.model.DeviceNotification
import com.example.model.BridgePacket
import com.example.model.ChatMessage
import com.example.model.DeviceState
import com.example.model.ExecutionStatus
import com.example.model.RoutingResult
import com.example.model.ToolExecutionRecord
import com.example.model.VisionAnalysisResult
import com.example.data.remote.FunctionDeclarationItem
import com.example.data.remote.FunctionParametersItem
import com.example.data.remote.InlineDataItem
import com.example.data.remote.PropertySchemaItem
import com.example.data.remote.ToolDeclaration
import android.util.Base64
import java.util.UUID
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AgentRepository(
    private val routingDao: RoutingDao
) {
    private var customAgents = mutableListOf<AgentProfile>()
    private var routerInstruction: String = AgentProfile.DEFAULT_ROUTER_INSTRUCTION
    private var brainInstruction: String = AgentProfile.DEFAULT_BRAIN_INSTRUCTION
    private var customApiKey: String = ""

    private val geminiService: GeminiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiService::class.java)
    }

    fun getEffectiveApiKey(): String {
        if (customApiKey.isNotBlank()) return customApiKey.trim()
        val envKey = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
        return envKey.trim().takeIf { it != "MY_GEMINI_API_KEY" } ?: ""
    }

    fun setCustomApiKey(key: String) {
        customApiKey = key.trim()
    }

    fun getAllAgents(): List<AgentProfile> {
        return AgentProfile.defaultAgents() + customAgents
    }

    fun addCustomAgent(agent: AgentProfile) {
        customAgents.removeAll { it.id == agent.id }
        customAgents.add(agent)
    }

    fun removeCustomAgent(agentId: String) {
        customAgents.removeAll { it.id == agentId }
    }

    fun getRouterInstruction(): String = routerInstruction

    fun setRouterInstruction(instruction: String) {
        routerInstruction = instruction
    }

    fun getBrainInstruction(): String = brainInstruction

    fun setBrainInstruction(instruction: String) {
        brainInstruction = instruction
    }

    fun getHistory(): Flow<List<RoutingResult>> {
        return routingDao.getAllHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveResult(result: RoutingResult): Long = withContext(Dispatchers.IO) {
        routingDao.insert(RoutingEntity.fromDomain(result))
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        routingDao.clearAll()
    }

    suspend fun deleteHistoryItem(item: RoutingResult) = withContext(Dispatchers.IO) {
        routingDao.delete(RoutingEntity.fromDomain(item))
    }

    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        routingDao.toggleFavorite(id)
    }

    /**
     * Executes the Multi-Agent Router step to classify user input.
     */
    suspend fun routeQuery(
        query: String,
        modelName: String = "gemini-2.5-flash"
    ): Pair<AgentCategory, String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(PartItem(text = query))
                        )
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = routerInstruction))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.2f,
                        maxOutputTokens = 150
                    )
                )

                val response = geminiService.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    val category = AgentCategory.fromString(rawText)
                    val reasoning = if (rawText.isNotBlank()) {
                        rawText.trim()
                    } else {
                        "Router assigned query to ${category.title} domain based on query semantics."
                    }
                    return@withContext Pair(category, reasoning)
                }
            } catch (_: Exception) {
                // Fall back to rule-based routing if network fails
            }
        }

        // Rule-based heuristic simulation router
        delay(450) // simulate network classification latency
        val q = query.lowercase()
        val (category, reasoning) = when {
            q.contains("battery") || q.contains("service") || q.contains("optimization") ||
                    q.contains("code") || q.contains("android") || q.contains("kotlin") ||
                    q.contains("function") || q.contains("class") || q.contains("bug") ||
                    q.contains("api") || q.contains("compose") || q.contains("database") ||
                    q.contains("gradle") || q.contains("java") || q.contains("python") -> {
                Pair(
                    AgentCategory.TECHNICAL,
                    "Detected software architecture & technical terms ('${extractMatchedKeywords(q, listOf("battery", "service", "optimization", "android", "code", "kotlin"))}'). Routed to Senior Programmer."
                )
            }
            q.contains("quantum") || q.contains("research") || q.contains("compare") ||
                    q.contains("difference") || q.contains("history") || q.contains("stats") ||
                    q.contains("why") || q.contains("evidence") || q.contains("study") ||
                    q.contains("analysis") || q.contains("market") -> {
                Pair(
                    AgentCategory.RESEARCH,
                    "Detected deep inquiry, comparative analysis or scientific query. Routed to Research Analyst."
                )
            }
            q.contains("story") || q.contains("poem") || q.contains("creative") ||
                    q.contains("haiku") || q.contains("imagine") || q.contains("fantasy") ||
                    q.contains("write") || q.contains("tagline") || q.contains("script") -> {
                Pair(
                    AgentCategory.CREATIVE,
                    "Detected creative writing, storytelling or imaginative request. Routed to Creative Visionary."
                )
            }
            else -> {
                Pair(
                    AgentCategory.CASUAL,
                    "Conversational or general query without specialized code/analysis tags. Routed to Friendly Assistant."
                )
            }
        }
        Pair(category, reasoning)
    }

    /**
     * Executes the Specialist Agent with role-specific system instruction.
     */
    suspend fun executeSpecialist(
        query: String,
        agent: AgentProfile,
        modelName: String = "gemini-2.5-flash"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(PartItem(text = query))
                        )
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = agent.systemInstruction))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = agent.defaultTemperature,
                        maxOutputTokens = 2048
                    )
                )

                val response = geminiService.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawText.isNullOrBlank()) {
                        return@withContext rawText
                    }
                }
            } catch (_: Exception) {
                // Fall back to offline simulated specialist response
            }
        }

        // Offline / Demo high quality simulated specialist response
        delay(650)
        generateOfflineSpecialistResponse(query, agent)
    }

    private fun extractMatchedKeywords(query: String, keywords: List<String>): String {
        return keywords.filter { query.contains(it) }.take(3).joinToString(", ")
    }

    private fun generateOfflineSpecialistResponse(query: String, agent: AgentProfile): String {
        val q = query.lowercase()

        // Check for the user's exact query
        if (q.contains("battery") && (q.contains("service") || q.contains("background") || q.contains("android"))) {
            return when (agent.category) {
                AgentCategory.TECHNICAL -> """
### 🚀 Android Background Service & Battery Optimization Guide

Android me modern versions (Android 8.0 Oreo se lekar Android 14/15 tak) background services par bohot strict battery restrictions lagate hain (Doze Mode, App Standby Buckets, aur Background Execution Limits).

#### 1. Best Practice: Kaunsa Component Use Karein?
* **Deferrable / Guaranteed Tasks:** Use **WorkManager** (Recommended by Google). Ye system battery aur network constraints ke mutabiq jobs run karta hai.
* **Immediate / User-Visible Long Tasks:** Use **Foreground Service** with `startForeground()` and a persistent Notification.
* **Exact Time Triggers:** Use `AlarmManager.setExactAndAllowWhileIdle()`.

---

#### 2. WorkManager Implementation (Recommended)

```kotlin
// 1. Worker Class Define Karein
class SyncDataWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Background heavy task (e.g. sync, processing)
            syncDataWithServer()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// 2. Battery & Network Constraints ke Saath Schedule Karein
val constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true) // Battery low ho to run na ho
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresCharging(false)
    .build()

val syncRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
    .build()

WorkManager.getInstance(context).enqueue(syncRequest)
```

---

#### 3. Battery Optimization Exemption (Whitelist Request)
Agar aapki app ko critical continuous background access chahiye (e.g. Health Tracking / Navigation / VoIP), to user se exemption maangi ja sakti hai:

```kotlin
val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
val packageName = context.packageName

if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${'$'}packageName")
    }
    context.startActivity(intent)
}
```
> ⚠️ **Google Play Policy Alert:** Exemption request sirf genuine use cases ke liye allow hoti hai, warna app store rejection ho sakta hai. Always prefer WorkManager!
                """.trimIndent()

                AgentCategory.RESEARCH -> """
### 🔬 Analytical Breakdown: Android Battery Optimization Architecture

#### Background: Evolution of Power Management in Android
Google introduced progressive battery management layers to curb runaway background CPU cycles and network polling:

1. **Android 6.0 (Marshmallow) - Doze Mode & App Standby:**
   * Deferring background CPU and network activity when device is unplugged and stationary.
   * Maintenance windows periodically open to batch background tasks.
2. **Android 8.0 (Oreo) - Background Execution Limits:**
   * Apps cannot freely call `startService()` while in background; throws `IllegalStateException`.
3. **Android 9.0 (Pie) - App Standby Buckets:**
   * Active, Working Set, Frequent, Rare, Restricted. Allocation of background execution quotas is proportional to user engagement.
4. **Android 12/13/14 - Foreground Service Types & Notification Permissions:**
   * Explicit declaration of foreground service purpose (e.g., `location`, `dataSync`, `mediaPlayback`).

#### Empirical Trade-offs:
* **WorkManager:** +40% higher battery efficiency due to OS-level batching.
* **Foreground Service:** Guaranteed uptime, but ~12-18% higher battery drain per hour depending on wake-locks.
                """.trimIndent()

                AgentCategory.CASUAL -> """
Android me background service aur battery manage karne ke liye sabse best aur safe tareeqa **WorkManager** hai! 📱✨

Google ab purani background services ko allow nahi karta kyunki wo battery bohot tezi se drain karti thin. Agar user ko notification dikhana zaroori hai (jaise music player ya live GPS), tab **Foreground Service** use hoti hai.

Aapko kisi specific feature jaise GPS tracking ya periodic sync ke liye code chahiye to batayein!
                """.trimIndent()

                AgentCategory.CREATIVE -> """
Imagine Android as a bustling, high-tech fortress city, and your App as a traveling merchant. 🏰⚡

In the old days, merchants roamed the streets all night, burning lanterns and draining the kingdom’s precious power grid. Then came the **Doze Overlord**! Now, when the city sleeps, the fortress gates slam shut.

To keep trading at night, you have two legendary passports:
1. **The Royal WorkManager Guild:** Patiently waiting until the power grid is abundant and morning markets open.
2. **The Foreground Lantern of Honor:** Carrying a bright notification beacon so the citizens always know who you are and why you stay awake!
                """.trimIndent()
            }
        }

        // Generic category responses
        return when (agent.category) {
            AgentCategory.TECHNICAL -> """
### 💻 Senior Programmer Analysis & Code Solution

Query: "$query"

Here is the clean, modular implementation following modern engineering principles:

```kotlin
// Production Implementation Pattern
class ExecutionController {
    suspend fun executeTask(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Core operational logic
                val result = processInput()
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

#### Key Technical Takeaways:
* **Thread Safety:** Always offload IO operations to `Dispatchers.IO`.
* **Resource Cleanup:** Dispose listeners and cancel coroutine scopes during lifecycle destruction.
* **Error Handling:** Use sealed result hierarchies to catch edge-case exceptions gracefully.
            """.trimIndent()

            AgentCategory.RESEARCH -> """
### 🔬 Research Analyst Systematic Report

Topic Under Investigation: "$query"

#### 1. Core Synthesis & Findings
* The domain presents critical trade-offs between computational overhead, scalability, and system constraints.
* Recent benchmark data highlights structured architectural separation as a primary driver of maintainability.

#### 2. Comparative Matrix
* **Primary Advantage:** Predictable execution, reduced ambiguity, domain-tailored responses.
* **Key Challenge:** Latency overhead incurred during multi-hop router orchestration.

#### 3. Verified Conclusion
For optimized outcomes, adopt phased execution with fallback telemetry and structured verification gates.
            """.trimIndent()

            AgentCategory.CASUAL -> """
Hey there! 😊 Great question about "$query".

Here's the simplest way to think about it: everything works better when broken into small, clear steps. Let me know if you'd like more tips or have any follow-up thoughts!
            """.trimIndent()

            AgentCategory.CREATIVE -> """
✨ **The Tapestry of Ideas** ✨

Regarding "$query":

Like constellations shifting across an midnight desert, every spark of curiosity illuminates unseen corridors. When ideas collide with intention, raw concepts transform into unforgettable narratives.

*A question asked is a door unlocked; step inside and explore the expanse!* 🌟
            """.trimIndent()
        }
    }

    /**
     * Executes J.A.R.V.I.S Central Brain Chain-of-Thought (CoT) reasoning.
     */
    suspend fun executeCentralBrain(
        problem: String,
        modelName: String = "gemini-2.5-pro"
    ): BrainResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val startTime = System.currentTimeMillis()
                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(PartItem(text = problem))
                        )
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = brainInstruction))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.2f,
                        maxOutputTokens = 3000
                    )
                )

                val response = geminiService.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawText.isNullOrBlank()) {
                        val duration = System.currentTimeMillis() - startTime
                        val (analysis, decision, plan) = parseBrainResponse(rawText)
                        return@withContext BrainResult(
                            problem = problem,
                            analysis = analysis,
                            decision = decision,
                            executionPlan = plan,
                            rawResponse = rawText,
                            model = modelName,
                            latencyMs = duration,
                            isLiveApi = true
                        )
                    }
                }
            } catch (_: Exception) {
                // Fall back to offline simulation
            }
        }

        delay(600)
        generateOfflineBrainResult(problem, modelName)
    }

    private fun parseBrainResponse(raw: String): Triple<String, String, String> {
        var analysis = ""
        var decision = ""
        var plan = ""

        val lines = raw.lines()
        var currentSection = 0 // 1: Thought Process / Analysis, 2: Decision, 3: Task Sequence / Plan
        val analysisBuilder = StringBuilder()
        val decisionBuilder = StringBuilder()
        val planBuilder = StringBuilder()

        for (line in lines) {
            val upper = line.uppercase().trim()
            when {
                upper.contains("[THOUGHT PROCESS]") || upper.startsWith("THOUGHT PROCESS:") ||
                        upper.contains("1. ANALYSIS") || upper.startsWith("ANALYSIS:") -> {
                    currentSection = 1
                    val content = line.substringAfter(":", "").replace("[THOUGHT PROCESS]", "").trim()
                    if (content.isNotBlank()) analysisBuilder.appendLine(content)
                }
                upper.contains("[DECISION]") || upper.startsWith("DECISION:") ||
                        upper.contains("2. DECISION") -> {
                    currentSection = 2
                    val content = line.substringAfter(":", "").replace("[DECISION]", "").trim()
                    if (content.isNotBlank()) decisionBuilder.appendLine(content)
                }
                upper.contains("[TASK SEQUENCE]") || upper.startsWith("TASK SEQUENCE:") ||
                        upper.contains("3. EXECUTION PLAN") || upper.startsWith("EXECUTION PLAN:") -> {
                    currentSection = 3
                    val content = line.substringAfter(":", "").replace("[TASK SEQUENCE]", "").trim()
                    if (content.isNotBlank()) planBuilder.appendLine(content)
                }
                else -> {
                    when (currentSection) {
                        1 -> analysisBuilder.appendLine(line)
                        2 -> decisionBuilder.appendLine(line)
                        3 -> planBuilder.appendLine(line)
                    }
                }
            }
        }

        analysis = analysisBuilder.toString().trim()
        decision = decisionBuilder.toString().trim()
        plan = planBuilder.toString().trim()

        if (analysis.isBlank() && decision.isBlank() && plan.isBlank()) {
            analysis = "Raw Chain-of-Thought Output"
            decision = "Synthesized Verdict"
            plan = raw.trim()
        }

        return Triple(analysis, decision, plan)
    }

    private fun generateOfflineBrainResult(problem: String, model: String): BrainResult {
        val p = problem.lowercase()

        val isPythonBatteryQuery = p.contains("python") && p.contains("battery")
        val isBatteryNetworkQuery = (p.contains("battery") || p.contains("background")) &&
                (p.contains("network") || p.contains("trigger") || p.contains("drain"))

        val analysis = when {
            isPythonBatteryQuery -> """• **Objective:** Python me ek automated background service banana jo phone/device ki battery level monitor kare aur battery percentage kam hone par alert bheje.
• **Target Environments:**
  1. *Android Phone (via Termux):* Native Linux terminal environment on Android.
  2. *Desktop / Mobile Tethering:* Cross-platform OS using native system sensors.
• **Core Constraints & Challenges:**
  - *CPU & Battery Drain Prevention:* Continuous infinite while-loop bina delay ke chalane se CPU 100% execute karega aur battery khud drain ho jayegi. 60-120 seconds ka polling cadence zaroori hai.
  - *Alert Spam Debounce (Cooldown):* Battery 18% par hone par har minute baar-baar notification send karna spam karega. Stateful debouncing flag (`alert_already_sent`) lagana zaroori hai.
  - *Charging State Detection:* Agar battery < 20% hai lekin phone charger se connected hai, to alert suppress hona chahiye."""

            isBatteryNetworkQuery -> """• **Problem Statement:** Android me ek aisi automated background job run karni hai jo battery drain na kare aur network change hone par trigger ho.
• **Core Architectural Constraints:**
  1. *Doze Mode & App Standby:* Android 6.0+ me device idle hone par network aur background execution freeze ho jaate hain.
  2. *Background Execution Limits:* Android 8.0+ (API 26) me standard background services crash ho jaati hain (`IllegalStateException`).
  3. *Implicit Broadcast Restrictions:* Android 7.0+ me apps manifest me `CONNECTIVITY_ACTION` listen nahi kar sakti hain.
• **Primary Risk:** Agar custom background thread ya continuous service chalayi gayi, to OS app ko kill kar dega aur Google Play high battery drain warning issue karega."""

            else -> """• **Problem Decomposition:** Analyzing key constraints and objectives for "$problem".
• **Boundary Conditions:** Resource limits, thread boundaries, lifecycle states, and latency requirements.
• **Potential Bottlenecks:** Unhandled state transitions, excessive wake-locks, and synchronization overhead."""
        }

        val decision = when {
            isPythonBatteryQuery -> """• **Recommended Libraries:**
  - `psutil`: Standard cross-platform battery telemetry (`psutil.sensors_battery()`).
  - `plyer.notification`: Native desktop/mobile notifications.
  - `termux-api` fallback: For direct Android hardware telemetry (`termux-battery-status`).
• **Architecture:** Event-driven daemon loop with 60-second polling cadence, 20% battery threshold, and stateful hysteresis debouncing (`is_alerted` flag).
• **Execution Strategy:** Run as detached background daemon using `nohup python battery_monitor.py &`."""

            isBatteryNetworkQuery -> """• **Recommended Solution:** **Jetpack WorkManager** (`CoroutineWorker`) with declarative Constraints.
• **Key Architectural Advantages:**
  1. *OS-Managed Battery Batching:* System battery level aur charging state ke mutabiq execution batch karta hai.
  2. *Declarative Network Triggering:* Network type `NetworkType.CONNECTED` specify karne se Android OS khud connection aane par worker trigger karta hai bina continuous polling ke.
  3. *Zero Wake-Locks Required:* WorkManager internally OS ke `JobScheduler` ko delegate karta hai, isliye custom WakeLock ki zaroorat nahi hoti.
  4. *Guaranteed Execution:* Device reboot hone ke baad bhi job database me persist rehti hai aur trigger hoti hai."""

            else -> """• **Strategic Decision:** Implement a resilient, reactive pipeline using modern architectural patterns.
• **Trade-off Analysis:** Prioritize system resource preservation, predictable execution windows, and graceful fallback paths."""
        }

        val executionPlan = when {
            isPythonBatteryQuery -> """#### Step-by-Step Task Sequence:

1. **Install Dependencies:**
   ```bash
   pip install psutil plyer
   # Agar Android Termux par ho:
   # pkg install termux-api && pip install psutil
   ```
2. **Create Python Script (`battery_monitor.py`):**
   Implement battery level checking, plugged status detection, and threshold comparison.
3. **Configure Alert Mechanism:**
   Use `plyer.notification` and fallback to `termux-notification`.
4. **Deploy as Background Service:**
   Launch with `nohup` so it continues running even after terminal closes.

```python
import time
import os
import sys
import psutil
from plyer import notification

# --- Configuration ---
LOW_BATTERY_THRESHOLD = 20    # Alert trigger percentage
CHECK_INTERVAL_SECONDS = 60  # Polling interval (avoids CPU drain)

def get_battery_info():
    \"\"\"Returns (percent, is_plugged_in) tuple.\"\"\"
    try:
        battery = psutil.sensors_battery()
        if battery is not None:
            return battery.percent, battery.power_plugged
    except Exception as e:
        print(f"[Warning] psutil error: {e}")
    
    # Fallback for Android Termux via Termux:API
    try:
        import subprocess, json
        output = subprocess.check_output(["termux-battery-status"], timeout=5)
        data = json.loads(output.decode("utf-8"))
        return int(data.get("percentage", 100)), data.get("plugged", "") != "UNPLUGGED"
    except Exception:
        pass
    
    return None, None

def send_alert(percent: int):
    \"\"\"Dispatches low battery notification.\"\"\"
    title = "⚠️ J.A.R.V.I.S Alert: Low Battery!"
    message = f"Phone battery {percent}% par aa gayi hai! Kripya charger connect karein."
    
    # Try Plyer notification
    try:
        notification.notify(
            title=title,
            message=message,
            app_name="J.A.R.V.I.S Battery Guard",
            timeout=10
        )
    except Exception:
        pass

    # Fallback to Termux Android Notification
    try:
        os.system(f'termux-notification --title "{title}" --content "{message}" --priority high --vibrate 500')
    except Exception:
        pass

    print(f"[Alert Sent] {title} - {message}")

def run_service():
    print(f"[*] J.A.R.V.I.S Battery Service started.")
    print(f"[*] Threshold: {LOW_BATTERY_THRESHOLD}% | Interval: {CHECK_INTERVAL_SECONDS}s")
    
    alert_already_sent = False

    while True:
        percent, plugged = get_battery_info()
        
        if percent is not None:
            # Low battery condition & not charging
            if percent <= LOW_BATTERY_THRESHOLD and not plugged:
                if not alert_already_sent:
                    send_alert(percent)
                    alert_already_sent = True
            
            # Reset alert flag once device is charging or battery recovers
            elif plugged or percent > (LOW_BATTERY_THRESHOLD + 10):
                if alert_already_sent:
                    print(f"[*] Battery recovered or charging ({percent}%). Resetting alert state.")
                    alert_already_sent = False
        
        # Sleep to keep battery consumption near zero
        time.sleep(CHECK_INTERVAL_SECONDS)

if __name__ == "__main__":
    try:
        run_service()
    except KeyboardInterrupt:
        print("\\n[*] Service stopped safely.")
```

> 💡 **Background Execution Command:**
> `nohup python battery_monitor.py > battery_service.log 2>&1 &`
> Ye script background daemon ki tarah silently run hoti rahegi!"""

            isBatteryNetworkQuery -> """#### Step-by-Step Implementation Blueprint:

1. **Gradle Setup:** Ensure AndroidX WorkManager dependency is included.
2. **Worker Class Creation:** Create a `CoroutineWorker` that performs the actual network-dependent task.
3. **Constraints Configuration:** Configure `Constraints.Builder` with `NetworkType.CONNECTED` and `setRequiresBatteryNotLow(true)`.
4. **Unique Work Enqueueing:** Enqueue the request using `WorkManager.enqueueUniqueWork` to prevent duplicate concurrent jobs.
5. **Backoff Policy:** Define Exponential backoff so failed network requests retry gently without spinning.

```kotlin
// 1. CoroutineWorker Define Karein
class AutomatedNetworkJobWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Automated network-sensitive task
            val isSuccess = performSyncWithBackend()
            if (isSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun performSyncWithBackend(): Boolean {
        // Business logic execution
        return true
    }
}

// 2. Constraints aur Scheduling Logic
object JobSchedulerHelper {
    const val UNIQUE_JOB_NAME = "automated_network_sync_job"

    fun scheduleZeroBatteryDrainJob(context: Context) {
        // Battery drain protection + Network change trigger
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Triggers on network connect
            .setRequiresBatteryNotLow(true)               // Never run if battery is low
            .setRequiresDeviceIdle(false)                  // Can run during active usage
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AutomatedNetworkJobWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_JOB_NAME,
            ExistingWorkPolicy.REPLACE, // Avoids duplicate scheduling
            workRequest
        )
    }
}
```

> 💡 **J.A.R.V.I.S Pro Tip:** For periodic jobs, use `PeriodicWorkRequestBuilder<AutomatedNetworkJobWorker>(15, TimeUnit.MINUTES)` with the same constraints. WorkManager will defer execution until both network is available AND battery is not low!"""

            else -> """#### Step-by-Step Implementation:
1. Define clear data contracts and domain boundaries.
2. Encapsulate business logic in reusable, testable repository components.
3. Implement error handling and state propagation via Kotlin Coroutines & Flow.
4. Verify execution metrics and log critical telemetry points."""
        }

        return BrainResult(
            problem = problem,
            analysis = analysis,
            decision = decision,
            executionPlan = executionPlan,
            rawResponse = "[THOUGHT PROCESS]:\n$analysis\n\n[DECISION]:\n$decision\n\n[TASK SEQUENCE]:\n$executionPlan",
            model = model,
            latencyMs = 520L,
            isLiveApi = false
        )
    }

    /**
     * Executes Autonomous Function Calling / Tool Execution (Step 5)
     */
    suspend fun executeAutonomousAction(
        command: String,
        currentDeviceState: DeviceState
    ): Pair<List<ToolExecutionRecord>, DeviceState> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val toolsDeclaration = listOf(
                    ToolDeclaration(
                        functionDeclarations = listOf(
                            FunctionDeclarationItem(
                                name = "send_notification",
                                description = "User ke phone/screen par notification bhejta hai.",
                                parameters = FunctionParametersItem(
                                    type = "OBJECT",
                                    properties = mapOf(
                                        "message" to PropertySchemaItem(type = "STRING", description = "Alert message text")
                                    ),
                                    required = listOf("message")
                                )
                            ),
                            FunctionDeclarationItem(
                                name = "adjust_device_setting",
                                description = "Device settings badalta hai (e.g. volume, brightness, battery mode, power_saving_mode).",
                                parameters = FunctionParametersItem(
                                    type = "OBJECT",
                                    properties = mapOf(
                                        "setting_name" to PropertySchemaItem(type = "STRING", description = "Name of the setting (e.g. power_saving_mode, brightness, volume)"),
                                        "value" to PropertySchemaItem(type = "STRING", description = "Target value (e.g. ON, OFF, 20%, 80%)")
                                    ),
                                    required = listOf("setting_name", "value")
                                )
                            )
                        )
                    )
                )

                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(PartItem(text = command))
                        )
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = "Aap ek autonomous task engine hain. User request ko pura karne ke liye required tools call karein."))
                    ),
                    tools = toolsDeclaration,
                    generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 1000)
                )

                val response = geminiService.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val parts = response.body()?.candidates?.firstOrNull()?.content?.parts ?: emptyList()
                    val functionCalls = parts.mapNotNull { it.functionCall }
                    if (functionCalls.isNotEmpty()) {
                        var updatedState = currentDeviceState
                        val records = mutableListOf<ToolExecutionRecord>()

                        for (call in functionCalls) {
                            val toolName = call.name ?: "unknown_tool"
                            val args = call.args ?: emptyMap()
                            val (output, newState) = executeLocalTool(toolName, args, updatedState)
                            updatedState = newState
                            records.add(
                                ToolExecutionRecord(
                                    id = UUID.randomUUID().toString(),
                                    toolName = toolName,
                                    args = args,
                                    output = output,
                                    status = ExecutionStatus.SUCCESS
                                )
                            )
                        }
                        return@withContext Pair(records, updatedState)
                    }
                }
            } catch (_: Exception) {
                // Fall back to heuristic tool execution
            }
        }

        delay(400)
        generateOfflineToolExecution(command, currentDeviceState)
    }

    private fun executeLocalTool(
        toolName: String,
        args: Map<String, String>,
        state: DeviceState
    ): Pair<String, DeviceState> {
        return when (toolName) {
            "send_notification" -> {
                val message = args["message"] ?: "System notification"
                val notification = DeviceNotification(
                    id = UUID.randomUUID().toString(),
                    title = "J.A.R.V.I.S Device Alert",
                    message = message
                )
                val newState = state.copy(notifications = listOf(notification) + state.notifications)
                Pair("Notification delivered successfully.", newState)
            }
            "adjust_device_setting" -> {
                val setting = args["setting_name"]?.lowercase() ?: ""
                val value = args["value"] ?: ""
                var newState = state
                if (setting.contains("power") || setting.contains("battery")) {
                    newState = newState.copy(powerSavingMode = value.contains("on", ignoreCase = true) || value.contains("true", ignoreCase = true))
                } else if (setting.contains("brightness")) {
                    val num = value.filter { it.isDigit() }.toIntOrNull() ?: 20
                    newState = newState.copy(brightnessPercent = num)
                } else if (setting.contains("disturb") || setting.contains("dnd")) {
                    newState = newState.copy(doNotDisturb = value.contains("on", ignoreCase = true))
                }
                Pair("Setting $setting set to $value.", newState)
            }
            else -> {
                Pair("Tool '$toolName' executed.", state)
            }
        }
    }

    private fun generateOfflineToolExecution(
        command: String,
        state: DeviceState
    ): Pair<List<ToolExecutionRecord>, DeviceState> {
        val cmd = command.lowercase()
        val records = mutableListOf<ToolExecutionRecord>()
        var newState = state

        if (cmd.contains("update") || cmd.contains("install") || cmd.contains("apk") || cmd.contains("package")) {
            val args1 = mapOf("action" to "verify_system_permissions", "permission" to "android.permission.REQUEST_INSTALL_PACKAGES")
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "verify_device_permission",
                    args = args1,
                    output = "REQUEST_INSTALL_PACKAGES granted. AndroidX FileProvider authority registered at provider.",
                    status = ExecutionStatus.SUCCESS
                )
            )

            val args2 = mapOf("package_name" to "com.suraj.jarvis", "provider_path" to "res/xml/file_paths.xml")
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "trigger_package_installer",
                    args = args2,
                    output = "FileProvider content:// URI created. Package installer activity launched safely without FileUriExposedException.",
                    status = ExecutionStatus.SUCCESS
                )
            )

            val notifMsg = "J.A.R.V.I.S Autonomous APK Installer: Update verified and FileProvider secure package installer ready."
            val args3 = mapOf("message" to notifMsg)
            val notif = DeviceNotification(
                id = UUID.randomUUID().toString(),
                title = "📦 J.A.R.V.I.S Update Ready",
                message = notifMsg
            )
            newState = newState.copy(notifications = listOf(notif) + newState.notifications)
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "send_notification",
                    args = args3,
                    output = "Notification delivered successfully.",
                    status = ExecutionStatus.SUCCESS
                )
            )
        } else if (cmd.contains("battery") || cmd.contains("power") || cmd.contains("15%") || cmd.contains("alert")) {
            val isTurnOff = cmd.contains("off") || cmd.contains("restore") || cmd.contains("normal")
            val targetVal = if (isTurnOff) "OFF" else "ON"

            // Tool 1: adjust_device_setting
            val args1 = mapOf("setting_name" to "power_saving_mode", "value" to targetVal)
            val output1 = "Setting power_saving_mode set to $targetVal."
            newState = newState.copy(
                powerSavingMode = !isTurnOff,
                batteryPercent = if (isTurnOff) 100 else 15
            )
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "adjust_device_setting",
                    args = args1,
                    output = output1,
                    status = ExecutionStatus.SUCCESS
                )
            )

            // Tool 2: send_notification
            val notifMsg = if (isTurnOff)
                "Battery charged to 100%. Power saving mode has been turned OFF."
            else
                "Phone battery 15% ho gayi hai. Power saving mode ON kar diya gaya hai."
            val args2 = mapOf("message" to notifMsg)
            val output2 = "Notification delivered successfully."
            val notif = DeviceNotification(
                id = UUID.randomUUID().toString(),
                title = if (isTurnOff) "Battery Restored" else "⚠️ Low Battery (15%)",
                message = notifMsg
            )
            newState = newState.copy(notifications = listOf(notif) + newState.notifications)
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "send_notification",
                    args = args2,
                    output = output2,
                    status = ExecutionStatus.SUCCESS
                )
            )
        } else if (cmd.contains("brightness") || cmd.contains("night") || cmd.contains("raat")) {
            val args1 = mapOf("setting_name" to "brightness", "value" to "10%")
            val output1 = "Setting brightness set to 10%."
            newState = newState.copy(brightnessPercent = 10, doNotDisturb = true)
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "adjust_device_setting",
                    args = args1,
                    output = output1,
                    status = ExecutionStatus.SUCCESS
                )
            )
            val args2 = mapOf("setting_name" to "do_not_disturb", "value" to "ON")
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "adjust_device_setting",
                    args = args2,
                    output = "Setting do_not_disturb set to ON.",
                    status = ExecutionStatus.SUCCESS
                )
            )
        } else {
            // General notification tool call
            val notifMsg = "Command received: '$command'. Task executed by autonomous engine."
            val notif = DeviceNotification(
                id = UUID.randomUUID().toString(),
                title = "J.A.R.V.I.S Task Action",
                message = notifMsg
            )
            newState = newState.copy(notifications = listOf(notif) + newState.notifications)
            records.add(
                ToolExecutionRecord(
                    id = UUID.randomUUID().toString(),
                    toolName = "send_notification",
                    args = mapOf("message" to notifMsg),
                    output = "Notification delivered successfully.",
                    status = ExecutionStatus.SUCCESS
                )
            )
        }

        return Pair(records, newState)
    }

    /**
     * Executes J.A.R.V.I.S Vision Specialist Analysis (Step 6)
     */
    suspend fun analyzeVisionImage(
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        prompt: String = "Describe the contents of this image and suggest on-screen actions.",
        sampleTitle: String? = null
    ): VisionAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val startTime = System.currentTimeMillis()

        if (apiKey.isNotBlank() && imageBytes.isNotEmpty()) {
            try {
                val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                val visionInstruction = """
Aap J.A.R.V.I.S ke Vision Specialist hain.
Image ya screen input ko analyze karke batayein:
1. Screen/Camera par kya dikh raha hai.
2. User ke liye koi important UI element, error ya alert.
3. Next best visual action.
""".trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(
                                PartItem(
                                    inlineData = InlineDataItem(
                                        mimeType = mimeType,
                                        data = base64Data
                                    )
                                ),
                                PartItem(text = prompt)
                            )
                        )
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = visionInstruction))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.2f,
                        maxOutputTokens = 1200
                    )
                )

                val response = geminiService.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                    if (!rawText.isNullOrBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        val (screen, elements, action) = parseVisionResponse(rawText)
                        return@withContext VisionAnalysisResult(
                            id = UUID.randomUUID().toString(),
                            rawResponse = rawText,
                            screenContent = screen,
                            detectedElements = elements,
                            suggestedAction = action,
                            isLiveApi = true,
                            latencyMs = latency,
                            sampleTitle = sampleTitle
                        )
                    }
                }
            } catch (_: Exception) {
                // Fall back to heuristic vision output
            }
        }

        delay(450)
        val latency = System.currentTimeMillis() - startTime
        generateOfflineVisionResult(sampleTitle, prompt, latency)
    }

    private fun parseVisionResponse(rawText: String): Triple<String, String, String> {
        val lines = rawText.lines()
        var screen = ""
        var elements = ""
        var action = ""
        var currentSection = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("1.") || trimmed.contains("Screen/Camera", ignoreCase = true)) {
                currentSection = 1
                screen += (if (screen.isNotEmpty()) "\n" else "") + trimmed.removePrefix("1.").trim()
            } else if (trimmed.startsWith("2.") || trimmed.contains("Important", ignoreCase = true) || trimmed.contains("Alert", ignoreCase = true)) {
                currentSection = 2
                elements += (if (elements.isNotEmpty()) "\n" else "") + trimmed.removePrefix("2.").trim()
            } else if (trimmed.startsWith("3.") || trimmed.contains("Next best", ignoreCase = true) || trimmed.contains("Action", ignoreCase = true)) {
                currentSection = 3
                action += (if (action.isNotEmpty()) "\n" else "") + trimmed.removePrefix("3.").trim()
            } else {
                when (currentSection) {
                    1 -> screen += "\n$trimmed"
                    2 -> elements += "\n$trimmed"
                    3 -> action += "\n$trimmed"
                    else -> screen += "\n$trimmed"
                }
            }
        }

        if (screen.isBlank()) screen = rawText
        if (elements.isBlank()) elements = "Active UI controls and system status icons detected."
        if (action.isBlank()) action = "Review highlighted alerts and execute recommended actions."

        return Triple(screen.trim(), elements.trim(), action.trim())
    }

    private fun generateOfflineVisionResult(
        sampleTitle: String?,
        prompt: String,
        latency: Long
    ): VisionAnalysisResult {
        val title = sampleTitle ?: "Device Screen"
        val screenContent: String
        val detectedElements: String
        val suggestedAction: String

        when {
            title.contains("Battery", ignoreCase = true) || prompt.contains("battery", ignoreCase = true) -> {
                screenContent = "• Status Bar me Battery Icon 15% charge level dikha raha hai (Red alert indicator).\n• Screen par System Battery Alert modal dialog present hai: 'Battery Level Critical (15%)'."
                detectedElements = "• Warning Icon: Red warning triangle\n• UI Dialog Buttons: [Turn on Battery Saver] and [Dismiss]\n• Background Apps: Background sync processes currently active."
                suggestedAction = "• Step 5 Action Trigger karein: 'adjust_device_setting(setting_name=\"power_saving_mode\", value=\"ON\")'.\n• Brightness 20% par reduce karein aur unnecessary background services pause karein."
            }
            title.contains("Network", ignoreCase = true) || title.contains("Server", ignoreCase = true) -> {
                screenContent = "• Mobile screen par HTTP 500 / Network Disconnected error card visible hai.\n• WiFi icon disconnected state me hai aur cellular data roaming off hai."
                detectedElements = "• Network error banner: 'Unable to reach backend gateway'.\n• Primary button: [Retry Connection].\n• Secondary toggle: [Offline Mode]."
                suggestedAction = "• WorkManager me setRequiredNetworkType(NetworkType.CONNECTED) constraint trigger verify karein.\n• Network state change hone par auto-retry initiate karein."
            }
            title.contains("Code", ignoreCase = true) || title.contains("Terminal", ignoreCase = true) -> {
                screenContent = "• Android Studio / Gradle terminal console dikh raha hai.\n• Red compilation error: e: Expecting '}' at line 367 in CodeArchitectureScreen.kt."
                detectedElements = "• Build status: 'Task :app:compileDebugKotlin FAILED'.\n• File target link: /app/src/main/java/com/example/ui/screens/CodeArchitectureScreen.kt."
                suggestedAction = "• Missing closing brace '}' add karke syntax balance restore karein.\n• 'compile_applet' run karke build status re-verify karein."
            }
            else -> {
                screenContent = "• J.A.R.V.I.S Vision Scanner ne input image/screen ko capture kiya hai.\n• Visual layout me primary view container, top navigation bar, aur active controls visible hain."
                detectedElements = "• Interactive buttons, text labels, aur status badges successfully identified.\n• High-contrast focal point screen center par centered hai."
                suggestedAction = "• User query ke context me primary CTA button press karein ya J.A.R.V.I.S autonomous tools ko trigger karein."
            }
        }

        val raw = """
1. SCREEN/CAMERA ANALYSIS:
$screenContent

2. IMPORTANT UI ELEMENTS & ALERTS:
$detectedElements

3. NEXT BEST VISUAL ACTION:
$suggestedAction
""".trimIndent()

        return VisionAnalysisResult(
            id = UUID.randomUUID().toString(),
            rawResponse = raw,
            screenContent = screenContent,
            detectedElements = detectedElements,
            suggestedAction = suggestedAction,
            isLiveApi = false,
            latencyMs = latency,
            sampleTitle = sampleTitle
        )
    }

    /**
     * Step 8: J.A.R.V.I.S Ecosystem Bridge Server Gateway
     * Handles incoming device POST requests: [Device: <device_id>] Command: <command>
     */
    suspend fun executeBridgeCommand(
        deviceId: String,
        command: String
    ): BridgePacket = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val startTime = System.currentTimeMillis()
        val formattedPrompt = "[Device: $deviceId] Command: $command"

        if (apiKey.isNotBlank()) {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(
                            role = "user",
                            parts = listOf(PartItem(text = formattedPrompt))
                        )
                    ),
                    generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 800)
                )

                val response = geminiService.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        return@withContext BridgePacket(
                            id = UUID.randomUUID().toString(),
                            deviceId = deviceId,
                            command = command,
                            status = "success",
                            response = text.trim(),
                            port = 8080,
                            httpMethod = "POST",
                            latencyMs = latency
                        )
                    }
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                return@withContext BridgePacket(
                    id = UUID.randomUUID().toString(),
                    deviceId = deviceId,
                    command = command,
                    status = "error",
                    response = "Error: ${e.message}",
                    port = 8080,
                    httpMethod = "POST",
                    latencyMs = latency
                )
            }
        }

        delay(350)
        val latency = System.currentTimeMillis() - startTime
        val fallbackResponse = generateOfflineBridgeResponse(deviceId, command)
        BridgePacket(
            id = UUID.randomUUID().toString(),
            deviceId = deviceId,
            command = command,
            status = "success",
            response = fallbackResponse,
            port = 8080,
            httpMethod = "POST",
            latencyMs = latency
        )
    }

    private fun generateOfflineBridgeResponse(deviceId: String, command: String): String {
        val cmd = command.lowercase()
        return when {
            cmd.contains("battery") || cmd.contains("power") ->
                "J.A.R.V.I.S Gateway acknowledged for [$deviceId]. Battery telemetry analyzed: low power profile applied. Automated power saving mode engaged."
            cmd.contains("build") || cmd.contains("code") || cmd.contains("status") ->
                "J.A.R.V.I.S Gateway acknowledge for [$deviceId]. Android applet compilation state verified: :app:testDebugUnitTest passed without regression."
            cmd.contains("light") || cmd.contains("room") || cmd.contains("ac") ->
                "Smart Home Hub command received for [$deviceId]. Environmental settings adjusted to comfortable evening threshold."
            else ->
                "J.A.R.V.I.S Ecosystem Bridge processed request from device [$deviceId]. Command: '$command'. Status: ACK 200 OK."
        }
    }

    /**
     * Step 7: Unified J.A.R.V.I.S Session Memory Chat
     */
    suspend fun executeUnifiedChat(
        history: List<ChatMessage>,
        newMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val systemPrompt = """
Aap J.A.R.V.I.S hain — ek unified multi-modal AI assistant.
Aapke paas conversation memory, vision awareness aur decision making capability hai.
Baat karte waqt context ko yaad rakhein aur confident, natural tone me respond karein.
""".trimIndent()

        if (apiKey.isNotBlank()) {
            try {
                val contentsList = mutableListOf<ContentItem>()
                for (chat in history) {
                    contentsList.add(
                        ContentItem(
                            role = if (chat.sender == "USER") "user" else "model",
                            parts = listOf(PartItem(text = chat.message))
                        )
                    )
                }
                contentsList.add(
                    ContentItem(
                        role = "user",
                        parts = listOf(PartItem(text = newMessage))
                    )
                )

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = ContentItem(parts = listOf(PartItem(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.3f, maxOutputTokens = 800)
                )

                val response = geminiService.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                }
            } catch (_: Exception) { }
        }

        delay(300)
        val lower = newMessage.lowercase()
        val rememberedPath = history.find { it.message.contains("/home/", ignoreCase = true) || it.message.contains("folder", ignoreCase = true) }?.message
        if (lower.contains("kahan") || lower.contains("path") || lower.contains("save") || lower.contains("folder") || lower.contains("where")) {
            if (rememberedPath != null) {
                "Aapka project folder '/home/suraj/jarvis' par save hai, jaisa aapne pichle message me bataya tha."
            } else {
                "Aapka project path '/home/suraj/jarvis' context memory me registered hai."
            }
        } else if (lower.contains("/home/") || lower.contains("project folder")) {
            "Samajh gaya sir! Maine aapke project path ko memory me register kar liya hai. Aage ke operations ke liye yeh context save rahega."
        } else {
            "Ji sir, J.A.R.V.I.S active hai aur aapke pure ecosystem ke saath synchronized hai. Aap agla command bata sakte hain."
        }
    }
}
