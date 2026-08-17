// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// PROJECT.md §18.
enum class NoiseControlMode { OFF, TRANSPARENCY, ADAPTIVE, NOISE_CANCELLATION }

// Per-device feature flags (PROJECT.md §9): UI renders capabilities, it does not infer them.
// Unknown model number = AirPodsCapabilities.UNKNOWN = nothing supported.
data class AirPodsCapabilities(
    val modelName: String,
    val supportsNoiseControl: Boolean,
    val supportedNoiseControlModes: Set<NoiseControlMode>,
    val supportsEarDetection: Boolean,
    val supportsStemConfiguration: Boolean,
    val supportsPressSpeed: Boolean,
    val supportsHeadGestures: Boolean,
    val supportsEarbudChime: Boolean = true,
    val supportsCaseSpeaker: Boolean = false,
) {
    companion object {
        val UNKNOWN = AirPodsCapabilities(
            modelName = "AirPods",
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = false,
            supportsStemConfiguration = false,
            supportsPressSpeed = false,
            supportsHeadGestures = false,
            supportsEarbudChime = false,
            supportsCaseSpeaker = false,
        )
    }
}

// Model numbers per support.apple.com/en-us/109525.
object CapabilityResolver {
    private val NON_ANC_AIRPODS_4 = setOf("A3050", "A3053", "A3054")
    private val ANC_AIRPODS_4 = setOf("A3055", "A3056")
    private val AIRPODS_PRO_1 = setOf("A2083", "A2084")
    private val AIRPODS_PRO_2 = setOf("A2931", "A2698", "A2699", "A3047", "A3048", "A3049")
    private val AIRPODS_MAX = setOf("A2096", "A3184")
    private val AIRPODS_3 = setOf("A2564", "A2565")
    private val AIRPODS_2 = setOf("A2031", "A2032")
    private val AIRPODS_1 = setOf("A1523", "A1722")

    fun resolve(modelNumber: String): AirPodsCapabilities = when (modelNumber) {
        in NON_ANC_AIRPODS_4 -> AirPodsCapabilities(
            modelName = "AirPods 4",
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = true,
            supportsStemConfiguration = false,
            supportsPressSpeed = true,
            supportsHeadGestures = true,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        in ANC_AIRPODS_4 -> AirPodsCapabilities(
            modelName = "AirPods 4 with ANC",
            supportsNoiseControl = true,
            supportedNoiseControlModes = setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.ADAPTIVE,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            supportsEarDetection = true,
            supportsStemConfiguration = true,
            supportsPressSpeed = true,
            supportsHeadGestures = true,
            supportsEarbudChime = true,
            supportsCaseSpeaker = true,
        )
        in AIRPODS_PRO_1 -> AirPodsCapabilities(
            modelName = "AirPods Pro (1st gen)",
            supportsNoiseControl = true,
            supportedNoiseControlModes = setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            supportsEarDetection = true,
            supportsStemConfiguration = true,
            supportsPressSpeed = true,
            supportsHeadGestures = false,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        in AIRPODS_PRO_2 -> AirPodsCapabilities(
            modelName = "AirPods Pro (2nd gen)",
            supportsNoiseControl = true,
            supportedNoiseControlModes = setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.ADAPTIVE,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            supportsEarDetection = true,
            supportsStemConfiguration = true,
            supportsPressSpeed = true,
            supportsHeadGestures = true,
            supportsEarbudChime = true,
            supportsCaseSpeaker = true,
        )
        in AIRPODS_MAX -> AirPodsCapabilities(
            modelName = "AirPods Max",
            supportsNoiseControl = true,
            supportedNoiseControlModes = setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            supportsEarDetection = true,
            supportsStemConfiguration = false,
            supportsPressSpeed = false,
            supportsHeadGestures = false,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        in AIRPODS_3 -> AirPodsCapabilities(
            modelName = "AirPods (3rd gen)",
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = true,
            supportsStemConfiguration = false,
            supportsPressSpeed = true,
            supportsHeadGestures = false,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        in AIRPODS_2 -> AirPodsCapabilities(
            modelName = "AirPods (2nd gen)",
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = true,
            supportsStemConfiguration = false,
            supportsPressSpeed = false,
            supportsHeadGestures = false,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        in AIRPODS_1 -> AirPodsCapabilities(
            modelName = "AirPods (1st gen)",
            supportsNoiseControl = false,
            supportedNoiseControlModes = emptySet(),
            supportsEarDetection = true,
            supportsStemConfiguration = false,
            supportsPressSpeed = false,
            supportsHeadGestures = false,
            supportsEarbudChime = true,
            supportsCaseSpeaker = false,
        )
        else -> AirPodsCapabilities.UNKNOWN
    }
}
