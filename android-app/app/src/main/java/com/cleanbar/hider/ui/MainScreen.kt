package com.cleanbar.hider.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cleanbar.hider.service.KeepAliveService
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.ui.theme.*
import com.cleanbar.hider.util.PreferencesHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun doVibrate(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(70)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun MainScreen(
    isShizukuActive: Boolean,
    hasShizukuPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRefreshStatus: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isHidden by remember { mutableStateOf(PreferencesHelper.isTargetHidden(context)) }
    var isProcessing by remember { mutableStateOf(false) }
    var isPersistentMode by remember {
        mutableStateOf(PreferencesHelper.isPersistentModeEnabled(context))
    }

    var bottomToastMessage by remember { mutableStateOf<String?>(null) }

    fun triggerBottomToast(message: String) {
        bottomToastMessage = message
        coroutineScope.launch {
            delay(2800)
            if (bottomToastMessage == message) {
                bottomToastMessage = null
            }
        }
    }

    fun vibrate() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (ignored: Throwable) {}
        doVibrate(context)
    }

    // Sync state with actual device status bar state on launch and when Shizuku status updates
    LaunchedEffect(hasShizukuPermission, isShizukuActive) {
        isHidden = ImmersiveController.isCurrentlyHidden(context)
    }

    // Continuously keep state updated whenever activity resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    isHidden = ImmersiveController.isCurrentlyHidden(context)
                    isPersistentMode = PreferencesHelper.isPersistentModeEnabled(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBarDarkBg)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Bar: Pure 3-Dot Button on Top-Right (No clutter)
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CleanBarDarkSurface)
                        .border(1.dp, CleanBarDarkBorder, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            vibrate()
                            onNavigateToAbout()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings & Modes",
                        tint = CleanBarTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // =================================================================
            // CENTER: PURE 1-CLICK MASTER BUTTON
            // =================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(if (isHidden) Color(0xFF261A1A) else Color(0xFF1A2330))
                        .border(
                            1.dp,
                            if (isHidden) Color(0xFF7F2D2D) else Color(0xFF2B4C7E),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isProcessing
                        ) {
                            vibrate()
                            if (!isShizukuActive) {
                                if (com.cleanbar.hider.shizuku.ShizukuManager.isShizukuInstalled(context)) {
                                    triggerBottomToast("Please start Shizuku service. Opening Shizuku...")
                                    com.cleanbar.hider.shizuku.ShizukuManager.openShizuku(context)
                                } else {
                                    triggerBottomToast("Shizuku app not found. Opening Play Store...")
                                    com.cleanbar.hider.shizuku.ShizukuManager.openShizukuPlayStore(context)
                                }
                                return@clickable
                            }
                            if (!hasShizukuPermission) {
                                onRequestPermission()
                                triggerBottomToast("Please allow Shizuku permission")
                                return@clickable
                            }

                            isProcessing = true
                            coroutineScope.launch {
                                try {
                                    if (isHidden) {
                                        // 1. Immediately mark not hidden and stop background service
                                        PreferencesHelper.setTargetHidden(context, false)
                                        KeepAliveService.stop(context)

                                        // 2. Perform clean factory restore
                                        val res = ImmersiveController.resetToDefault(context)
                                        if (res.isSuccess) {
                                            isHidden = false
                                            triggerBottomToast("✓ Status bar visible")
                                        } else {
                                            PreferencesHelper.setTargetHidden(context, true)
                                            triggerBottomToast(res.exceptionOrNull()?.message ?: "Failed to make visible")
                                        }
                                    } else {
                                        PreferencesHelper.setTargetHidden(context, true)
                                        val res = ImmersiveController.hideStatusBar(context)
                                        if (res.isSuccess) {
                                            isHidden = true
                                            if (PreferencesHelper.isPersistentModeEnabled(context)) {
                                                onRequestNotificationPermission()
                                                KeepAliveService.start(context)
                                            }
                                            triggerBottomToast("✓ Status bar hidden")
                                        } else {
                                            PreferencesHelper.setTargetHidden(context, false)
                                            triggerBottomToast(res.exceptionOrNull()?.message ?: "Failed to hide status bar")
                                        }
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    triggerBottomToast("Operation failed: ${e.message}")
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (isHidden) Color(0xFFFF8585) else Color(0xFF6CA8E8),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHidden) "HIDDEN" else "VISIBLE",
                            color = if (isHidden) Color(0xFFFF8585) else Color(0xFF6CA8E8),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHidden) "TAP TO SHOW" else "TAP TO HIDE",
                            color = if (isHidden) Color(0xFF4DAB9A) else CleanBarTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isHidden) "Status Bar is HIDDEN\nTap button to make it VISIBLE" else "Status Bar is VISIBLE\nTap button to make it HIDDEN",
                    color = if (isHidden) Color(0xFFFF8585) else CleanBarTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            // Bottom Spacer (Balances top bar for center alignment)
            Spacer(modifier = Modifier.size(38.dp))

        }

        // ==========================================
        // FLOATING BOTTOM TOAST (No OEM Truncation)
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
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CleanBarBlueText,
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
