// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// Press speed calibration (AAP Opcode 0x09, Config 0x25).
enum class PressSpeed(val rawValue: Byte) {
    DEFAULT(0x01),
    SLOW(0x02),
    SLOWEST(0x03);

    companion object {
        fun fromRaw(value: Byte): PressSpeed = when (value) {
            0x02.toByte() -> SLOW
            0x03.toByte() -> SLOWEST
            else -> DEFAULT
        }
    }
}

// Press-and-hold duration calibration (AAP Opcode 0x09, Config 0x26).
enum class HoldDuration(val rawValue: Byte) {
    DEFAULT(0x01),
    SHORT(0x02),
    LONG(0x03);

    companion object {
        fun fromRaw(value: Byte): HoldDuration = when (value) {
            0x02.toByte() -> SHORT
            0x03.toByte() -> LONG
            else -> DEFAULT
        }
    }
}

// Head gestures for Siri & calls (AAP Opcode 0x09, Config 0x3e).
enum class HeadGesturesState(val rawValue: Byte) {
    DISABLED(0x00),
    ENABLED(0x02);

    companion object {
        fun fromRaw(value: Byte): HeadGesturesState = when (value) {
            0x02.toByte() -> ENABLED
            else -> DISABLED
        }
    }
}
