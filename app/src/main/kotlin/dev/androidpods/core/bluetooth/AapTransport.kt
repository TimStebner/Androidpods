// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    override val deviceAddress: String = device.address

    private val _state = MutableStateFlow<AirPodsTransport.ConnectionState>(
        AirPodsTransport.ConnectionState.Disconnected,
    )
    override val state = _state.asStateFlow()

    // The session-start burst is 28 packets (session-start-capture.txt); sized above that so
    // emit() never has to suspend the read loop waiting for AirPodsRepository's collector.
    private val _packets = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val packets: Flow<ByteArray> = _packets

    private var socket: BluetoothSocket? = null
    private var readScope: CoroutineScope? = null
    private val connectMutex = Mutex()

    private fun isDeviceAclConnected(): Boolean {
        return runCatching {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/bluetooth/")
            HiddenApiBypass.invoke(
                BluetoothDevice::class.java,
                device,
                "isConnected",
            ) as Boolean
        }.getOrDefault(true)
    }

    override suspend fun connect() {
        connectMutex.withLock {
            if (_state.value == AirPodsTransport.ConnectionState.Connected) {
                return@withLock
            }

            // If the device is not currently connected to Android Bluetooth (e.g. inside the charging case),
            // remain cleanly Disconnected without failing as a platform Tier B rejection.
            if (!isDeviceAclConnected()) {
                _state.value = AirPodsTransport.ConnectionState.Disconnected
                return@withLock
            }

            _state.value = AirPodsTransport.ConnectionState.Connecting

            var lastFailure = "connect failed"
            repeat(CONNECT_ATTEMPTS) { attempt ->
                try {
                    readScope?.cancel()
                    readScope = null
                    runCatching { socket?.close() }
                    socket = null

                    if (!isDeviceAclConnected()) {
                        _state.value = AirPodsTransport.ConnectionState.Disconnected
                        return@withLock
                    }

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
                    return@withLock
                } catch (e: IOException) {
                    lastFailure = e.message ?: lastFailure
                    ProtocolLogging.rawPacket(TAG) { "connect attempt ${attempt + 1}/$CONNECT_ATTEMPTS failed: $lastFailure" }
                    if (!isDeviceAclConnected()) {
                        _state.value = AirPodsTransport.ConnectionState.Disconnected
                        return@withLock
                    }
                    if (attempt < CONNECT_ATTEMPTS - 1) delay(CONNECT_RETRY_DELAY_MS)
                }
            }
            if (!isDeviceAclConnected()) {
                _state.value = AirPodsTransport.ConnectionState.Disconnected
            } else {
                _state.value = AirPodsTransport.ConnectionState.Failed(lastFailure)
            }
        }
    }

    override suspend fun disconnect() {
        connectMutex.withLock {
            readScope?.cancel()
            readScope = null
            runCatching { socket?.close() }
            socket = null
            _state.value = AirPodsTransport.ConnectionState.Disconnected
        }
    }

    override suspend fun send(packet: ByteArray) {
        val activeSocket = socket ?: return
        if (dev.androidpods.app.BuildConfig.DEBUG) {
            android.util.Log.d(TAG, "SEND [${packet.size}b]: ${packet.joinToString(" ") { "%02x".format(it) }}")
        }
        withContext(Dispatchers.IO) {
            runCatching {
                activeSocket.outputStream.write(packet)
                activeSocket.outputStream.flush()
            }
        }
    }

    private fun startReadLoop(activeSocket: BluetoothSocket) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        readScope = scope
        scope.launch {
            val buffer = ByteArray(1024)
            val input = activeSocket.inputStream
            try {
                while (isActive) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    val packet = buffer.copyOf(read)
                    if (dev.androidpods.app.BuildConfig.DEBUG) {
                        android.util.Log.d(TAG, "RECV [${read}b]: ${packet.joinToString(" ") { "%02x".format(it) }}")
                    }
                    ProtocolLogging.rawPacket(TAG) { "read $read bytes" }
                    _packets.emit(packet)
                }
            } catch (e: IOException) {
                ProtocolLogging.rawPacket(TAG) { "read loop ended: ${e.message}" }
            }
            if (socket === activeSocket) {
                _state.value = AirPodsTransport.ConnectionState.Disconnected
            }
        }
    }

    private companion object {
        const val TAG = "AapTransport"
        const val AAP_PSM = 0x1001
        const val CONNECT_ATTEMPTS = 3
        const val CONNECT_RETRY_DELAY_MS = 2_000L
    }
}
