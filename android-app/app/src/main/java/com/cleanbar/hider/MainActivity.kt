package com.cleanbar.hider

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cleanbar.hider.shizuku.ShizukuManager
import com.cleanbar.hider.ui.AboutScreen
import com.cleanbar.hider.ui.MainScreen
import com.cleanbar.hider.ui.theme.CleanBarDarkBg
import com.cleanbar.hider.ui.theme.CleanBarTheme
import rikka.shizuku.Shizuku

enum class AppScreen {
    MAIN,
    ABOUT
}

class MainActivity : ComponentActivity() {

    private var isShizukuActive by mutableStateOf(false)
    private var hasShizukuPermission by mutableStateOf(false)

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkShizukuStatus()
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { /* handled */ }

    fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Defined as a field so it can be properly added AND removed from Shizuku
    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == ShizukuManager.SHIZUKU_REQ_CODE) {
                hasShizukuPermission = (grantResult == PackageManager.PERMISSION_GRANTED)
                isShizukuActive = ShizukuManager.isShizukuAvailable()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge with transparent dark system bars to prevent white flash
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        // Disable contrast scrims on Android 10+ and set window decor to dark
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#121212"))

        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            // Register permission result listener at activity start (not only on request)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        } catch (ignored: Throwable) {}

        checkShizukuStatus()

        setContent {
            CleanBarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CleanBarDarkBg
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                (slideInHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                                    initialOffsetX = { width -> (width * 0.25f).toInt() }
                                ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing))).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                                        targetOffsetX = { width -> -(width * 0.15f).toInt() }
                                    ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                                    initialOffsetX = { width -> -(width * 0.15f).toInt() }
                                ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing))).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                                        targetOffsetX = { width -> (width * 0.25f).toInt() }
                                    ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                                )
                            }
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                    when (screen) {
                        AppScreen.MAIN -> {
                            MainScreen(
                                isShizukuActive = isShizukuActive,
                                hasShizukuPermission = hasShizukuPermission,
                                onRequestPermission = {
                                    ShizukuManager.requestPermission(requestPermissionResultListener)
                                },
                                onRefreshStatus = {
                                    checkShizukuStatus()
                                },
                                onNavigateToAbout = {
                                    currentScreen = AppScreen.ABOUT
                                },
                                onRequestNotificationPermission = {
                                    requestNotificationPermission()
                                }
                            )
                        }
                        AppScreen.ABOUT -> {
                            AboutScreen(
                                onBack = {
                                    currentScreen = AppScreen.MAIN
                                },
                                onRequestNotificationPermission = {
                                    requestNotificationPermission()
                                },
                                isShizukuActive = isShizukuActive,
                                hasShizukuPermission = hasShizukuPermission,
                                onRequestShizukuPermission = {
                                    ShizukuManager.requestPermission(requestPermissionResultListener)
                                },
                                onRefreshShizukuStatus = {
                                    checkShizukuStatus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    private fun checkShizukuStatus() {
        isShizukuActive = ShizukuManager.isShizukuAvailable()
        hasShizukuPermission = ShizukuManager.hasPermission()
    }

    override fun onResume() {
        super.onResume()
        checkShizukuStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (ignored: Throwable) {}
    }
}
