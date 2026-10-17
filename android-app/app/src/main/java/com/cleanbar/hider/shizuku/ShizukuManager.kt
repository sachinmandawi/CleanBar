package com.cleanbar.hider.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    const val SHIZUKU_REQ_CODE = 1001
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

    fun isShizukuInstalled(context: android.content.Context): Boolean {
        if (isShizukuAvailable()) return true
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    SHIZUKU_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openShizuku(context: android.content.Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openShizukuPlayStore(context: android.content.Context) {
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://play.google.com/store/apps/details?id=$SHIZUKU_PACKAGE")
            ).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ignored: Throwable) {}
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        if (!isShizukuAvailable()) return
        if (Shizuku.isPreV11()) return

        try {
            Shizuku.removeRequestPermissionResultListener(listener)
        } catch (ignored: Throwable) {}

        try {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_REQ_CODE)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Executes shell command using Shizuku privileged binder process safely.
     */
    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext Result.failure(IllegalStateException("Shizuku permission not granted"))
        }

        var process: Process? = null
        try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process

            val output = StringBuilder()
            val errorOutput = StringBuilder()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }

            BufferedReader(InputStreamReader(process.errorStream)).use { errorReader ->
                var line: String?
                while (errorReader.readLine().also { line = it } != null) {
                    errorOutput.append(line).append("\n")
                }
            }

            val exitCode = process.waitFor()
            val outputStr = output.toString().trim()
            val errStr = errorOutput.toString().trim()

            if (exitCode == 0 || outputStr.isNotEmpty()) {
                Result.success(outputStr)
            } else {
                Result.failure(RuntimeException(if (errStr.isNotEmpty()) errStr else "Exit code: $exitCode"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            try {
                process?.destroy()
            } catch (ignored: Throwable) {}
        }
    }
}
