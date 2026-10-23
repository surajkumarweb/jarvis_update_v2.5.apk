package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CodeArchitectureScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val jarvisBrainSnippet = remember {
        """
import os
from google import genai
from google.genai import types

# Client initialize karein
client = genai.Client()

system_prompt = \"\"\"
Aap J.A.R.V.I.S ke Central Decision-Making Brain hain.
Aapka kaam input ko analyze karna, problem solve karna aur action steps decide karna hai.
Hamesha is structured format me response dein:
[THOUGHT PROCESS]: Problem ka breakdown.
[DECISION]: Kya action perform karna hai.
[TASK SEQUENCE]: Step-by-step execution list.
\"\"\"

user_input = "Mujhe ek Python background service banani hai jo phone ki battery check kare aur kam hone par alert bheje."

response = client.models.generate_content(
    model="gemini-2.5-pro",
    contents=user_input,
    config=types.GenerateContentConfig(
        system_instruction=system_prompt,
        temperature=0.2
    )
)

print("--- Step 2: Core Brain Output ---")
print(response.text)
""".trimIndent()
    }

    val routerSnippet = remember {
        """
import os
from google import genai
from google.genai import types

client = genai.Client()

def get_agent_response(role_instruction: str, user_input: str) -> str:
    \"\"\"Ek specific agent ko prompt bhejkar response leta hai.\"\"\"
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=user_input,
        config=types.GenerateContentConfig(
            system_instruction=role_instruction,
            temperature=0.3
        )
    )
    return response.text

# 1. Router Agent: User input ka purpose identify karta hai
router_instruction = (
    "Aap ek Router Agent hain. User query ko analyze karke sirf category batayein: "
    "[TECHNICAL, RESEARCH, CASUAL]"
)

# 2. Specialist Agents
coder_instruction = "Aap ek Senior Programmer hain. Code aur technical guidance dein."
researcher_instruction = "Aap ek Research Analyst hain. Facts aur detailed explanation dein."

user_query = "Android background service me battery optimization kaise handle karein?"

# Routing decision
category = get_agent_response(router_instruction, user_query).strip()
print(f"[System] Selected Agent Category: {category}")

# Directing to the right specialist
if "TECHNICAL" in category:
    final_output = get_agent_response(coder_instruction, user_query)
else:
    final_output = get_agent_response(researcher_instruction, user_query)

print("\n--- Agent Response ---")
print(final_output)
""".trimIndent()
    }

    val kotlinSnippet = remember {
        """
// Android WorkManager + Jetpack Compose Architecture
class JobSchedulerHelper {
    fun scheduleZeroBatteryDrainJob(context: Context) {
        // Battery drain protection + Network change trigger
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Triggers on network connect
            .setRequiresBatteryNotLow(true)               // Never run if battery is low
            .setRequiresDeviceIdle(false)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AutomatedNetworkJobWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "automated_network_job",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
""".trimIndent()
    }

    val sttSnippet = remember {
        """
import os
from google import genai
from google.genai import types

client = genai.Client()

# Apni audio file ka path yahan dein
audio_file_path = "user_command.mp3"

stt_instruction = \"\"\"
Aap ek high-speed Speech-to-Text transcriber hain.
Diye gaye audio ko bina kisi extra comment ke bilkul accurately text me convert karein.
Har bhasha (Hindi, English, Hinglish) ko sahi se transcribe karein.
\"\"\"

# Audio file upload karein
audio_upload = client.files.upload(file=audio_file_path)

response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=[
        audio_upload,
        "Transcribe this spoken audio accurately into text."
    ],
    config=types.GenerateContentConfig(
        system_instruction=stt_instruction,
        temperature=0.0
    )
)

print("--- Step 4: Speech-to-Text Result ---")
print("Transcribed Text:", response.text)
""".trimIndent()
    }

    val actionSnippet = remember {
        """
import os
from google import genai
from google.genai import types

client = genai.Client()

# System ke actions (Tools) define karein
def send_notification(message: str) -> str:
    \"\"\"User ke phone/screen par notification bhejta hai.\"\"\"
    print(f"\n[DEVICE ACTION]: Alert -> {message}")
    return "Notification delivered successfully."

def adjust_device_setting(setting_name: str, value: str) -> str:
    \"\"\"Device settings badalta hai (e.g. volume, brightness, battery mode).\"\"\"
    print(f"\n[DEVICE ACTION]: Setting {setting_name} set to {value}")
    return f"{setting_name} changed to {value}."

tools = [send_notification, adjust_device_setting]

user_command = "Phone ki battery 15% ho gayi hai, power saving mode on karo aur screen par alert dikhao."

# Model automatically tools aur steps select karega
response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=user_command,
    config=types.GenerateContentConfig(
        tools=tools,
        system_instruction="Aap ek autonomous task engine hain. User request ko pura karne ke liye required tools call karein."
    )
)

print("--- Step 5: Task Execution Decision ---")
for part in response.candidates[0].content.parts:
    if part.function_call:
        print(f"Autonomous Action Triggered: {part.function_call.name}({part.function_call.args})")
""".trimIndent()
    }

    val visionSnippet = remember {
        """
import os
from PIL import Image
from google import genai
from google.genai import types

client = genai.Client()

# Image load karein (screen capture ya camera photo)
# Niche 'screen_shot.jpg' ko apni image file se badal lein
try:
    img = Image.open("screen_shot.jpg")
except FileNotFoundError:
    # Test ke liye ek dummy blank image bana lete hain
    img = Image.new("RGB", (300, 300), color="blue")

vision_instruction = \"\"\"
Aap J.A.R.V.I.S ke Vision Specialist hain.
Image ya screen input ko analyze karke batayein:
1. Screen/Camera par kya dikh raha hai.
2. User ke liye koi important UI element, error ya alert.
3. Next best visual action.
\"\"\"

response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=[img, "Describe the contents of this image and suggest on-screen actions."],
    config=types.GenerateContentConfig(
        system_instruction=vision_instruction
    )
)

print("--- Step 6: Vision Analysis ---")
print(response.text)
""".trimIndent()
    }

    val step7Snippet = remember {
        """
import os
from google import genai
from google.genai import types

client = genai.Client()

# Unified Chat Session jisme session-level memory maintain rehti hai
chat = client.chats.create(
    model="gemini-2.5-flash",
    config=types.GenerateContentConfig(
        system_instruction=\"\"\"
        Aap J.A.R.V.I.S hain — ek unified multi-modal AI assistant.
        Aapke paas conversation memory, vision awareness aur decision making capability hai.
        Baat karte waqt context ko yaad rakhein aur confident, natural tone me respond karein.
        \"\"\"
    )
)

print("--- Step 7: Unified System Active ---")

# Step 7.1: User context provide karta hai
r1 = chat.send_message("Mera project folder /home/suraj/jarvis me hai.")
print(f"J.A.R.V.I.S: {r1.text}\n")

# Step 7.2: Unified memory se question ka answer deta hai
r2 = chat.send_message("Mera project kaun se path par save hai?")
print(f"J.A.R.V.I.S: {r2.text}")
""".trimIndent()
    }

    val step8Snippet = remember {
        """
import os
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from google import genai

client = genai.Client()

class JARVISBridgeServer(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length).decode('utf-8')
        
        try:
            req = json.loads(post_data)
            device_id = req.get("device_id", "Unknown-Device")
            command = req.get("command", "")
            
            # Central AI process
            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=f"[Device: {device_id}] Command: {command}"
            )
            
            reply = {"status": "success", "response": response.text}
        except Exception as e:
            reply = {"status": "error", "message": str(e)}

        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(reply).encode('utf-8'))

# Local machine / Server par listener run karna
print("--- Step 8: J.A.R.V.I.S Ecosystem Bridge Started on port 8080 ---")
# server = HTTPServer(('0.0.0.0', 8080), JARVISBridgeServer)
# server.serve_forever()
""".trimIndent()
    }

    val step9ManifestSnippet = remember {
        """
<!-- Step 9: J.A.R.V.I.S Autonomous APK Installer & System Updater -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.suraj.jarvis">

    <!-- इंटरनेट और नेटवर्क परमिशन -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- APK इंस्टॉल करने की परमिशन (Android 8.0+) -->
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

    <!-- स्टोरेज परमिशन (पुराने और नए वर्शन के लिए सुरक्षित) -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="J.A.R.V.I.S"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Android 7+ (FileUriExposedException) एरर को फिक्स करने के लिए Provider -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${'$'}{applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>

<!-- res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="external_files" path="." />
    <external-cache-path name="external_cache" path="." />
    <cache-path name="internal_cache" path="." />
    <files-path name="internal_files" path="." />
</paths>

<!-- res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0A0E17"
    android:orientation="vertical"
    android:padding="20dp"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="J.A.R.V.I.S APK INSTALLER"
        android:textColor="#00E5FF"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginTop="20dp"
        android:layout_marginBottom="16dp" />

    <!-- Permission Box -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#1E1905"
        android:padding="12dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginBottom="20dp">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Package Install Permission Needed"
            android:textColor="#FFB300"
            android:textSize="13sp" />

        <Button
            android:id="@+id/btnGrantPermission"
            android:layout_width="wrap_content"
            android:layout_height="36dp"
            android:text="Grant"
            android:backgroundTint="#FFB300"
            android:textColor="#000000"
            android:textSize="12sp" />
    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="APK Download Link (URL):"
        android:textColor="#8A99AD"
        android:textSize="14sp"
        android:layout_marginBottom="6dp" />

    <EditText
        android:id="@+id/etUrl"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="#131B2A"
        android:textColor="#FFFFFF"
        android:padding="10dp"
        android:textSize="14sp"
        android:layout_marginBottom="20dp" />

    <!-- Status Box -->
    <TextView
        android:id="@+id/tvStatus"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#131B2A"
        android:padding="12dp"
        android:textColor="#00E5FF"
        android:textSize="14sp"
        android:text="STATUS: Ready to download and install updates."
        android:layout_marginBottom="20dp" />

    <Button
        android:id="@+id/btnDownloadInstall"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        android:backgroundTint="#00E5FF"
        android:text="⬇ Download &amp; Install APK"
        android:textColor="#000000"
        android:textStyle="bold"
        android:textSize="15sp" />

</LinearLayout>

<!-- Java MainActivity Implementation (DownloadManager + BroadcastReceiver) -->
// package com.suraj.jarvis;
//
// public class MainActivity extends AppCompatActivity {
//     private TextView tvStatus;
//     private EditText etUrl;
//     private Button btnDownloadInstall, btnGrantPermission;
//     private long downloadId = -1;
//     
//     private void downloadApkToDownloadsFolder(String url) {
//         DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//         request.setTitle("J.A.R.V.I.S-Assistant.apk");
//         request.setDescription("J.A.R.V.I.S Assistant अपडेट डाउनलोड हो रहा है...");
//         request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "J.A.R.V.I.S-Assistant.apk");
//         request.setMimeType("application/vnd.android.package-archive");
//         
//         DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//         downloadId = manager.enqueue(request);
//     }
//     
//     private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
//         @Override
//         public void onReceive(Context context, Intent intent) {
//             long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//             if (downloadId == id) triggerApkInstall();
//         }
//     };
//     
//     private void triggerApkInstall() {
//         File publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//         File apkFile = new File(publicDownloadDir, "J.A.R.V.I.S-Assistant.apk");
//         Intent installIntent = new Intent(Intent.ACTION_VIEW);
//         installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//         Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", apkFile);
//         installIntent.setDataAndType(contentUri, "application/vnd.android.package-archive");
//         installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//         startActivity(installIntent);
//     }
// }

// --- Kotlin Coroutines Implementation (ApkInstallerManager.kt) ---
object ApkInstallerManager {
    suspend fun downloadAndInstallApk(context: Context, fileUrl: String) = withContext(Dispatchers.IO) {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val apkFile = File(downloadDir, "jarvis_update.apk")
        // Coroutine download streaming...
        launchPackageInstaller(context, apkFile)
    }

    fun launchPackageInstaller(context: Context, file: File) {
        val apkUri = FileProvider.getUriForFile(context, "${'$'}{context.packageName}.provider", file)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }
}
""".trimIndent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Architecture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI ARCHITECTURE & SDK CODE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "J.A.R.V.I.S Ecosystem Bridge, Unified Chat & Multi-Agent Architecture",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Architectural Notes Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Central Brain Advantage",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End-to-End J.A.R.V.I.S Ecosystem Architecture",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Step 9: Autonomous APK Installer & System Updater: Declares REQUEST_INSTALL_PACKAGES and AndroidX FileProvider to securely install runtime updates without FileUriExposedException.\n\n" +
                            "• Step 8: Ecosystem Bridge Server (Port 8080): HTTP BaseHTTPRequestHandler receiving JSON payloads from multi-device ecosystem nodes.\n\n" +
                            "• Step 7: Unified Multi-modal Chat: Full session-level conversation memory recalling context (e.g., project folder paths) across turns.\n\n" +
                            "• Step 6: Vision Specialist (gemini-2.5-flash): Inspects device screenshots & camera inputs, extracting key alerts and UI recommendations.\n\n" +
                            "• Step 5: Autonomous Tools (gemini-2.5-flash): Device tool calls (send_notification, adjust_device_setting) executing hardware actions.\n\n" +
                            "• Step 4: Speech-to-Text (gemini-2.5-flash @ temp 0.0): Ingests user audio commands in Hindi, English, or Hinglish.\n\n" +
                            "• Step 2: Central Brain (gemini-2.5-pro @ temp 0.2): Deconstructs problems into [THOUGHT PROCESS] -> [DECISION] -> [TASK SEQUENCE].\n\n" +
                            "• Step 1: Router Agent (gemini-2.5-flash): Dispatches prompts to domain specialists.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Step 9: APK Installer", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.SystemUpdate, contentDescription = "Installer", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Step 8: Bridge (:8080)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Router, contentDescription = "Bridge", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Step 7: Unified Memory", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Step 6: Vision Specialist", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Visibility, contentDescription = "Vision", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Step 5: Autonomous Tools", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Bolt, contentDescription = "Tools", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = { Text("Step 4: Audio STT", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Terminal, contentDescription = "STT", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                text = { Text("Step 2: J.A.R.V.I.S Brain", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Psychology, contentDescription = "Brain", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 7,
                onClick = { selectedTab = 7 },
                text = { Text("Step 1: Router Agent", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Code, contentDescription = "Router", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 8,
                onClick = { selectedTab = 8 },
                text = { Text("Android WorkManager", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.IntegrationInstructions, contentDescription = "Kotlin", modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> CodeBlockView(code = step9ManifestSnippet, language = "XML & Kotlin (FileProvider + Manifest)")
            1 -> CodeBlockView(code = step8Snippet, language = "Python (JARVISBridgeServer HTTP)")
            2 -> CodeBlockView(code = step7Snippet, language = "Python (gemini-2.5-flash Chats)")
            3 -> CodeBlockView(code = visionSnippet, language = "Python (gemini-2.5-flash Vision)")
            4 -> CodeBlockView(code = actionSnippet, language = "Python (gemini-2.5-flash Tools)")
            5 -> CodeBlockView(code = sttSnippet, language = "Python (gemini-2.5-flash STT)")
            6 -> CodeBlockView(code = jarvisBrainSnippet, language = "Python (gemini-2.5-pro)")
            7 -> CodeBlockView(code = routerSnippet, language = "Python (gemini-2.5-flash)")
            8 -> CodeBlockView(code = kotlinSnippet, language = "Kotlin (WorkManager)")
        }
    }
}
