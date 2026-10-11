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
    private var isReceiverRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == Intent.ACTION_USER_PRESENT || action == Intent.ACTION_SCREEN_ON) {
                reapplyIfHidden()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        registerScreenReceiver()
        startPeriodicWatcher()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Re-apply immediately when service command is triggered
        reapplyIfHidden()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
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
            .setSmallIcon(R.drawable.ic_status_bar_tile)
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
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun unregisterScreenReceiver() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(screenReceiver)
            isReceiverRegistered = false
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun reapplyIfHidden() {
        if (!PreferencesHelper.isPersistentModeEnabled(this)) return
        if (!PreferencesHelper.isTargetHidden(this)) return
        if (!ShizukuManager.hasPermission()) return

        serviceScope.launch(Dispatchers.IO) {
            try {
                // Pass 1: Immediate reapply
                ImmersiveController.hideStatusBarGlobally()

                // Pass 2: Delay 350ms to override aggressive OEM keyguard dismiss cleanup (Vivo OriginOS / Xiaomi HyperOS)
                delay(350)
                ImmersiveController.hideStatusBarGlobally()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun startPeriodicWatcher() {
        serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                // Check every 10 minutes to fix silent OEM timeout resets (e.g. OriginOS 20-min reset)
                delay(10 * 60 * 1000L)
                if (PreferencesHelper.isPersistentModeEnabled(this@KeepAliveService) &&
                    PreferencesHelper.isTargetHidden(this@KeepAliveService) &&
                    ShizukuManager.hasPermission()
                ) {
                    val currentlyHidden = ImmersiveController.isCurrentlyHidden()
                    if (!currentlyHidden) {
                        ImmersiveController.hideStatusBarGlobally()
                    }
                }
            }
        }
    }
}
