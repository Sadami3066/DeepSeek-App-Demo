package com.opends.chat.core.util

import java.net.URI

internal object SiteIcons {
    private const val CDN_PREFIX = "https://cdn.deepseek.com/site-icons/"
    private val multiPartSuffixes = setOf(
        "com.cn", "net.cn", "org.cn", "gov.cn", "edu.cn",
        "co.uk", "org.uk", "ac.uk",
        "com.hk", "com.tw", "com.au", "co.jp", "co.kr", "com.sg", "co.in", "com.br",
    )

    fun favicon(url: String?, siteIcon: String?): String? {
        val existing = siteIcon?.trim()
        if (!existing.isNullOrEmpty()) return existing
        return derive(url)
    }

    fun derive(url: String?): String? {
        val host = hostOf(url) ?: return null
        val clean = host.removePrefix("www.").lowercase()
        val labels = clean.split('.').filter { it.isNotEmpty() }
        if (labels.size < 2) return null
        val suffix = if (labels.size >= 3 && multiPartSuffixes.contains(labels.takeLast(2).joinToString("."))) {
            labels.takeLast(3).joinToString(".")
        } else {
            labels.takeLast(2).joinToString(".")
        }
        return CDN_PREFIX + suffix
    }

    private fun hostOf(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val parsed = runCatching { URI(url.trim()) }.getOrNull() ?: return null
        val scheme = parsed.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        return parsed.host?.takeIf { it.isNotBlank() }
    }
}
