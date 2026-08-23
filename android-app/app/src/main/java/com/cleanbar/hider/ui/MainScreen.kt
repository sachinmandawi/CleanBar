package com.cleanbar.hider.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.ui.theme.*
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
    onRefreshStatus: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isHidden by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBarDarkBg)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Top Status Pill
            Box(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = CleanBarDarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanBarDarkBorder),
                    modifier = Modifier.clickable {
                        vibrate()
                        if (!hasShizukuPermission) onRequestPermission() else onRefreshStatus()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
                        .clickable(enabled = !isProcessing) {
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
                                        }
                                    } else {
                                        val res = ImmersiveController.hideStatusBarGlobally()
                                        if (res.isSuccess) {
                                            isHidden = true
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

            // Bottom Spacer
            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}
