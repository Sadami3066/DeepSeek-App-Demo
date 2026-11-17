package com.opends.chat.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SiteIconsTest {

    @Test
    fun derivesMainDomainIconFromUrl() {
        assertEquals(
            "https://cdn.deepseek.com/site-icons/xiaoduoai.com",
            SiteIcons.derive("https://insight.xiaoduoai.com/latest-news/x.html#1"),
        )
        assertEquals(
            "https://cdn.deepseek.com/site-icons/aliyun.com",
            SiteIcons.derive("https://developer.aliyun.com/article/1783452#1"),
        )
        assertEquals(
            "https://cdn.deepseek.com/site-icons/example.com",
            SiteIcons.derive("https://www.example.com/a/b"),
        )
    }

    @Test
    fun handlesMultiPartSuffixes() {
        assertEquals(
            "https://cdn.deepseek.com/site-icons/bbc.co.uk",
            SiteIcons.derive("https://news.bbc.co.uk/zhongwen/simp"),
        )
        assertEquals(
            "https://cdn.deepseek.com/site-icons/people.com.cn",
            SiteIcons.derive("http://www.people.com.cn/n1/2026.html"),
        )
    }

    @Test
    fun keepsExistingSiteIconAndRejectsInvalidUrls() {
        assertEquals("https://cdn.deepseek.com/site-icons/custom.com", SiteIcons.favicon("https://a.com", "https://cdn.deepseek.com/site-icons/custom.com"))
        assertNull(SiteIcons.favicon("not a url", null))
        assertNull(SiteIcons.favicon(null, null))
        assertNull(SiteIcons.derive("ftp://example.com/file"))
        assertNull(SiteIcons.derive("https://localhost/abc"))
    }
}
