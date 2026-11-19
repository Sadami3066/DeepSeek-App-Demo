package com.opends.chat.core.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceProfilesTest {

    @Test
    fun onlyTwoNativeVoicesRemain() {
        assertEquals(2, VoiceProfiles.all.size)
        assertEquals("stella", VoiceProfiles.haixing.apiVoiceId)
        assertEquals("tide", VoiceProfiles.anchao.apiVoiceId)
    }

    @Test
    fun mapsDisplayIdsToApiVoices() {
        assertEquals("stella", VoiceProfiles.byId("stella").apiVoiceId)
        assertEquals("tide", VoiceProfiles.byId("tide").apiVoiceId)
        assertEquals("stella", VoiceProfiles.byId("haixing").apiVoiceId)
        assertEquals("tide", VoiceProfiles.byId("anchao").apiVoiceId)
    }

    @Test
    fun retiredVoicesFallBackToTheirBaseVoice() {
        // 贝壳曾是 stella 变调，白浪曾是 tide 变调；下线后读到旧值也要能出声
        assertEquals("stella", VoiceProfiles.byId("mira").apiVoiceId)
        assertEquals("stella", VoiceProfiles.byId("beike").apiVoiceId)
        assertEquals("tide", VoiceProfiles.byId("echo").apiVoiceId)
        assertEquals("tide", VoiceProfiles.byId("bailang").apiVoiceId)
    }

    @Test
    fun unknownAndNullDefaultToStella() {
        assertEquals("stella", VoiceProfiles.byId(null).apiVoiceId)
        assertEquals("stella", VoiceProfiles.byId("unknown").apiVoiceId)
        assertEquals("stella", VoiceProfiles.byId("  ").apiVoiceId)
    }
}
