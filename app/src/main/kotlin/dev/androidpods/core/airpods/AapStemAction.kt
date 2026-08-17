// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// AAP Stem / Button Actions for Press-and-Hold gestures (PROJECT.md §17, §33).
// Config 0x18 = Left Earbud Stem, Config 0x17 = Right Earbud Stem.
enum class StemPressAndHoldAction(val rawValue: Byte) {
    VOICE_ASSISTANT(0x00),
    NOISE_CONTROL(0x01),
    DISABLED(0x02);

    companion object {
        fun fromRaw(value: Byte): StemPressAndHoldAction = when (value) {
            0x00.toByte() -> VOICE_ASSISTANT
            0x01.toByte() -> NOISE_CONTROL
            else -> DISABLED
        }
    }
}
