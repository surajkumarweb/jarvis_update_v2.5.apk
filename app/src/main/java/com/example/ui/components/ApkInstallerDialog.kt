package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.installer.ApkInstallerManager
import com.example.installer.ApkInstallerStatus
import com.example.ui.theme.AmberCreative
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApkInstallerDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val installerState by ApkInstallerManager.installerState.collectAsState()

    var apkUrl by remember { mutableStateOf("https://example.com/sample.apk") }
    var hasInstallPermission by remember {
        mutableStateOf(ApkInstallerManager.canRequestPackageInstalls(context))
    }

    val sampleUrls = remember {
        listOf(
            "https://example.com/sample.apk",
            "https://example.com/jarvis_update_v2.5.apk",
            "https://example.com/release_signed.apk"
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, CyanPrimaryLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "System Update",
                                tint = CyanPrimaryLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "J.A.R.V.I.S APK INSTALLER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "AndroidX FileProvider & Package Manager",
                                fontSize = 11.5.sp,
                                color = CyanPrimaryLight
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Permission Warning / Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasInstallPermission) Color(0xFF10B981).copy(alpha = 0.1f)
                        else Color(0xFFF59E0B).copy(alpha = 0.12f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (hasInstallPermission) Color(0xFF10B981).copy(alpha = 0.4f)
                            else Color(0xFFF59E0B).copy(alpha = 0.4f)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasInstallPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Permission status",
                                tint = if (hasInstallPermission) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (hasInstallPermission) "REQUEST_INSTALL_PACKAGES: Granted" else "Package Install Permission Needed",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasInstallPermission) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                                Text(
                                    text = if (hasInstallPermission) "Android 8.0+ unknown app sources authorized." else "Tap to allow J.A.R.V.I.S to install package updates.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (!hasInstallPermission) {
                            OutlinedButton(
                                onClick = {
                                    ApkInstallerManager.openUnknownAppSourcesSettings(context)
                                    hasInstallPermission = ApkInstallerManager.canRequestPackageInstalls(context)
                                },
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text("Grant", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // APK URL Input Field
                Text(
                    text = "APK Download Link (URL):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = apkUrl,
                    onValueChange = { apkUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apk_download_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = CyanPrimaryLight,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sample URLs
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleUrls.forEach { url ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (apkUrl == url) CyanPrimary.copy(alpha = 0.25f) else DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (apkUrl == url) CyanPrimaryLight else DarkCardBorder
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = { apkUrl = url },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = url.substringAfterLast("/"),
                                        fontSize = 11.sp,
                                        color = if (apkUrl == url) CyanPrimaryLight else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STATUS:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            if (installerState.status == ApkInstallerStatus.DOWNLOADING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = CyanPrimaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = installerState.statusMessage,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (installerState.status == ApkInstallerStatus.ERROR) Color(0xFFEF4444)
                            else if (installerState.status == ApkInstallerStatus.DOWNLOAD_COMPLETE) Color(0xFF10B981)
                            else TextPrimary
                        )

                        if (installerState.status == ApkInstallerStatus.DOWNLOADING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { installerState.downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanPrimaryLight,
                                trackColor = DarkCardBorder
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Button (DownloadManager + FileProvider)
                Button(
                    onClick = {
                        hasInstallPermission = ApkInstallerManager.canRequestPackageInstalls(context)
                        if (!hasInstallPermission) {
                            Toast.makeText(context, "Please allow Unknown App Sources first!", Toast.LENGTH_SHORT).show()
                            ApkInstallerManager.openUnknownAppSourcesSettings(context)
                        } else {
                            Toast.makeText(context, "डाउनलोड शुरू हुआ...", Toast.LENGTH_SHORT).show()
                            ApkInstallerManager.downloadApkViaDownloadManager(context, apkUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btnDownloadInstall"),
                    enabled = installerState.status != ApkInstallerStatus.DOWNLOADING,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download via DownloadManager",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (installerState.status == ApkInstallerStatus.DOWNLOADING) "डाउनलोड हो रहा है..."
                        else "Download & Install APK (DownloadManager)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // In-App Direct Stream Download Button
                OutlinedButton(
                    onClick = {
                        hasInstallPermission = ApkInstallerManager.canRequestPackageInstalls(context)
                        if (!hasInstallPermission) {
                            ApkInstallerManager.openUnknownAppSourcesSettings(context)
                        } else {
                            scope.launch {
                                ApkInstallerManager.downloadAndInstallApk(context, apkUrl)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btnInstallApk"),
                    enabled = installerState.status != ApkInstallerStatus.DOWNLOADING,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "In-App Stream Download (Fallback)",
                        fontSize = 12.5.sp
                    )
                }

                if (installerState.lastDownloadedFile != null && installerState.status != ApkInstallerStatus.DOWNLOADING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            installerState.lastDownloadedFile?.let {
                                ApkInstallerManager.launchPackageInstaller(context, it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "Re-install",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-launch System Package Installer", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Architecture Notes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "FileProvider Security",
                        tint = CyanPrimaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Android 7+ FileUriExposedException Fix via FileProvider",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimaryLight
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generates secure content://${context.packageName}.provider URIs with FLAG_GRANT_READ_URI_PERMISSION, preventing system installer sandbox crashes.",
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
