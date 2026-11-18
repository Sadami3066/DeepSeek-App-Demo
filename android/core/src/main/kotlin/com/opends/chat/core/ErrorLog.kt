package com.opends.chat.core

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ErrorLog {
    private const val MAX_ENTRIES = 80
    private const val MAX_FILE_BYTES = 256L * 1024
    private const val MAX_DETAIL = 1200
    private const val SEPARATOR = "\n----\n"

    private val lock = Any()
    private val entries = ArrayList<String>()
    private var logFile: File? = null
    private var appVersion = ""

    internal fun install(context: Context) {
        val app = context.applicationContext
        val file = File(app.filesDir, "logs/error.log")
        val version = runCatching {
            app.packageManager.getPackageInfo(app.packageName, 0).versionName
        }.getOrNull().orEmpty()
        val loaded = runCatching {
            if (file.isFile) {
                file.readText(Charsets.UTF_8).split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
        synchronized(lock) {
            appVersion = version
            logFile = file
            entries.clear()
            entries.addAll(loaded.takeLast(MAX_ENTRIES))
        }
        installCrashHandler()
    }

    fun record(category: String, summary: String, detail: String? = null, error: Throwable? = null) {
        val entry = buildEntry(category, summary, detail, error)
        synchronized(lock) {
            entries.add(entry)
            while (entries.size > MAX_ENTRIES) entries.removeAt(0)
            val file = logFile ?: return
            runCatching {
                file.parentFile?.mkdirs()
                file.appendText(entry + SEPARATOR, Charsets.UTF_8)
            }
            runCatching { if (file.length() > MAX_FILE_BYTES) rewriteLocked(file) }
        }
    }

    fun text(): String {
        val snapshot = synchronized(lock) { entries.toList() }
        val header = buildString {
            append("DeepFish ")
            append(appVersion.ifBlank { "?" })
            append(" · Android ")
            append(Build.VERSION.RELEASE)
            append(" (API ")
            append(Build.VERSION.SDK_INT)
            append(") · ")
            append(Build.MODEL)
            append('\n')
            append("时间: ")
            append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            append('\n')
            append("记录条数: ")
            append(snapshot.size)
            append('\n')
            append("说明: 仅记录错误类型、接口与状态，不包含对话内容和 API Key。")
            append('\n')
        }
        if (snapshot.isEmpty()) return header + "\n暂无错误记录"
        return header + "\n" + snapshot.asReversed().joinToString(SEPARATOR)
    }

    fun isEmpty(): Boolean = synchronized(lock) { entries.isEmpty() }

    fun clear() {
        synchronized(lock) {
            entries.clear()
            logFile?.let { file -> runCatching { if (file.isFile) file.delete() } }
        }
    }

    private fun rewriteLocked(file: File) {
        val keep = entries.takeLast(MAX_ENTRIES / 2)
        file.writeText(keep.joinToString(SEPARATOR) + SEPARATOR, Charsets.UTF_8)
    }

    private fun buildEntry(category: String, summary: String, detail: String?, error: Throwable?): String {
        val time = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date())
        val builder = StringBuilder()
        builder.append('[').append(time).append("] ").append(category).append(" · ")
        builder.append(sanitize(summary, 200))
        detail?.takeIf { it.isNotBlank() }?.let { value ->
            builder.append('\n').append("  ").append(sanitize(value, MAX_DETAIL).replace("\n", "\n  "))
        }
        error?.let { builder.append('\n').append("  ").append(describeThrowable(it)) }
        return builder.toString()
    }

    private fun describeThrowable(error: Throwable): String {
        val builder = StringBuilder()
        builder.append(error.javaClass.name)
        val message = error.message
        if (!message.isNullOrBlank()) builder.append(": ").append(sanitize(message, 400))
        error.stackTrace.take(4).forEach { frame ->
            builder.append('\n').append("    at ").append(frame.className).append('.')
                .append(frame.methodName).append('(').append(frame.fileName ?: "?").append(':')
                .append(frame.lineNumber).append(')')
        }
        val cause = error.cause
        if (cause != null && cause !== error) {
            builder.append('\n').append("  caused by ").append(cause.javaClass.name).append(": ")
                .append(sanitize(cause.message ?: "", 300))
        }
        return builder.toString()
    }

    private fun sanitize(raw: String, max: Int): String {
        val text = raw.replace('\u0000', ' ').trim()
        return if (text.length <= max) text else text.take(max) + "…"
    }

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        if (previous is ErrorLogHandler) return
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogHandler(previous))
    }

    private class ErrorLogHandler(
        private val previous: Thread.UncaughtExceptionHandler?,
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, error: Throwable) {
            runCatching { record("崩溃", "未捕获异常：${thread.name}", null, error) }
            if (previous != null) {
                previous.uncaughtException(thread, error)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
}
