package com.opends.chat.core.net

import com.opends.chat.core.ErrorLog
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

internal enum class ApiErrorKind { HTTP, STREAM, NETWORK, INTERRUPTED, PROTOCOL, UPLOAD }

internal enum class ApiCondition {
    UNAUTHORIZED,
    INSUFFICIENT_BALANCE,
    TOO_LARGE,
    RATE_LIMITED,
    SERVER_BUSY,
    NETWORK,
    OTHER,
}

internal class ApiException(
    val kind: ApiErrorKind,
    override val message: String,
    val status: Int? = null,
    val retryable: Boolean = false,
    val endpoint: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    val condition: ApiCondition
        get() = when {
            kind == ApiErrorKind.NETWORK -> ApiCondition.NETWORK
            status == 401 -> ApiCondition.UNAUTHORIZED
            status == 402 -> ApiCondition.INSUFFICIENT_BALANCE
            status == 413 -> ApiCondition.TOO_LARGE
            status == 429 -> ApiCondition.RATE_LIMITED
            status != null && status >= 500 -> ApiCondition.SERVER_BUSY
            else -> ApiCondition.OTHER
        }

    val isRetryable: Boolean
        get() = status == null || status == 429 || status == 408 || status >= 500

    val userText: String
        get() = when {
            kind == ApiErrorKind.NETWORK -> networkDetail(cause)
            kind == ApiErrorKind.INTERRUPTED -> message
            kind == ApiErrorKind.UPLOAD -> message
            kind == ApiErrorKind.PROTOCOL -> message
            status == 401 -> "API Key 无效或已被删除，请重新填写"
            status == 402 -> "账户余额不足，请前往 DeepSeek 开放平台充值后再试"
            status == 403 -> detailOr("当前 API Key 无权访问该模型或接口（HTTP 403）")
            status == 413 -> "上传内容超出服务端限制，请减小文件后重试"
            status == 429 -> "请求过于频繁，请稍后再试（HTTP 429）"
            status != null && status >= 500 -> "服务器繁忙，请稍后再试（HTTP $status）"
            status == 400 && message.contains("Content Exists Risk", ignoreCase = true) ->
                "输入内容未通过内容安全审核，请调整后重试"
            status == 400 && (
                message.contains("context length", ignoreCase = true) ||
                    message.contains("too long", ignoreCase = true) ||
                    message.contains("context_length", ignoreCase = true) ||
                    message.contains("maximum context", ignoreCase = true)
                ) -> "上下文长度已超出模型限制，请开启新对话或删除部分历史消息后重试"
            status != null -> detailOr("请求失败（HTTP $status）")
            message.isNotBlank() -> message
            else -> "请求失败，请稍后重试"
        }

    private fun detailOr(fallback: String): String {
        val text = message.trim()
        if (text.isEmpty() || text.equals(fallback, ignoreCase = true)) return fallback
        if (text.startsWith("HTTP")) return fallback
        return if (text.length > 200) fallback else text
    }

    companion object {
        fun network(cause: Throwable, endpoint: String? = null): ApiException {
            ErrorLog.record(
                category = "网络",
                summary = "请求失败：${endpoint ?: "未知接口"}",
                detail = null,
                error = cause,
            )
            return ApiException(
                ApiErrorKind.NETWORK,
                cause.message ?: "网络连接失败",
                endpoint = endpoint,
                cause = cause,
            )
        }

        fun protocol(message: String): ApiException =
            ApiException(ApiErrorKind.PROTOCOL, message)

        fun interrupted(message: String = "生成已中断"): ApiException =
            ApiException(ApiErrorKind.INTERRUPTED, message)

        fun stream(message: String): ApiException =
            ApiException(ApiErrorKind.STREAM, message)

        fun upload(message: String, retryable: Boolean): ApiException =
            ApiException(ApiErrorKind.UPLOAD, message, retryable = retryable)

        fun http(code: Int, message: String, body: String, endpoint: String? = null): ApiException {
            val text = apiErrorMessage(body).orEmpty().ifBlank {
                body.trim().take(300).ifBlank { message.ifBlank { "HTTP $code" } }
            }
            ErrorLog.record(
                category = "接口",
                summary = "${endpoint ?: "未知接口"} 返回 HTTP $code",
                detail = text.take(600),
            )
            return ApiException(ApiErrorKind.HTTP, text, code, endpoint = endpoint)
        }
    }
}

internal fun apiErrorMessage(body: String): String? {
    if (body.isBlank()) return null
    val root = runCatching { com.google.gson.JsonParser.parseString(body) }.getOrNull() ?: return null
    if (!root.isJsonObject) return null
    val json = root.asJsonObject
    val error = json.get("error")
    if (error != null && error.isJsonObject) {
        val errorObject = error.asJsonObject
        errorObject.get("message")?.takeIf { it.isJsonPrimitive }
            ?.let { runCatching { it.asString }.getOrNull() }
            ?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return json.get("message")?.takeIf { it.isJsonPrimitive }
        ?.let { runCatching { it.asString }.getOrNull() }
        ?.takeIf { it.isNotBlank() }
}

internal fun networkDetail(cause: Throwable?): String = when (cause) {
    is UnknownHostException -> "网络异常：无法解析服务器域名，请检查网络或关闭代理后重试"
    is SSLException -> "网络异常：与服务器的安全连接失败，请检查系统时间或关闭代理后重试"
    is SocketTimeoutException -> "网络超时：连接服务器超时，请检查网络后重试"
    is ConnectException -> "网络异常：无法连接到服务器，请检查网络或代理设置"
    is NoRouteToHostException -> "网络异常：网络不可达，请检查网络或代理设置"
    is SocketException -> "网络异常：连接已中断，请重试"
    else -> {
        val message = cause?.message.orEmpty()
        when {
            message.contains("closed", ignoreCase = true) ||
                message.contains("reset", ignoreCase = true) ||
                message.contains("broken pipe", ignoreCase = true) ||
                message.contains("unexpected end", ignoreCase = true) ||
                message.contains("EOF", ignoreCase = true) ->
                "网络异常：与服务器的连接意外中断，请重试"
            message.contains("timeout", ignoreCase = true) ||
                message.contains("timed out", ignoreCase = true) ->
                "网络超时：等待服务器响应超时，请检查网络后重试"
            message.isNotBlank() -> "网络异常：${message.take(120)}"
            else -> "网络异常，请检查你的网络状况"
        }
    }
}

internal fun Throwable.userText(): String = when (this) {
    is ApiException -> userText
    is UnknownHostException, is SSLException, is SocketTimeoutException,
    is ConnectException, is NoRouteToHostException, is SocketException,
    is IOException -> networkDetail(this)
    else -> message?.takeIf { it.isNotBlank() } ?: "操作失败，请重试"
}

internal fun conditionText(condition: ApiCondition): String = when (condition) {
    ApiCondition.UNAUTHORIZED -> "API Key 无效或已被删除，请重新填写"
    ApiCondition.INSUFFICIENT_BALANCE -> "账户余额不足，请前往 DeepSeek 开放平台充值后再试"
    ApiCondition.RATE_LIMITED -> "请求过于频繁，请稍后再试"
    ApiCondition.SERVER_BUSY -> "服务器繁忙，请稍后再试"
    ApiCondition.TOO_LARGE -> "文件过大，超出服务端限制"
    ApiCondition.NETWORK -> "网络异常，请检查你的网络状况"
    ApiCondition.OTHER -> ""
}
