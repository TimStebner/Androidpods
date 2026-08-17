// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChimePlayerTest {
    @Test
    fun `generateFindMyChirpPcm produces non-empty stereo PCM audio buffer`() {
        val pcm = ChimePlayer.generateFindMyChirpPcm(sampleRate = 44100)

        // 1.6s * 44100 * 2 channels
        assertTrue(pcm.isNotEmpty())
        assertEquals(141120, pcm.size)

        // Ensure non-zero amplitude signals are generated
        val maxSample = pcm.maxOf { it }
        val minSample = pcm.minOf { it }
        assertTrue(maxSample > 10000)
        assertTrue(minSample < -10000)
    }

    @Test
    fun `generateFindMyChirpPcm mutes right channel when leftGain is 1 and rightGain is 0`() {
        val pcm = ChimePlayer.generateFindMyChirpPcm(sampleRate = 44100, leftGain = 1.0, rightGain = 0.0)

        // All right channel samples (odd indices) should be 0
        for (i in 1 until pcm.size step 2) {
            assertEquals(0.toShort(), pcm[i])
        }
        // Left channel samples (even indices) should have non-zero signal
        val maxLeft = (0 until pcm.size step 2).maxOf { pcm[it] }
        assertTrue(maxLeft > 10000)
    }

    @Test
    fun `generateFindMyChirpPcm mutes left channel when leftGain is 0 and rightGain is 1`() {
        val pcm = ChimePlayer.generateFindMyChirpPcm(sampleRate = 44100, leftGain = 0.0, rightGain = 1.0)

        // All left channel samples (even indices) should be 0
        for (i in 0 until pcm.size step 2) {
            assertEquals(0.toShort(), pcm[i])
        }
        // Right channel samples (odd indices) should have non-zero signal
        val maxRight = (1 until pcm.size step 2).maxOf { pcm[it] }
        assertTrue(maxRight > 10000)
    }

    @Test
    fun `initial state of ChimePlayer is idle`() {
        val player = ChimePlayer()

        assertFalse(player.isPlaying.value)
        assertEquals(null, player.activeTarget.value)
    }

    @Test
    fun `stop resets playing state and active target`() {
        val player = ChimePlayer()

        player.stop()

        assertFalse(player.isPlaying.value)
        assertEquals(null, player.activeTarget.value)
    }
}
