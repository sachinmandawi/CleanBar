package com.cleanbar.hider.util

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import com.cleanbar.hider.shizuku.ShizukuManager
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val MAX_LOGS = 150
    private val logQueue = ArrayDeque<LogEntry>()
    private val lock = Any()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String
    )

    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val extra = throwable?.let { " [${it.javaClass.simpleName}: ${it.message}]" } ?: ""
        log("WARN", tag, message + extra)
    }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val stack = throwable?.let { "\n${android.util.Log.getStackTraceString(it)}" } ?: ""
        log("ERROR", tag, message + stack)
    }

    private fun log(level: String, tag: String, message: String) {
        synchronized(lock) {
            if (logQueue.size >= MAX_LOGS) {
                logQueue.pollFirst()
            }
            logQueue.addLast(LogEntry(System.currentTimeMillis(), level, tag, message))
        }
        // Also forward to Android logcat
        when (level) {
            "DEBUG" -> android.util.Log.d(tag, message)
            "INFO" -> android.util.Log.i(tag, message)
            "WARN" -> android.util.Log.w(tag, message)
            "ERROR" -> android.util.Log.e(tag, message)
        }
    }

    fun getLogs(): List<LogEntry> {
        synchronized(lock) {
            return logQueue.toList()
        }
    }

    fun clearLogs() {
        synchronized(lock) {
            logQueue.clear()
        }
    }

    suspend fun getFormattedDiagnosticReport(context: Context): String {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isBatteryExempt = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

        val sb = StringBuilder()
        sb.append("### CleanBar Diagnostic Report\n")
        sb.append("```yaml\n")
        sb.append("App Version: ").append(UpdateChecker.CURRENT_VERSION).append(" (5)\n")
        sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n")
        sb.append("Device Model: ").append(Build.MODEL).append(" (").append(Build.DEVICE).append(")\n")
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
        sb.append("Build Fingerprint: ").append(Build.FINGERPRINT).append("\n")
        sb.append("Shizuku Installed: ").append(ShizukuManager.isShizukuInstalled(context)).append("\n")
        sb.append("Shizuku Running: ").append(ShizukuManager.isShizukuAvailable()).append("\n")
        sb.append("Shizuku Permission: ").append(ShizukuManager.hasPermission()).append("\n")
        sb.append("Battery Optimization Exempt: ").append(isBatteryExempt).append("\n")
        sb.append("Notifications Enabled: ").append(areNotificationsEnabled).append("\n")
        sb.append("Target Status Bar Hidden: ").append(PreferencesHelper.isTargetHidden(context)).append("\n")
        sb.append("Auto-Reapply on Unlock: ").append(PreferencesHelper.isPersistentModeEnabled(context)).append("\n")

        if (ShizukuManager.hasPermission()) {
            val policyRes = ShizukuManager.executeCommand("settings get global policy_control")
            val policy = policyRes.getOrDefault("").trim()
            sb.append("policy_control: ").append(if (policy.isEmpty()) "null" else policy).append("\n")

            val disableRes = ShizukuManager.executeCommand("dumpsys statusbar | grep mDisabled")
            val disabled = disableRes.getOrDefault("").trim().replace("\n", "; ")
            sb.append("StatusBar mDisabled: ").append(if (disabled.isEmpty()) "null" else disabled).append("\n")
        }
        sb.append("```\n\n")

        sb.append("#### Recent Event Logs (Last ").append(logQueue.size).append("):\n")
        sb.append("```text\n")
        val currentLogs = getLogs()
        if (currentLogs.isEmpty()) {
            sb.append("No events logged yet.\n")
        } else {
            for (entry in currentLogs) {
                val time = timeFormat.format(Date(entry.timestamp))
                sb.append("[").append(time).append("] [").append(entry.level).append("] [")
                    .append(entry.tag).append("] ").append(entry.message).append("\n")
            }
        }
        sb.append("```\n")

        return sb.toString()
    }
}
