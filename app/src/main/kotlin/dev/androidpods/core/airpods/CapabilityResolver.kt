// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// PROJECT.md §18.
enum class NoiseControlMode { OFF, TRANSPARENCY, ADAPTIVE, NOISE_CANCELLATION }

// Per-device feature flags (PROJECT.md §9): UI renders capabilities, it does not infer them.
// Unknown model number = AirPodsCapabilities.UNKNOWN = nothing supported.
data class AirPodsCapabilities(
    val supportsNoiseControl: Boolean,
    val supportedNoiseControlModes: Set<NoiseControlMode>,
    val supportsEarDetection: Boolean,
) {
    companion object {
        val UNKNOWN = AirPodsCapabilities(
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = false,
        )
    }
}

// Model numbers per support.apple.com/en-us/109525. Confirmed against this project's own
// hardware: the captured INFORMATION packet (session-start-capture.txt) reports "A3050" --
// the non-ANC AirPods 4 variant -- so M3's hardware-validation target is ear-detection/stem
// actions, not noise control (plan file M2 checkpoint).
object CapabilityResolver {
    private val NON_ANC_AIRPODS_4 = setOf("A3050", "A3053", "A3054")
    private val ANC_AIRPODS_4 = setOf("A3055", "A3056")

    fun resolve(modelNumber: String): AirPodsCapabilities = when (modelNumber) {
        in NON_ANC_AIRPODS_4 -> AirPodsCapabilities(
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = true,
        )
        in ANC_AIRPODS_4 -> AirPodsCapabilities(
            supportsNoiseControl = true,
            supportedNoiseControlModes = setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.ADAPTIVE,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            supportsEarDetection = true,
        )
        else -> AirPodsCapabilities.UNKNOWN
    }
}
