// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import kotlin.concurrent.thread
import org.junit.Test
import org.junit.runner.RunWith
import org.lsposed.hiddenapibypass.HiddenApiBypass

private const val TAG = "L2capTierBProbe"
private const val AAP_PSM = 0x1001
private const val CAPTURE_WINDOW_MS = 15_000L

// AAP session-start packets: byte layout adapted from LibrePods (GPL-3.0-or-later), file
// AACPManager.kt (createHandshakePacket / createSetFeatureFlagsPacket /
// createRequestNotificationPacket) and the send sequence in AirPodsService.connectToSocket(),
// commit 790e3963451002a3aabf8dcd71d40c635724176a:
// https://github.com/kavishdevar/librepods/blob/790e3963451002a3aabf8dcd71d40c635724176a/android/app/src/main/java/me/kavishdevar/librepods/bluetooth/AACPManager.kt
// The handshake packet is sent as-is (it carries its own 4-byte pseudo-header); the other two
// are the shared HEADER_BYTES (04 00 04 00) + 2-byte opcode + payload. This is a session-start
// handshake, not a configuration write -- M2's "no write commands" rule is about §8.1 controls.
private val HANDSHAKE_PACKET = byteArrayOf(
    0x00, 0x00, 0x04, 0x00, 0x01, 0x00, 0x02, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
)
private val SET_FEATURE_FLAGS_PACKET = byteArrayOf(
    0x04, 0x00, 0x04, 0x00, 0x4D, 0x00,
    0xD7.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
)
private val REQUEST_NOTIFICATIONS_PACKET = byteArrayOf(
    0x04, 0x00, 0x04, 0x00, 0x0F, 0x00,
    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
)

// One-shot reachability probe for PROJECT.md §13.5 Tier B. Result (real Pixel 9 Pro XL,
// Android 17): both public createL2capChannel/createInsecureL2capChannel FAIL for PSM 0x1001
// and a control PSM 0x1003, with native log "GAP_ConnOpen: Failure registering PSM 0x...,
// is_le: true" -- this public API is LE L2CAP CoC only and cannot reach classic BR/EDR AAP.
// See plan file M2 section for the full writeup. Not a regression test -- reads Logcat for the
// answer. Run manually: build+install both APKs, `pm grant`, then
// `adb shell am instrument -w -e class dev.androidpods.core.bluetooth.L2capTierBProbeTest
// dev.androidpods.app.test/androidx.test.runner.AndroidJUnitRunner`
// (connectedDebugAndroidTest uninstalls the app afterward, wiping the grant).
@RunWith(AndroidJUnit4::class)
class L2capTierBProbeTest {
    @Test
    fun probeAapChannel() {
        val adapter = checkNotNull(BluetoothAdapter.getDefaultAdapter())
        val device = adapter.bondedDevices.firstOrNull { it.name?.startsWith("AirPods") == true }
        checkNotNull(device) { "No bonded device named AirPods*" }

        attempt("secure", AAP_PSM) { device.createL2capChannel(AAP_PSM) }
        attempt("insecure", AAP_PSM) { device.createInsecureL2capChannel(AAP_PSM) }
        attempt("insecure-other-psm", 0x1003) { device.createInsecureL2capChannel(0x1003) }
    }

    // Non-SDK createInsecureL2capSocket(int) (note "Socket", not "Channel" -- a different,
    // older hidden method than the two public ones above) implements classic BR/EDR L2CAP per
    // CAPod's research, unlike the LE-CoC-only public API. No root, no system modification --
    // HiddenApiBypass exempts this app's own reflection calls from Android's non-SDK-interface
    // enforcement via a JNI call into libart.so. See plan file M2 section.
    @Test
    fun probeHiddenL2capSocket() {
        Log.i(TAG, "HIDDEN: before addHiddenApiExemptions")
        HiddenApiBypass.addHiddenApiExemptions("Landroid/bluetooth/")
        Log.i(TAG, "HIDDEN: after addHiddenApiExemptions")

        val adapter = checkNotNull(BluetoothAdapter.getDefaultAdapter())
        val device = adapter.bondedDevices.firstOrNull { it.name?.startsWith("AirPods") == true }
        checkNotNull(device) { "No bonded device named AirPods*" }
        Log.i(TAG, "HIDDEN: got device $device")

        attempt("hidden-socket", AAP_PSM) {
            Log.i(TAG, "HIDDEN: before HiddenApiBypass.invoke")
            val result = HiddenApiBypass.invoke(
                BluetoothDevice::class.java,
                device,
                "createInsecureL2capSocket",
                AAP_PSM,
            )
            Log.i(TAG, "HIDDEN: after HiddenApiBypass.invoke, result=$result")
            result as BluetoothSocket
        }
    }

    // Sends the AAP handshake and hex-logs every inbound packet for CAPTURE_WINDOW_MS -- the
    // fixture source for M2's decoder (plan file: "Zuerst echte Bytes aufzeichnen, dann
    // parsen"). Run manually like probeHiddenL2capSocket above, then pull the CAPTURE lines out
    // of logcat, e.g.:
    // adb logcat -d -s L2capTierBProbe | grep 'CAPTURE: PACKET'
    @Test
    fun captureAapFixture() {
        HiddenApiBypass.addHiddenApiExemptions("Landroid/bluetooth/")
        val adapter = checkNotNull(BluetoothAdapter.getDefaultAdapter())
        val device = adapter.bondedDevices.firstOrNull { it.name?.startsWith("AirPods") == true }
        checkNotNull(device) { "No bonded device named AirPods*" }

        val socket = HiddenApiBypass.invoke(
            BluetoothDevice::class.java,
            device,
            "createInsecureL2capSocket",
            AAP_PSM,
        ) as BluetoothSocket
        socket.connect()
        Log.i(TAG, "CAPTURE: connected isConnected=${socket.isConnected}")

        // BluetoothSocket has no read timeout; closing from another thread is what unblocks a
        // blocked inputStream.read() once the capture window is over.
        thread(name = "capture-watchdog") {
            Thread.sleep(CAPTURE_WINDOW_MS)
            runCatching { socket.close() }
        }

        val output = socket.outputStream
        output.write(HANDSHAKE_PACKET)
        output.flush()
        Log.i(TAG, "CAPTURE: sent HANDSHAKE")
        Thread.sleep(200)
        output.write(SET_FEATURE_FLAGS_PACKET)
        output.flush()
        Log.i(TAG, "CAPTURE: sent SET_FEATURE_FLAGS")
        Thread.sleep(200)
        output.write(REQUEST_NOTIFICATIONS_PACKET)
        output.flush()
        Log.i(TAG, "CAPTURE: sent REQUEST_NOTIFICATIONS")

        val input = socket.inputStream
        val buffer = ByteArray(1024)
        var packetIndex = 0
        try {
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                val hex = buffer.copyOf(read).joinToString(" ") { "%02x".format(it) }
                Log.i(TAG, "CAPTURE: PACKET[${packetIndex++}] ($read bytes): $hex")
            }
        } catch (e: IOException) {
            Log.i(TAG, "CAPTURE: read loop ended (${e.javaClass.simpleName}: ${e.message})")
        }
        Log.i(TAG, "CAPTURE: done, $packetIndex packet(s) captured")
    }

    // Retries: a PAGE_TIMEOUT (radio-level, seen when the AirPods' classic ACL was idle) is a
    // transient condition, not a structural API rejection -- see plan file M2 section. One-shot
    // would misreport "unsupported" for a capable device that just wasn't actively paged yet.
    private fun attempt(label: String, psm: Int, open: () -> BluetoothSocket) {
        for (retry in 1..3) {
            Log.i(TAG, "ATTEMPT[$label]: PSM 0x${psm.toString(16)} (try $retry/3)")
            var socket: BluetoothSocket? = null
            try {
                socket = open()
                socket.connect()
                Log.i(TAG, "ATTEMPT[$label]: CONNECTED isConnected=${socket.isConnected}")

                val input = socket.inputStream
                val buffer = ByteArray(256)
                val read = input.read(buffer)
                if (read > 0) {
                    val hex = buffer.copyOf(read).joinToString(" ") { "%02x".format(it) }
                    Log.i(TAG, "ATTEMPT[$label]: FIRST_BYTES ($read): $hex")
                } else {
                    Log.i(TAG, "ATTEMPT[$label]: FIRST_BYTES none (read=$read)")
                }
                return
            } catch (e: Exception) {
                Log.e(TAG, "ATTEMPT[$label]: FAILED ${e.javaClass.simpleName}: ${e.message}", e)
            } finally {
                socket?.close()
            }
            if (retry < 3) Thread.sleep(2000)
        }
    }
}
