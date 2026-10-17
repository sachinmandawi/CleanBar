package com.cleanbar.hider.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cleanbar.hider.MainActivity
import com.cleanbar.hider.R
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.shizuku.ShizukuManager
import com.cleanbar.hider.util.AppLogger
import com.cleanbar.hider.util.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KeepAliveService : Service() {

    companion object {
        private const val CHANNEL_ID = "cleanbar_persistent"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            PreferencesHelper.setTargetHidden(context, false)
            val intent = Intent(context, KeepAliveService::class.java)
            try {
                context.stopService(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var reapplyJob: kotlinx.coroutines.Job? = null
    private var isReceiverRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: return
            AppLogger.i("KeepAliveService", "Broadcast received: $action")
            if (action == Intent.ACTION_USER_PRESENT ||
                action == Intent.ACTION_SCREEN_ON ||
                action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS ||
                action == Intent.ACTION_CONFIGURATION_CHANGED
            ) {
                reapplyIfHidden()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.i("KeepAliveService", "Service created, starting in foreground...")
        startInForeground()
        registerScreenReceiver()
        startPeriodicWatcher()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d("KeepAliveService", "onStartCommand triggered")
        reapplyIfHidden()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.i("KeepAliveService", "Service destroyed")
        reapplyJob?.cancel()
        unregisterScreenReceiver()
        serviceScope.cancel()
    }

    private fun startInForeground() {
        createNotificationChannel()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CleanBar Active")
            .setContentText("Status bar hidden • Auto-reapply on unlock")
            .setSmallIcon(R.drawable.ic_cleanbar_logo_vector)
            .setColor(0xFF2563EB.toInt())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "CleanBar Persistent Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Monitors screen lock state to keep status bar hidden"
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun registerScreenReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    this,
                    screenReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else {
                registerReceiver(screenReceiver, filter)
            }
            isReceiverRegistered = true
            android.util.Log.i("CleanBar", "KeepAliveService: Screen broadcast receiver registered with RECEIVER_EXPORTED")
        } catch (e: Throwable) {
            android.util.Log.e("CleanBar", "KeepAliveService: Failed to register screen receiver", e)
        }
    }

    private fun unregisterScreenReceiver() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(screenReceiver)
            isReceiverRegistered = false
            android.util.Log.i("CleanBar", "KeepAliveService: Screen broadcast receiver unregistered")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun reapplyIfHidden() {
        if (!PreferencesHelper.isPersistentModeEnabled(this)) {
            android.util.Log.d("CleanBar", "KeepAliveService: Persistent mode is disabled, ignoring event")
            return
        }
        if (!PreferencesHelper.isTargetHidden(this)) {
            android.util.Log.d("CleanBar", "KeepAliveService: Target status is VISIBLE, ignoring event")
            return
        }
        if (!ShizukuManager.hasPermission()) {
            android.util.Log.w("CleanBar", "KeepAliveService: Shizuku permission missing, cannot reapply")
            return
        }

        AppLogger.i("KeepAliveService", "Triggering multi-pass reapply sequence...")
        reapplyJob?.cancel()
        reapplyJob = serviceScope.launch(Dispatchers.IO) {
            try {
                if (!PreferencesHelper.isTargetHidden(this@KeepAliveService)) return@launch
                // Pass 1: Immediate reapply (0ms) - resets to none then applies flags
                ImmersiveController.hideStatusBar(this@KeepAliveService)

                // Pass 2: 350ms (Mid-keyguard unlock animation)
                delay(350)
                if (!PreferencesHelper.isTargetHidden(this@KeepAliveService)) return@launch
                ImmersiveController.hideStatusBar(this@KeepAliveService)

                // Pass 3: 750ms (Post-keyguard dismiss on Vivo OriginOS / Funtouch / HyperOS)
                delay(400)
                if (!PreferencesHelper.isTargetHidden(this@KeepAliveService)) return@launch
                ImmersiveController.hideStatusBar(this@KeepAliveService)

                // Pass 4: 1400ms (Final OEM transition cleanup for high refresh rate displays)
                delay(650)
                if (!PreferencesHelper.isTargetHidden(this@KeepAliveService)) return@launch
                ImmersiveController.hideStatusBar(this@KeepAliveService)
                AppLogger.d("KeepAliveService", "✓ Re-apply sequence finished")
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Expected when a new event supersedes the previous reapply pass
            } catch (e: Throwable) {
                AppLogger.e("KeepAliveService", "Reapply error", e)
            }
        }
    }

    private fun startPeriodicWatcher() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                // Check periodically while screen is interactive to catch any silent OEM resets
                delay(30 * 1000L)
                if (PreferencesHelper.isPersistentModeEnabled(this@KeepAliveService) &&
                    PreferencesHelper.isTargetHidden(this@KeepAliveService) &&
                    ShizukuManager.hasPermission() &&
                    powerManager?.isInteractive == true
                ) {
                    try {
                        ImmersiveController.hideStatusBar(this@KeepAliveService)
                    } catch (ignored: Throwable) {}
                }
            }
        }
    }
}
