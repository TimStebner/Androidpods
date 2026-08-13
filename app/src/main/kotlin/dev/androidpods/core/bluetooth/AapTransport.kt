// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.hiddenapibypass.HiddenApiBypass

// Tier B transport (PROJECT.md §13.5): classic BR/EDR L2CAP to the AAP PSM. Mechanism proven on
// real hardware (Pixel 9 Pro XL, Android 17, AirPods 4) in L2capTierBProbeTest -- this is that
// probe's connection logic promoted to production, socket-lifecycle-only (§11), no packet parsing.
//
// createInsecureL2capSocket(int) is a denied hidden API (Android's public
// createL2capChannel/createInsecureL2capChannel are LE Connection-oriented-Channel only and
// cannot reach a classic BR/EDR accessory channel -- see plan file M2 section).
// HiddenApiBypass.invoke exempts this app's own reflection calls from non-SDK-interface
// enforcement via JNI into libart.so: not root, not a stack modification (§8.4).
//
// Not covered by a JVM unit test: BluetoothDevice/BluetoothSocket cannot be constructed outside
// a real Android Bluetooth stack, and none of the other framework-touching classes in this
// package (AirPodsAssociationManager, BluetoothAvailability) have one either. Handshake
// sequencing and packet decoding -- the actual logic -- are unit-tested in AapSessionTest and
// AapPacketDecoderTest against a fake transport. This class is unverified until run on the
// reference Pixel + AirPods 4 hardware.
class AapTransport(private val device: BluetoothDevice) : AirPodsTransport {
    private val _state = MutableStateFlow<AirPodsTransport.ConnectionState>(
        AirPodsTransport.ConnectionState.Disconnected,
    )
    override val state = _state.asStateFlow()

    private val _packets = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)
    override val packets: Flow<ByteArray> = _packets

    private var socket: BluetoothSocket? = null
    private var readScope: CoroutineScope? = null

    override suspend fun connect() {
        _state.value = AirPodsTransport.ConnectionState.Connecting
        try {
            val connected = withContext(Dispatchers.IO) {
                HiddenApiBypass.addHiddenApiExemptions("Landroid/bluetooth/")
                @Suppress("UNCHECKED_CAST")
                val newSocket = HiddenApiBypass.invoke(
                    BluetoothDevice::class.java,
                    device,
                    "createInsecureL2capSocket",
                    AAP_PSM,
                ) as BluetoothSocket
                newSocket.connect()
                newSocket
            }
            socket = connected
            _state.value = AirPodsTransport.ConnectionState.Connected
            startReadLoop(connected)
        } catch (e: IOException) {
            _state.value = AirPodsTransport.ConnectionState.Failed(e.message ?: "connect failed")
        }
    }

    override suspend fun disconnect() {
        readScope?.cancel()
        readScope = null
        runCatching { socket?.close() }
        socket = null
        _state.value = AirPodsTransport.ConnectionState.Disconnected
    }

    override suspend fun send(packet: ByteArray) {
        val activeSocket = checkNotNull(socket) { "send() called while disconnected" }
        withContext(Dispatchers.IO) {
            activeSocket.outputStream.write(packet)
            activeSocket.outputStream.flush()
        }
    }

    private fun startReadLoop(activeSocket: BluetoothSocket) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        readScope = scope
        scope.launch {
            val buffer = ByteArray(1024)
            val input = activeSocket.inputStream
            // One read == one AAP notification on this hardware -- captured fixture packet sizes
            // (e.g. 22-byte battery, 8-byte ear-detection) match their opcodes' expected lengths
            // exactly, with no evidence of coalesced reads (session-start-capture.txt). Revisit if
            // AapPacketDecoder ever sees an Unrecognized packet whose length looks like two
            // concatenated known packets.
            try {
                while (isActive) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    ProtocolLogging.rawPacket(TAG) { "read $read bytes" }
                    _packets.emit(buffer.copyOf(read))
                }
            } catch (e: IOException) {
                ProtocolLogging.rawPacket(TAG) { "read loop ended: ${e.message}" }
            }
            _state.value = AirPodsTransport.ConnectionState.Disconnected
        }
    }

    private companion object {
        const val TAG = "AapTransport"
        const val AAP_PSM = 0x1001
    }
}
