// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

// Field layouts (opcodes, byte offsets, NUL-delimited string framing) adapted from LibrePods
// (GPL-3.0-or-later), files Packets.kt and AACPManager.kt, commit
// 790e3963451002a3aabf8dcd71d40c635724176a:
// https://github.com/kavishdevar/librepods/blob/790e3963451002a3aabf8dcd71d40c635724176a/android/app/src/main/java/me/kavishdevar/librepods/data/Packets.kt
// https://github.com/kavishdevar/librepods/blob/790e3963451002a3aabf8dcd71d40c635724176a/android/app/src/main/java/me/kavishdevar/librepods/bluetooth/AACPManager.kt
// These are protocol facts (PROJECT.md §25), not copied code -- verified against
// app/src/test/resources/fixtures/aap/session-start-capture.txt, a real capture from this
// project's own AirPods 4.

enum class BatteryChargeStatus { CHARGING, NOT_CHARGING, DISCONNECTED, OPTIMIZED_CHARGING }

data class BatteryComponentState(val level: Int, val status: BatteryChargeStatus)

data class BatteryState(val left: BatteryComponentState, val right: BatteryComponentState, val case: BatteryComponentState)

data class EarDetectionState(val leftInEar: Boolean, val rightInEar: Boolean)

data class AirPodsInformation(
    val name: String,
    val modelNumber: String,
    val manufacturer: String,
    val serialNumber: String,
    val version1: String,
    val version2: String,
    val hardwareRevision: String,
    val updaterIdentifier: String,
    val leftSerialNumber: String,
    val rightSerialNumber: String,
    val version3: String,
)

sealed interface AapEvent {
    data class Battery(val state: BatteryState) : AapEvent
    data class EarDetection(val state: EarDetectionState) : AapEvent
    data class DeviceInfo(val info: AirPodsInformation) : AapEvent
    data class StemConfig(val isLeft: Boolean, val action: StemPressAndHoldAction) : AapEvent
    data class PressSpeedConfig(val speed: PressSpeed) : AapEvent
    data class HoldDurationConfig(val duration: HoldDuration) : AapEvent
    data class HeadGesturesConfig(val state: HeadGesturesState) : AapEvent
    data class HeadMotion(val pitch: Float, val yaw: Float, val roll: Float) : AapEvent
    data object Unrecognized : AapEvent
}

// Transport-layer-agnostic: takes the raw bytes a socket read produced, no I/O here (§11).
object AapPacketDecoder {
    private val HEADER = byteArrayOf(0x04, 0x00, 0x04, 0x00)
    private const val OPCODE_BATTERY_INFO = 0x04
    private const val OPCODE_EAR_DETECTION = 0x06
    private const val OPCODE_CONTROL = 0x09
    private const val OPCODE_HEADTRACKING = 0x17
    private const val OPCODE_INFORMATION = 0x1D
    private const val COMPONENT_LEFT = 2 // In AAP: 0x02 = Left, 0x04 = Right, 0x08 = Case

    fun decode(packet: ByteArray): AapEvent {
        if (packet.size < 6 || !packet.copyOfRange(0, 4).contentEquals(HEADER)) {
            return AapEvent.Unrecognized
        }
        return when (packet[4].toInt() and 0xFF) {
            OPCODE_BATTERY_INFO -> decodeBattery(packet)
            OPCODE_EAR_DETECTION -> decodeEarDetection(packet)
            OPCODE_INFORMATION -> decodeInformation(packet)
            OPCODE_CONTROL -> decodeControl(packet)
            OPCODE_HEADTRACKING -> decodeHeadTracking(packet)
            else -> AapEvent.Unrecognized
        }
    }

    private fun decodeHeadTracking(packet: ByteArray): AapEvent {
        // Only decode actual IMU spatial sensor reports (at least 50 bytes)
        if (packet.size < 50) return AapEvent.Unrecognized

        // Standard AAP spatial sensor packet with 16-bit orientation coordinates at offsets 43, 45, 47
        val o1Raw = (packet[43].toInt() and 0xFF) or (packet[44].toInt() shl 8)
        val o2Raw = (packet[45].toInt() and 0xFF) or (packet[46].toInt() shl 8)
        val o3Raw = (packet[47].toInt() and 0xFF) or (packet[48].toInt() shl 8)

        val o1 = o1Raw.toShort().toFloat()
        val o2 = o2Raw.toShort().toFloat()
        val o3 = o3Raw.toShort().toFloat()

        val pitch = (((o2 + o3) / 2f) / 32000f * 180f).coerceIn(-90f, 90f)
        val yaw = (((o2 - o3) / 2f) / 32000f * 180f).coerceIn(-70f, 70f)
        val roll = (o1 / 32000f * 180f).coerceIn(-180f, 180f)
        dev.androidpods.core.bluetooth.ProtocolLogging.rawPacket("AapPacketDecoder") {
            "HeadMotion: pitch=%.1f, yaw=%.1f, roll=%.1f (raw: %04x, %04x, %04x)".format(pitch, yaw, roll, o1Raw, o2Raw, o3Raw)
        }
        return AapEvent.HeadMotion(pitch = pitch, yaw = yaw, roll = roll)
    }

    private fun decodeControl(packet: ByteArray): AapEvent {
        if (packet.size < 8) return AapEvent.Unrecognized
        val configId = packet[6].toInt() and 0xFF
        val byte7 = packet[7]
        val byte8 = if (packet.size > 8) packet[8] else 0.toByte()
        val value = if (byte7 != 0.toByte()) byte7 else byte8
        return when (configId) {
            0x18 -> AapEvent.StemConfig(isLeft = true, action = StemPressAndHoldAction.fromRaw(value))
            0x17 -> AapEvent.StemConfig(isLeft = false, action = StemPressAndHoldAction.fromRaw(value))
            0x25 -> AapEvent.PressSpeedConfig(speed = PressSpeed.fromRaw(value))
            0x26 -> AapEvent.HoldDurationConfig(duration = HoldDuration.fromRaw(value))
            0x3E -> AapEvent.HeadGesturesConfig(state = HeadGesturesState.fromRaw(value))
            else -> AapEvent.Unrecognized
        }
    }

    private fun decodeBattery(packet: ByteArray): AapEvent {
        if (packet.size != 22) return AapEvent.Unrecognized
        val first = componentState(packet, componentIndex = 7, levelIndex = 9, statusIndex = 10)
        val second = componentState(packet, componentIndex = 12, levelIndex = 14, statusIndex = 15)
        val case = componentState(packet, componentIndex = 17, levelIndex = 19, statusIndex = 20)
        val (left, right) = if (first.first == COMPONENT_LEFT) first to second else second to first
        return AapEvent.Battery(BatteryState(left.second, right.second, case.second))
    }

    // Left/right are not fixed slots in the packet -- the component byte says which is which.
    private fun componentState(
        packet: ByteArray,
        componentIndex: Int,
        levelIndex: Int,
        statusIndex: Int,
    ): Pair<Int, BatteryComponentState> {
        val component = packet[componentIndex].toInt() and 0xFF
        val level = packet[levelIndex].toInt() and 0xFF
        val status = when (packet[statusIndex].toInt() and 0xFF) {
            1 -> BatteryChargeStatus.CHARGING
            2 -> BatteryChargeStatus.NOT_CHARGING
            5 -> BatteryChargeStatus.OPTIMIZED_CHARGING
            else -> BatteryChargeStatus.DISCONNECTED
        }
        return component to BatteryComponentState(level, status)
    }

    private fun decodeEarDetection(packet: ByteArray): AapEvent {
        if (packet.size != 8) return AapEvent.Unrecognized
        // Calibration pass confirmed against physical hardware (2026-08-17):
        // Byte 6 = Right Earbud (0x00 In-Ear, 0x01 Out-of-Ear)
        // Byte 7 = Left Earbud  (0x00 In-Ear, 0x01 Out-of-Ear)
        val rightIn = packet[6].toInt() and 0xFF == 0
        val leftIn = packet[7].toInt() and 0xFF == 0
        return AapEvent.EarDetection(EarDetectionState(leftInEar = leftIn, rightInEar = rightIn))
    }

    private fun decodeInformation(packet: ByteArray): AapEvent {
        val fields = splitNulDelimited(packet.copyOfRange(6, packet.size)).drop(1)
        if (fields.size < 11) return AapEvent.Unrecognized
        return AapEvent.DeviceInfo(
            AirPodsInformation(
                name = fields[0],
                modelNumber = fields[1],
                manufacturer = fields[2],
                serialNumber = fields[3],
                version1 = fields[4],
                version2 = fields[5],
                hardwareRevision = fields[6],
                updaterIdentifier = fields[7],
                leftSerialNumber = fields[8],
                rightSerialNumber = fields[9],
                version3 = fields[10],
            ),
        )
    }

    private fun splitNulDelimited(data: ByteArray): List<String> {
        var index = data.indexOfFirst { it == 0x00.toByte() }.let { if (it == -1) data.size else it }
        val strings = mutableListOf<String>()
        while (index < data.size) {
            while (index < data.size && data[index] == 0x00.toByte()) index++
            if (index >= data.size) break
            val start = index
            while (index < data.size && data[index] != 0x00.toByte()) index++
            strings.add(String(data, start, index - start, Charsets.UTF_8))
        }
        return strings
    }
}
