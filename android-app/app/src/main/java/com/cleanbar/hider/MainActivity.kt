package com.cleanbar.hider

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.runtime.*
import com.cleanbar.hider.shizuku.ShizukuManager
import com.cleanbar.hider.ui.AboutScreen
import com.cleanbar.hider.ui.MainScreen
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
        super.onCreate(savedInstanceState)

        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            // Register permission result listener at activity start (not only on request)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        } catch (ignored: Throwable) {}

        checkShizukuStatus()

        setContent {
            CleanBarTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
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
