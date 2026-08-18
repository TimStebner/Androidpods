// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// Per-device feature flags (PROJECT.md §9): UI renders capabilities, it does not infer them.
// Unknown model number = AirPodsCapabilities.UNKNOWN = nothing supported.
data class AirPodsCapabilities(
    val modelName: String,
    val supportsEarDetection: Boolean,
    val supportsPressSpeed: Boolean = false,
    val supportsHeadGestures: Boolean = false,
    val supportsEarbudChime: Boolean = false,
) {
    companion object {
        val UNKNOWN = AirPodsCapabilities(
            modelName = "AirPods",
            supportsEarDetection = false,
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
            supportsEarDetection = true,
            supportsPressSpeed = true,
            supportsHeadGestures = true,
            supportsEarbudChime = true,
        )
        in ANC_AIRPODS_4 -> AirPodsCapabilities(
            modelName = "AirPods 4 with ANC",
            supportsEarDetection = true,
        )
        in AIRPODS_PRO_1 -> AirPodsCapabilities(
            modelName = "AirPods Pro (1st gen)",
            supportsEarDetection = true,
        )
        in AIRPODS_PRO_2 -> AirPodsCapabilities(
            modelName = "AirPods Pro (2nd gen)",
            supportsEarDetection = true,
        )
        in AIRPODS_MAX -> AirPodsCapabilities(
            modelName = "AirPods Max",
            supportsEarDetection = true,
        )
        in AIRPODS_3 -> AirPodsCapabilities(
            modelName = "AirPods (3rd gen)",
            supportsEarDetection = true,
        )
        in AIRPODS_2 -> AirPodsCapabilities(
            modelName = "AirPods (2nd gen)",
            supportsEarDetection = true,
        )
        in AIRPODS_1 -> AirPodsCapabilities(
            modelName = "AirPods (1st gen)",
            supportsEarDetection = true,
        )
        else -> AirPodsCapabilities.UNKNOWN
    }
}
