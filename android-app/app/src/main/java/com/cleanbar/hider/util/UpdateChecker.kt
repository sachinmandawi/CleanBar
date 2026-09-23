package com.cleanbar.hider.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    const val CURRENT_VERSION = "1.1.0"
    private const val GITHUB_API_URL = "https://api.github.com/repos/sachinmandawi/CleanBar/releases/latest"

    data class UpdateResult(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val downloadUrl: String,
        val releaseNotes: String = ""
    )

    suspend fun checkForUpdates(currentVersion: String = CURRENT_VERSION): Result<UpdateResult> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            var connection: HttpURLConnection? = null
            try {
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "CleanBar-Android-App")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val rawTag = json.optString("tag_name", "").trim()
                    val latestVer = rawTag.removePrefix("v").removePrefix("V")
                    val releaseUrl = json.optString("html_url", "https://github.com/sachinmandawi/CleanBar/releases/latest")
                    val notes = json.optString("body", "")

                    // Find .apk asset url if available, otherwise fallback to release webpage
                    var downloadUrl = releaseUrl
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = asset.optString("browser_download_url", releaseUrl)
                                break
                            }
                        }
                    }

                    val isNewer = isNewerVersion(latestVer, currentVersion)
                    Result.success(
                        UpdateResult(
                            hasUpdate = isNewer,
                            latestVersion = if (rawTag.isNotEmpty()) rawTag else "v$latestVer",
                            downloadUrl = downloadUrl,
                            releaseNotes = notes
                        )
                    )
                } else {
                    Result.failure(Exception("Server returned HTTP $responseCode"))
                }
            } finally {
                connection?.disconnect()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        if (latest.isEmpty()) return false
        val lParts = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(lParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val l = lParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
