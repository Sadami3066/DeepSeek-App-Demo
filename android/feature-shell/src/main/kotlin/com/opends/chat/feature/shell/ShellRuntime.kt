package com.opends.chat.feature.shell

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opends.chat.core.AppSettings
import com.opends.chat.core.ArchiveService
import com.opends.chat.core.OpenDS
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.SessionStore
import com.opends.chat.core.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

internal fun runningSettings(): SettingsStore? = runCatching { OpenDS.settings }.getOrNull()

internal fun runningSessions(): SessionStore? = runCatching { OpenDS.sessions }.getOrNull()

internal fun runningArchive(): ArchiveService? = runCatching { OpenDS.archive }.getOrNull()

@Composable
internal fun rememberSettingsState(): AppSettings {
    val store = remember { runningSettings() }
    val flow = remember(store) { store?.state ?: MutableStateFlow(AppSettings()) }
    return flow.collectAsStateWithLifecycle().value
}

@Composable
internal fun rememberSessionList(): List<SessionInfo> {
    val store = remember { runningSessions() }
    if (store == null) {
        return remember { emptyList() }
    }
    val flow = remember(store) { store.observeSessions() }
    return flow.collectAsStateWithLifecycle(initialValue = emptyList()).value
}

@Composable
internal fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.1.0"
    }
}

internal fun openExternalUrl(context: Context, url: String): Boolean = runCatching {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
    true
}.getOrDefault(false)

internal suspend fun readUriBytes(context: Context, uri: Uri): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()
    }

internal suspend fun writeUriBytes(context: Context, uri: Uri, bytes: ByteArray): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
        }.getOrDefault(false)
    }

internal fun uriFileName(context: Context, uri: Uri): String =
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull() ?: uri.lastPathSegment ?: "data"

private const val SHELL_PREFS = "deepfish_shell_prefs"
private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"

internal fun isDisclaimerAccepted(context: Context): Boolean =
    context.getSharedPreferences(SHELL_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_DISCLAIMER_ACCEPTED, false)

internal fun acceptDisclaimer(context: Context) {
    context.getSharedPreferences(SHELL_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_DISCLAIMER_ACCEPTED, true)
        .apply()
}

internal enum class ImportKind { ZIP, DATABASE }

internal fun detectImportKind(fileName: String, bytes: ByteArray): ImportKind? {
    val lower = fileName.lowercase()
    val isDatabase = bytes.size >= 16 &&
        String(bytes, 0, 16, Charsets.ISO_8859_1) == "SQLite format 3\u0000"
    val isZip = bytes.size >= 4 &&
        bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
        (bytes[2] == 0x03.toByte() || bytes[2] == 0x05.toByte()) &&
        (bytes[3] == 0x04.toByte() || bytes[3] == 0x06.toByte())
    return when {
        lower.endsWith(".db") || lower.endsWith(".sqlite") || isDatabase -> ImportKind.DATABASE
        lower.endsWith(".zip") || isZip -> ImportKind.ZIP
        else -> null
    }
}

internal fun describeError(error: Throwable): String =
    error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
