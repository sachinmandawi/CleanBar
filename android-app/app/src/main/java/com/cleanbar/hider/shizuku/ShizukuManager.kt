package com.cleanbar.hider.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    const val SHIZUKU_REQ_CODE = 1001

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
