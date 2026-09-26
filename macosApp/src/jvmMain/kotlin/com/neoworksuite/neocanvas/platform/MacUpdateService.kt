package com.neoworksuite.neocanvas.platform

import com.neoworksuite.neocanvas.ui.AppUpdateInfo
import java.awt.EventQueue
import java.net.HttpURLConnection
import java.net.URI

internal object MacUpdateService {
    private const val latestReleaseApi = "https://api.github.com/repos/neoworkssuite/NeoCanvas/releases/latest"

    fun check(onResult: (Result<AppUpdateInfo?>) -> Unit) {
        Thread({
            val result = runCatching { requestLatestRelease() }
            EventQueue.invokeLater { onResult(result) }
        }, "NeoCanvas-macOS-UpdateCheck").apply { isDaemon = true; start() }
    }

    internal fun parseLatestRelease(json: String): AppUpdateInfo? {
        val version = jsonString(json, "tag_name")?.trim().orEmpty().removePrefix("v").removePrefix("V")
        if (version.isBlank()) return null
        val releasePage = jsonString(json, "html_url")?.takeIf { it.startsWith("https://") }
        val download = Regex("\\\"browser_download_url\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
            .findAll(json).map { decodeJsonString(it.groupValues[1]) }
            .firstOrNull { it.startsWith("https://") && it.substringBefore('?').endsWith(".dmg", true) }
        return AppUpdateInfo(
            version = version,
            storeUrl = download ?: releasePage ?: return null,
            releaseNotes = jsonString(json, "body")?.trim()?.takeIf(String::isNotBlank),
        )
    }

    private fun requestLatestRelease(): AppUpdateInfo? {
        val connection = URI(latestReleaseApi).toURL().openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 6_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "NeoCanvas-macOS/1.0.0")
            when (val status = connection.responseCode) {
                HttpURLConnection.HTTP_NOT_FOUND -> null
                in 200..299 -> connection.inputStream.bufferedReader().use { parseLatestRelease(it.readText()) }
                else -> error("Release service returned HTTP $status")
            }
        } finally { connection.disconnect() }
    }

    private fun jsonString(json: String, key: String): String? {
        val match = Regex("\\\"" + Regex.escape(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"").find(json) ?: return null
        return decodeJsonString(match.groupValues[1])
    }

    private fun decodeJsonString(value: String): String = value
        .replace("\\\\/", "/").replace("\\\\n", "\n").replace("\\\\r", "\r")
        .replace("\\\\t", "\t").replace("\\\\\"", "\"").replace("\\\\\\\\", "\\")
}
