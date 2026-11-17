package com.opends.chat.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceProfilesTest {

    @Test
    fun mapsFourVoiceIdsToApiVoicesAndPlaybackParams() {
        val beike = VoiceProfiles.byId("mira")
        assertEquals("stella", beike.apiVoiceId)
        assertEquals(1.06f, beike.pitch, 0.0001f)
        assertEquals(0.97f, beike.speed, 0.0001f)

        val bailang = VoiceProfiles.byId("echo")
        assertEquals("tide", bailang.apiVoiceId)
        assertEquals(1.10f, bailang.pitch, 0.0001f)
        assertEquals(1.02f, bailang.speed, 0.0001f)

        val haixing = VoiceProfiles.byId("stella")
        assertEquals("stella", haixing.apiVoiceId)
        assertEquals(1.0f, haixing.pitch, 0.0001f)
        assertEquals(1.0f, haixing.speed, 0.0001f)

        val anchao = VoiceProfiles.byId("tide")
        assertEquals("tide", anchao.apiVoiceId)
        assertEquals(1.0f, anchao.pitch, 0.0001f)
        assertEquals(1.0f, anchao.speed, 0.0001f)
    }

    @Test
    fun supportsLegacyIdsAndDisplayIds() {
        assertEquals(VoiceProfiles.beike, VoiceProfiles.byId("beike"))
        assertEquals(VoiceProfiles.bailang, VoiceProfiles.byId("bailang"))
        assertEquals(VoiceProfiles.haixing, VoiceProfiles.byId("haixing"))
        assertEquals(VoiceProfiles.anchao, VoiceProfiles.byId("anchao"))
        assertEquals(VoiceProfiles.haixing, VoiceProfiles.byId(null))
        assertEquals(VoiceProfiles.haixing, VoiceProfiles.byId("unknown"))
    }

    @Test
    fun clampsPlaybackParamsToSafeRanges() {
        assertEquals(1.10f, VoiceProfiles.safePitch(2.0f), 0.0001f)
        assertEquals(0.90f, VoiceProfiles.safePitch(0.2f), 0.0001f)
        assertEquals(1.05f, VoiceProfiles.safeSpeed(3.0f), 0.0001f)
        assertEquals(0.95f, VoiceProfiles.safeSpeed(0.1f), 0.0001f)
    }

    @Test
    fun onlyVariantVoicesNeedPlaybackProcessing() {
        assertFalse(VoiceProfiles.needsProcessing(VoiceProfiles.haixing))
        assertFalse(VoiceProfiles.needsProcessing(VoiceProfiles.anchao))
        assertTrue(VoiceProfiles.needsProcessing(VoiceProfiles.beike))
        assertTrue(VoiceProfiles.needsProcessing(VoiceProfiles.bailang))
    }
}
