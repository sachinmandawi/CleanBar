package com.cleanbar.hider.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cleanbar.hider.R
import com.cleanbar.hider.service.KeepAliveService
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.shizuku.ShizukuManager
import com.cleanbar.hider.ui.theme.*
import com.cleanbar.hider.util.AppLogger
import com.cleanbar.hider.util.PreferencesHelper
import com.cleanbar.hider.util.UpdateChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEVELOPER_EMAIL = "sachinmandawi@gmail.com"
private const val GITHUB_REPO_URL = "https://github.com/sachinmandawi/CleanBar"
private const val GITHUB_ISSUES_URL = "https://github.com/sachinmandawi/CleanBar/issues"
private const val UPI_ID = "darkcaptain@ybl"
private const val BINANCE_UID = "1138545342"

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onRequestNotificationPermission: () -> Unit = {},
    isShizukuActive: Boolean = false,
    hasShizukuPermission: Boolean = false,
    onRequestShizukuPermission: () -> Unit = {},
    onRefreshShizukuStatus: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.UpdateResult?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var isPersistentMode by remember {
        mutableStateOf(PreferencesHelper.isPersistentModeEnabled(context))
    }

    var isBatteryOptimized by remember {
        mutableStateOf(
            runCatching {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            }.getOrDefault(false)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                isBatteryOptimized = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
                onRefreshShizukuStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var logsReportText by remember { mutableStateOf("") }
    var isLoadingLogs by remember { mutableStateOf(false) }

    LaunchedEffect(showLogsDialog) {
        if (showLogsDialog) {
            isLoadingLogs = true
            logsReportText = AppLogger.getFormattedDiagnosticReport(context)
            isLoadingLogs = false
        }
    }

    // Bottom Toast State
    var bottomToastMessage by remember { mutableStateOf<String?>(null) }

    fun triggerBottomToast(message: String) {
        bottomToastMessage = message
        coroutineScope.launch {
            delay(2500)
            if (bottomToastMessage == message) {
                bottomToastMessage = null
            }
        }
    }

    // Handle Android native back gesture
    BackHandler(enabled = true) {
        if (showUpdateDialog) {
            showUpdateDialog = false
        } else if (showRestoreConfirmDialog) {
            showRestoreConfirmDialog = false
        } else if (showLogsDialog) {
            showLogsDialog = false
        } else {
            doVibrate(context)
            onBack()
        }
    }

    // Update Available Dialog
    if (showUpdateDialog && updateResult != null) {
        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2F3A))
                            .border(1.dp, Color(0xFF2B527E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = CleanBarBlueText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Update Available 🚀",
                        color = CleanBarTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CleanBar ${updateResult?.latestVersion} is ready to download.",
                        color = CleanBarTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showUpdateDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarDarkBg,
                                contentColor = CleanBarTextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanBarDarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showUpdateDialog = false
                                doVibrate(context)
                                openBrowser(context, updateResult?.downloadUrl ?: GITHUB_REPO_URL)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarBlueText,
                                contentColor = Color(0xFF191919)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Download", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Restore to Default Confirmation Dialog
    if (showRestoreConfirmDialog) {
        Dialog(onDismissRequest = { showRestoreConfirmDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1919))
                            .border(1.dp, Color(0xFF6B2D2D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = Color(0xFFFF7B7B),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Restore All to Default?",
                        color = CleanBarTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "This will restore all status bar icons, disable flags, and clear custom configurations. Recommended before uninstalling CleanBar.",
                        color = CleanBarTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showRestoreConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarDarkBg,
                                contentColor = CleanBarTextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanBarDarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showRestoreConfirmDialog = false
                                doVibrate(context)
                                coroutineScope.launch {
                                    try {
                                        ImmersiveController.resetToDefault(context)
                                        PreferencesHelper.setTargetHidden(context, false)
                                        PreferencesHelper.setPersistentModeEnabled(context, false)
                                        isPersistentMode = false
                                        KeepAliveService.stop(context)
                                        triggerBottomToast("✓ Status bar fully restored to default")
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        triggerBottomToast("Failed to restore default")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B2525),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Restore", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Diagnostic Logs Dialog
    if (showLogsDialog) {
        Dialog(onDismissRequest = { showLogsDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2330)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = CleanBarBlueText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Diagnostic Logs",
                                color = CleanBarTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showLogsDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = CleanBarTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isLoadingLogs) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CleanBarBlueText,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141414))
                                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = logsReportText.ifEmpty { "No diagnostic logs available yet." },
                                color = Color(0xFFD4D4D4),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clear Button
                        OutlinedButton(
                            onClick = {
                                doVibrate(context)
                                AppLogger.clearLogs()
                                coroutineScope.launch {
                                    logsReportText = AppLogger.getFormattedDiagnosticReport(context)
                                }
                                triggerBottomToast("✓ Event logs cleared")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33EF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        // Copy Button
                        Button(
                            onClick = {
                                doVibrate(context)
                                clipboardManager.setText(AnnotatedString(logsReportText))
                                triggerBottomToast("✓ Diagnostic logs copied to clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanBarBlueText,
                                contentColor = Color(0xFF121820)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy Logs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Share Button
                        OutlinedButton(
                            onClick = {
                                doVibrate(context)
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "CleanBar Diagnostic Report")
                                        putExtra(Intent.EXTRA_TEXT, logsReportText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Diagnostic Logs"))
                                } catch (e: Throwable) {
                                    triggerBottomToast("Could not open share menu")
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CleanBarTextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanBarDarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBarDarkBg)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // Top Navigation Bar
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                val isBackPressed by backInteraction.collectIsPressedAsState()
                val backScale by animateFloatAsState(
                    targetValue = if (isBackPressed) 0.88f else 1f,
                    animationSpec = tween(durationMillis = 150),
                    label = "backScale"
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(backScale)
                        .clip(CircleShape)
                        .background(CleanBarDarkSurface)
                        .border(1.dp, CleanBarDarkBorder, CircleShape)
                        .clickable(
                            interactionSource = backInteraction,
                            indication = null
                        ) {
                            doVibrate(context)
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CleanBarTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "About",
                    color = CleanBarTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Brand Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = "CleanBar Logo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "CleanBar",
                            color = CleanBarTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version ${UpdateChecker.CURRENT_VERSION} • Full Status Bar Hider",
                            color = CleanBarTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Settings Section
            // ==========================================
            MinimalSectionTitle(title = "SETTINGS")

            Spacer(modifier = Modifier.height(6.dp))

            // Shizuku Service & Permission Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        doVibrate(context)
                        if (!isShizukuActive) {
                            if (ShizukuManager.isShizukuInstalled(context)) {
                                triggerBottomToast("Please start Shizuku service. Opening Shizuku...")
                                ShizukuManager.openShizuku(context)
                            } else {
                                triggerBottomToast("Shizuku app not found. Opening Play Store...")
                                ShizukuManager.openShizukuPlayStore(context)
                            }
                        } else if (!hasShizukuPermission) {
                            onRequestShizukuPermission()
                            triggerBottomToast("Please allow Shizuku popup permission")
                        } else {
                            ShizukuManager.openShizuku(context)
                            triggerBottomToast("To revoke, toggle CleanBar off in Shizuku")
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasShizukuPermission) Color(0xFF15261E)
                                    else if (isShizukuActive) Color(0xFF1E2F3A)
                                    else Color(0xFF282014)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (hasShizukuPermission) CleanBarGreenText
                                else if (isShizukuActive) CleanBarBlueText
                                else Color(0xFFFFB84D),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Shizuku Permission",
                                color = CleanBarTextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasShizukuPermission) "Authorized • Status bar control active"
                                else if (isShizukuActive) "Service Running • Tap to allow popup"
                                else if (ShizukuManager.isShizukuInstalled(context)) "Service Stopped • Tap to start"
                                else "Not Installed • Tap for Play Store",
                                color = CleanBarTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = hasShizukuPermission,
                        onCheckedChange = { _ ->
                            doVibrate(context)
                            if (!isShizukuActive) {
                                if (ShizukuManager.isShizukuInstalled(context)) {
                                    triggerBottomToast("Please start Shizuku service. Opening Shizuku...")
                                    ShizukuManager.openShizuku(context)
                                } else {
                                    triggerBottomToast("Shizuku app not found. Opening Play Store...")
                                    ShizukuManager.openShizukuPlayStore(context)
                                }
                            } else if (!hasShizukuPermission) {
                                onRequestShizukuPermission()
                                triggerBottomToast("Please allow Shizuku popup permission")
                            } else {
                                ShizukuManager.openShizuku(context)
                                triggerBottomToast("To revoke, toggle CleanBar off in Shizuku")
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2B4C7E),
                            uncheckedThumbColor = Color(0xFF8E99AF),
                            uncheckedTrackColor = Color(0xFF1E2330),
                            uncheckedBorderColor = CleanBarDarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auto-Reapply on Unlock Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        doVibrate(context)
                        val newState = !isPersistentMode
                        isPersistentMode = newState
                        PreferencesHelper.setPersistentModeEnabled(context, newState)
                        if (newState) {
                            onRequestNotificationPermission()
                            if (PreferencesHelper.isTargetHidden(context)) {
                                KeepAliveService.start(context)
                            }
                            triggerBottomToast("Auto-Reapply Enabled")
                        } else {
                            KeepAliveService.stop(context)
                            triggerBottomToast("Auto-Reapply Disabled")
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPersistentMode) Color(0xFF17241F) else Color(0xFF161616)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isPersistentMode) CleanBarGreenText else CleanBarTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Auto-Reapply on Unlock",
                                color = CleanBarTextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keeps hidden across screen locks",
                                color = CleanBarTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isPersistentMode,
                        onCheckedChange = { newState ->
                            doVibrate(context)
                            isPersistentMode = newState
                            PreferencesHelper.setPersistentModeEnabled(context, newState)
                            if (newState) {
                                onRequestNotificationPermission()
                                if (PreferencesHelper.isTargetHidden(context)) {
                                    KeepAliveService.start(context)
                                }
                                triggerBottomToast("Auto-Reapply Enabled")
                            } else {
                                KeepAliveService.stop(context)
                                triggerBottomToast("Auto-Reapply Disabled")
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2B4C7E),
                            uncheckedThumbColor = Color(0xFF8E99AF),
                            uncheckedTrackColor = Color(0xFF1E2330),
                            uncheckedBorderColor = CleanBarDarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ignore Battery Optimization Card
            MinimalCard(
                icon = Icons.Default.Bolt,
                iconBg = if (isBatteryOptimized) Color(0xFF15261E) else Color(0xFF282014),
                iconTint = if (isBatteryOptimized) CleanBarGreenText else Color(0xFFFFB84D),
                title = "Ignore Battery Optimization",
                subtitle = "Prevents iQOO, Vivo & Xiaomi from killing app",
                trailingContent = {
                    if (isBatteryOptimized) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF15261E))
                                .border(1.dp, Color(0xFF224E3A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Allowed",
                                color = CleanBarGreenText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF282014))
                                .border(1.dp, Color(0xFF5E451A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Allow",
                                color = Color(0xFFFFB84D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                onClick = {
                    doVibrate(context)
                    try {
                        val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                        if (pm != null && pm.isIgnoringBatteryOptimizations(context.packageName)) {
                            triggerBottomToast("✓ Battery optimization already unrestricted")
                        } else {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallbackIntent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(fallbackIntent)
                            }
                        }
                    } catch (e: Exception) {
                        triggerBottomToast("Could not open battery settings")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Restore All to System Default Card
            MinimalCard(
                icon = Icons.Default.Restore,
                iconBg = Color(0xFF261818),
                iconTint = Color(0xFFFF7B7B),
                title = "Restore All to System Default",
                subtitle = "Reset everything before uninstalling",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    showRestoreConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Diagnostic & Error Logs Card
            MinimalCard(
                icon = Icons.Default.BugReport,
                iconBg = Color(0xFF1E2330),
                iconTint = CleanBarBlueText,
                title = "Diagnostic & Error Logs",
                subtitle = "View, copy or clear diagnostic event logs",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    showLogsDialog = true
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Support Section
            // ==========================================
            MinimalSectionTitle(title = "SUPPORT")

            Spacer(modifier = Modifier.height(6.dp))

            // UPI
            MinimalCard(
                drawableId = R.drawable.ic_upi_logo,
                iconBg = Color(0xFF19251E),
                title = "UPI • $UPI_ID",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy UPI ID",
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    clipboardManager.setText(AnnotatedString(UPI_ID))
                    triggerBottomToast("✓ Copied: $UPI_ID")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Binance
            MinimalCard(
                drawableId = R.drawable.ic_binance_logo,
                iconBg = Color(0xFF292416),
                title = "Binance • $BINANCE_UID",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Binance UID",
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    clipboardManager.setText(AnnotatedString(BINANCE_UID))
                    triggerBottomToast("✓ Copied: $BINANCE_UID")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Links Section
            // ==========================================
            MinimalSectionTitle(title = "LINKS")

            Spacer(modifier = Modifier.height(6.dp))

            // Check Updates
            MinimalCard(
                icon = Icons.Default.SystemUpdate,
                iconTint = CleanBarBlueText,
                iconBg = Color(0xFF18222E),
                title = "Check for Updates",
                trailingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            color = CleanBarBlueText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = "v${UpdateChecker.CURRENT_VERSION}",
                            color = CleanBarTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                onClick = {
                    doVibrate(context)
                    if (isCheckingUpdate) return@MinimalCard
                    isCheckingUpdate = true
                    coroutineScope.launch {
                        val res = UpdateChecker.checkForUpdates(UpdateChecker.CURRENT_VERSION)
                        isCheckingUpdate = false
                        res.onSuccess { update ->
                            if (update.hasUpdate) {
                                updateResult = update
                                showUpdateDialog = true
                            } else {
                                triggerBottomToast("CleanBar is up to date 🎉")
                            }
                        }.onFailure {
                            triggerBottomToast("CleanBar is up to date 🎉")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gmail (With Copy icon on the left of Launch icon)
            MinimalCard(
                icon = Icons.Default.Email,
                iconTint = CleanBarRedText,
                iconBg = Color(0xFF261919),
                title = "Gmail",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Copy Email Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    doVibrate(context)
                                    clipboardManager.setText(AnnotatedString(DEVELOPER_EMAIL))
                                    triggerBottomToast("✓ Copied: $DEVELOPER_EMAIL")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = CleanBarTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Launch Email Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    doVibrate(context)
                                    sendEmail(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Gmail",
                                tint = CleanBarTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                },
                onClick = {
                    doVibrate(context)
                    sendEmail(context)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GitHub
            MinimalCard(
                icon = Icons.Default.Code,
                iconTint = CleanBarGreenText,
                iconBg = Color(0xFF17241F),
                title = "GitHub",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    openBrowser(context, GITHUB_REPO_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Report Bug
            MinimalCard(
                icon = Icons.Default.BugReport,
                iconTint = Color(0xFFFFB86C),
                iconBg = Color(0xFF261F17),
                title = "Report Bug",
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    openBrowser(context, GITHUB_ISSUES_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Share App
            MinimalCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFFC678DD),
                iconBg = Color(0xFF231926),
                title = "Share App",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CleanBarTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                },
                onClick = {
                    doVibrate(context)
                    shareCleanBar(context)
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ==========================================
        // FLOATING BOTTOM TOAST (Neeche me Toast)
        // ==========================================
        AnimatedVisibility(
            visible = bottomToastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF252525))
                    .border(1.dp, Color(0xFF3E3E3E), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.widthIn(max = 330.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CleanBarGreenText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = bottomToastMessage ?: "",
                        color = CleanBarTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalSectionTitle(title: String) {
    Text(
        text = title,
        color = CleanBarTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun MinimalCard(
    icon: ImageVector? = null,
    drawableId: Int? = null,
    iconTint: Color = Color.Unspecified,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CleanBarDarkSurface)
            .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (drawableId != null) {
                        Icon(
                            painter = painterResource(id = drawableId),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = CleanBarTextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = CleanBarTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            trailingContent()
        }
    }
}

/**
 * Triggers intent to open Gmail / default email client.
 */
private fun sendEmail(context: Context) {
    val subject = "[CleanBar] Feedback"
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$DEVELOPER_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(emailIntent, "Open with..."))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Developer Email", DEVELOPER_EMAIL)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Email copied: $DEVELOPER_EMAIL", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Opens a web URL in browser.
 */
private fun openBrowser(context: Context, url: String) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Shares CleanBar link.
 */
private fun shareCleanBar(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CleanBar")
            putExtra(Intent.EXTRA_TEXT, GITHUB_REPO_URL)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share CleanBar"))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}
