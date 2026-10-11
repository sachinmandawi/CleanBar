package com.cleanbar.hider.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
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
    var isHidden by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isPersistentMode by remember {
        mutableStateOf(PreferencesHelper.isPersistentModeEnabled(context))
    }

    fun vibrate() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (ignored: Throwable) {}
        doVibrate(context)
    }

    // Sync state with actual device status bar state on launch
    LaunchedEffect(isShizukuActive, hasShizukuPermission) {
        if (isShizukuActive && hasShizukuPermission) {
            isHidden = ImmersiveController.isCurrentlyHidden()
        }
    }

    // Continuously keep state updated whenever activity resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isShizukuActive, hasShizukuPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isShizukuActive && hasShizukuPermission) {
                    coroutineScope.launch {
                        isHidden = ImmersiveController.isCurrentlyHidden()
                    }
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
            
            // Top Bar with Status Pill and 3-Dot Overflow Menu
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                // Centered Shizuku Status Pill
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CleanBarDarkSurface)
                            .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                vibrate()
                                if (!hasShizukuPermission) onRequestPermission() else onRefreshStatus()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isShizukuActive && hasShizukuPermission) CleanBarGreenText else CleanBarRedText)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isShizukuActive && hasShizukuPermission) "Shizuku Ready" else "Tap to Authorize Shizuku",
                                color = CleanBarTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Top-Right 3-Dot Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
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
                        contentDescription = "About Developer & App",
                        tint = CleanBarTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // =================================================================
            // CENTER: PURE 1-CLICK MASTER BUTTON (No square/gray ripple box)
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
                            if (!isShizukuActive || !hasShizukuPermission) {
                                onRequestPermission()
                                return@clickable
                            }
                            isProcessing = true
                            coroutineScope.launch {
                                try {
                                    if (isHidden) {
                                        val res = ImmersiveController.resetToDefault()
                                        if (res.isSuccess) {
                                            isHidden = false
                                            PreferencesHelper.setTargetHidden(context, false)
                                            KeepAliveService.stop(context)
                                        }
                                    } else {
                                        val res = ImmersiveController.hideStatusBarGlobally()
                                        if (res.isSuccess) {
                                            isHidden = true
                                            PreferencesHelper.setTargetHidden(context, true)
                                            if (isPersistentMode) {
                                                onRequestNotificationPermission()
                                                KeepAliveService.start(context)
                                            }
                                        }
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
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
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHidden) "HIDDEN" else "VISIBLE",
                            color = if (isHidden) Color(0xFFFF8585) else Color(0xFF6CA8E8),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isHidden) "Status Bar is 100% HIDDEN\nTap button to make it VISIBLE" else "Status Bar is VISIBLE\nTap button to HIDE everything",
                    color = if (isHidden) Color(0xFFFF8585) else CleanBarTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            // Bottom Section: Persistent Auto-Reapply Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CleanBarDarkSurface)
                    .border(1.dp, CleanBarDarkBorder, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        vibrate()
                        val newState = !isPersistentMode
                        isPersistentMode = newState
                        PreferencesHelper.setPersistentModeEnabled(context, newState)
                        if (newState) {
                            onRequestNotificationPermission()
                            if (isHidden) {
                                KeepAliveService.start(context)
                            }
                        } else {
                            KeepAliveService.stop(context)
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isPersistentMode) CleanBarGreenText else CleanBarTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Reapply on Unlock",
                                color = CleanBarTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keeps status bar hidden after screen lock. Fixes reset on Vivo, Xiaomi & OnePlus.",
                            color = CleanBarTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Switch(
                        checked = isPersistentMode,
                        onCheckedChange = { newState ->
                            vibrate()
                            isPersistentMode = newState
                            PreferencesHelper.setPersistentModeEnabled(context, newState)
                            if (newState) {
                                onRequestNotificationPermission()
                                if (isHidden) {
                                    KeepAliveService.start(context)
                                }
                            } else {
                                KeepAliveService.stop(context)
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

        }
    }
}
