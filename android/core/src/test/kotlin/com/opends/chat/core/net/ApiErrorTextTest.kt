package com.opends.chat.core.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiErrorTextTest {

    @Test
    fun unauthorizedAndBalanceHaveDetailedText() {
        val auth = ApiException.http(
            401,
            "Unauthorized",
            "{\"error\":{\"message\":\"Authentication Fails, Your api key: ****test is invalid\"}}",
        )
        assertEquals("API Key 无效或已被删除，请重新填写", auth.userText)
        val balance = ApiException.http(
            402,
            "Payment Required",
            "{\"error\":{\"message\":\"Insufficient Balance\"}}",
        )
        assertTrue(balance.userText.contains("余额不足"))
    }

    @Test
    fun networkCausesAreClassified() {
        assertTrue(ApiException.network(UnknownHostException("api.deepseek.com")).userText.contains("域名"))
        assertTrue(ApiException.network(SocketTimeoutException("timeout")).userText.contains("超时"))
    }

    @Test
    fun throttlingAndServerErrorsAreMapped() {
        assertTrue(ApiException.http(429, "Too Many Requests", "").userText.contains("频繁"))
        assertTrue(ApiException.http(503, "Service Unavailable", "").userText.contains("繁忙"))
    }
}
