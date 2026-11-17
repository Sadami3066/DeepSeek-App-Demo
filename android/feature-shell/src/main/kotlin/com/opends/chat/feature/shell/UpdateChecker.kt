package com.opends.chat.feature.shell

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal data class UpdateAsset(
    val name: String,
    val url: String,
    val size: Long,
)

internal data class UpdateRelease(
    val version: String,
    val notes: String,
    val asset: UpdateAsset,
)

private const val RELEASES_URL =
    "https://api.github.com/repos/Sadami3066/DeepSeek-App-Demo/releases/latest"
private const val UPDATE_DIR = "update"
private const val UPDATE_PREFS = "deepfish_update_prefs"
private const val KEY_PROMPT_VERSION = "prompted_version"
private const val KEY_PROMPT_AT = "prompted_at"
private const val PROMPT_INTERVAL_MS = 24L * 60L * 60L * 1000L

internal suspend fun fetchLatestRelease(): Result<UpdateRelease> = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(RELEASES_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "DeepFish")
        }
        try {
            if (connection.responseCode !in 200..299) {
                error("HTTP ${connection.responseCode}")
            }
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val version = json.optString("tag_name", "")
                .removePrefix("v")
                .removePrefix("V")
                .trim()
            if (version.isEmpty()) error("missing tag_name")
            val notes = json.optString("body", "")
            val assets = json.optJSONArray("assets")
            var asset: UpdateAsset? = null
            if (assets != null) {
                for (index in 0 until assets.length()) {
                    val item = assets.optJSONObject(index) ?: continue
                    val name = item.optString("name", "")
                    val url = item.optString("browser_download_url", "")
                    if (name.endsWith(".apk", ignoreCase = true) && url.isNotEmpty()) {
                        asset = UpdateAsset(name = name, url = url, size = item.optLong("size", 0L))
                        break
                    }
                }
            }
            UpdateRelease(version = version, notes = notes, asset = asset ?: error("no apk asset"))
        } finally {
            connection.disconnect()
        }
    }
}

internal fun isRemoteNewer(remoteVersion: String, currentVersion: String): Boolean {
    fun segments(value: String): List<Int> =
        Regex("\\d+").findAll(value).map { it.value.toIntOrNull() ?: 0 }.toList()

    val remote = segments(remoteVersion)
    val current = segments(currentVersion)
    if (remote.isEmpty()) return false
    val size = maxOf(remote.size, current.size)
    for (index in 0 until size) {
        val remotePart = remote.getOrElse(index) { 0 }
        val currentPart = current.getOrElse(index) { 0 }
        if (remotePart != currentPart) {
            return remotePart > currentPart
        }
    }
    return false
}

internal fun currentVersionName(context: Context): String =
    runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "0.0.0"

internal fun shouldPromptUpdate(context: Context, version: String): Boolean {
    val prefs = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
    val promptedVersion = prefs.getString(KEY_PROMPT_VERSION, null)
    val promptedAt = prefs.getLong(KEY_PROMPT_AT, 0L)
    if (promptedVersion != version) return true
    return System.currentTimeMillis() - promptedAt >= PROMPT_INTERVAL_MS
}

internal fun markUpdatePrompted(context: Context, version: String) {
    context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_PROMPT_VERSION, version)
        .putLong(KEY_PROMPT_AT, System.currentTimeMillis())
        .apply()
}

internal suspend fun downloadApk(
    context: Context,
    asset: UpdateAsset,
    version: String,
    onProgress: (Int) -> Unit,
): File = withContext(Dispatchers.IO) {
    val directory = File(context.cacheDir, UPDATE_DIR).apply { mkdirs() }
    val target = File(directory, "DeepFish-$version.apk")
    val connection = (URL(asset.url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 30_000
        setRequestProperty("Accept", "application/octet-stream")
        setRequestProperty("User-Agent", "DeepFish")
    }
    try {
        if (connection.responseCode !in 200..299) {
            error("HTTP ${connection.responseCode}")
        }
        val total = connection.contentLengthLong.takeIf { it > 0 } ?: asset.size
        var received = 0L
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    received += read
                    if (total > 0) {
                        onProgress(((received * 100) / total).toInt().coerceIn(0, 100))
                    }
                }
            }
        }
        if (target.length() <= 0L) error("empty apk")
        target
    } finally {
        connection.disconnect()
    }
}

internal fun installApk(context: Context, file: File): Result<Unit> = runCatching {
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
