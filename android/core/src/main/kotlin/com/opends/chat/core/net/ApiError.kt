package com.opends.chat.core.net

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
) : Exception(message) {
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

    companion object {
        fun network(cause: Throwable): ApiException =
            ApiException(ApiErrorKind.NETWORK, cause.message ?: "网络异常，请检查你的网络状况")

        fun protocol(message: String): ApiException =
            ApiException(ApiErrorKind.PROTOCOL, message)

        fun interrupted(message: String = "生成已中断"): ApiException =
            ApiException(ApiErrorKind.INTERRUPTED, message)

        fun stream(message: String): ApiException =
            ApiException(ApiErrorKind.STREAM, message)

        fun upload(message: String, retryable: Boolean): ApiException =
            ApiException(ApiErrorKind.UPLOAD, message, retryable = retryable)
    }
}

internal fun conditionText(condition: ApiCondition): String = when (condition) {
    ApiCondition.UNAUTHORIZED -> "API Key 无效或已被删除，请更换后重试"
    ApiCondition.INSUFFICIENT_BALANCE -> "账户余额不足，请充值后重试"
    ApiCondition.RATE_LIMITED -> "请求过于频繁，请稍后再试"
    ApiCondition.SERVER_BUSY -> "服务器繁忙，请稍后再试"
    ApiCondition.TOO_LARGE -> "文件过大，超出服务端限制"
    ApiCondition.NETWORK -> "网络异常，请检查你的网络状况"
    ApiCondition.OTHER -> ""
}
