// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.benchmark

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.FileInputStream
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidpodsMacrobenchmark {
    @get:Rule val macrobenchmarkRule = MacrobenchmarkRule()
    @get:Rule val baselineProfileRule = BaselineProfileRule()

    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun coldStart() = macrobenchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        iterations = 10,
        startupMode = StartupMode.COLD,
        setupBlock = {
            grantRuntimePermissions()
            ensureCompanionAssociation()
            pressHome()
            killProcess()
        },
    ) {
        startActivityAndWait()
    }

    @Test
    fun topLevelNavigation() {
        warmUpNavigation()
        macrobenchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 20,
            startupMode = StartupMode.WARM,
            setupBlock = {
                pressHome()
                launchApp()
            },
        ) {
            navigateTopLevelDestinations()
        }
    }

    @Test
    fun navigationDoesNotGrowPssMoreThanTenPercent() {
        launchApp()
        repeat(MEMORY_WARM_UP_CYCLES) { navigateTopLevelDestinations() }
        val beforeMemory = settledMemorySnapshot()
        launchApp()
        repeat(MEMORY_MEASUREMENT_CYCLES) { navigateTopLevelDestinations() }
        val afterMemory = settledMemorySnapshot()
        Log.i(BENCHMARK_TAG, "navigationMemory before=$beforeMemory after=$afterMemory")

        assertTrue(
            "PSS grew: before=$beforeMemory after=$afterMemory",
            afterMemory.totalPssKb <= beforeMemory.totalPssKb * 1.10,
        )
    }

    @Test
    fun nonAncAirPods4FiftyHzMotionStreamStopsExplicitlyAndWithLifecycle() {
        launchApp()
        device.clickDestination("Controls")
        val isRequiredHardware = device.findObject(By.text("AirPods 4")) != null &&
            device.findObject(By.desc("Start Motion Stream"))?.isEnabled == true
        assumeTrue("Requires paired, active non-ANC AirPods 4 hardware", isRequiredHardware)

        repeat(5) { motionStreamCycle() }
        val beforePssKb = memorySnapshot().totalPssKb
        repeat(20) {
            device.clickDestination("Widgets")
            device.clickDestination("Controls")
            motionStreamCycle()
        }
        val afterPssKb = memorySnapshot().totalPssKb
        assertTrue(
            "PSS grew from ${beforePssKb}KB to ${afterPssKb}KB",
            afterPssKb <= beforePssKb * 1.10,
        )

        device.clickDestination("Start Motion Stream")
        device.pressHome()
        launchApp()
        device.clickDestination("Controls")
        assertTrue("Motion stream remained active after lifecycle stop", device.waitFor("Start Motion Stream") != null)
    }

    @Test
    fun generateBaselineProfile() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        grantRuntimePermissions()
        ensureCompanionAssociation()
        pressHome()
        startActivityAndWait()
        navigateTopLevelDestinations()
        device.clickDestination("Controls")
    }

    private fun warmUpNavigation() = repeat(5) {
        launchApp()
        navigateTopLevelDestinations()
    }

    private fun navigateTopLevelDestinations() {
        device.navigateTo("Controls", CONTROLS_SCREEN_TEXT, HOME_SCREEN_TEXT)
        device.navigateTo("Widgets", WIDGETS_SCREEN_TEXT, CONTROLS_SCREEN_TEXT)
        device.navigateTo("Settings", SETTINGS_SCREEN_TEXT, WIDGETS_SCREEN_TEXT)
        device.navigateTo("Home", HOME_SCREEN_TEXT, SETTINGS_SCREEN_TEXT)
    }

    private fun motionStreamCycle() {
        device.clickDestination("Start Motion Stream")
        assertTrue("Motion stream did not start", device.waitFor("Stop Motion Stream") != null)
        device.clickDestination("Stop Motion Stream")
        assertTrue("Motion stream did not stop", device.waitFor("Start Motion Stream") != null)
    }

    private fun launchApp() {
        grantRuntimePermissions()
        ensureCompanionAssociation()
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(PACKAGE_NAME, "$PACKAGE_NAME.MainActivity")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        InstrumentationRegistry.getInstrumentation().context.startActivity(intent)
        requireNotNull(device.waitFor("Home")) { "Androidpods did not launch" }
    }

    private fun ensureCompanionAssociation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val benchmarkPackage = instrumentation.context.packageName
        instrumentation.uiAutomation.grantRuntimePermission(
            benchmarkPackage,
            "android.permission.BLUETOOTH_CONNECT",
        )
        val airPodsAddress = instrumentation.context
            .getSystemService(BluetoothManager::class.java)
            .adapter
            .bondedDevices
            .singleOrNull { it.name?.contains("AirPods", ignoreCase = true) == true }
            ?.address
            ?: return
        require(MAC_ADDRESS.matches(airPodsAddress)) { "Invalid paired-device address" }
        val packageMarker = "mPackageName='$PACKAGE_NAME'"
        val existingAssociations = instrumentation.uiAutomation
            .executeShellCommand("cmd companiondevice list 0")
            .readText()
        if (packageMarker in existingAssociations) return

        instrumentation.uiAutomation
            .executeShellCommand("cmd companiondevice associate 0 $PACKAGE_NAME $airPodsAddress")
            .readText()
        val updatedAssociations = instrumentation.uiAutomation
            .executeShellCommand("cmd companiondevice list 0")
            .readText()
        require(packageMarker in updatedAssociations) { "Could not associate paired AirPods" }
    }

    private fun grantRuntimePermissions() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf(
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.POST_NOTIFICATIONS",
        ).forEach { permission ->
            uiAutomation.grantRuntimePermission(PACKAGE_NAME, permission)
        }
    }

    private fun UiDevice.clickDestination(description: String) {
        repeat(2) {
            val destination = requireNotNull(waitFor(description)) {
                "Missing navigation target: $description"
            }
            try {
                destination.click()
                waitForIdle()
                return
            } catch (_: StaleObjectException) {
                // A connection pop-up can replace the activity window between lookup and click.
            }
        }
        error("Navigation target stayed stale: $description")
    }

    private fun UiDevice.navigateTo(
        description: String,
        expectedScreenText: String,
        previousScreenText: String,
    ) {
        clickDestination(description)
        requireNotNull(waitForAppObject(By.text(expectedScreenText))) {
            "Destination did not appear: $description"
        }
        require(wait(Until.gone(By.text(previousScreenText).pkg(PACKAGE_NAME)), UI_TIMEOUT_MS)) {
            "Previous destination did not finish exiting: $description"
        }
    }

    private fun UiDevice.waitFor(description: String): UiObject2? =
        waitForAppObject(By.desc(description))

    private fun UiDevice.waitForAppObject(selector: BySelector): UiObject2? {
        val appSelector = selector.pkg(PACKAGE_NAME)
        var target = wait(Until.findObject(appSelector), UI_TIMEOUT_MS)
        if (target == null && findObject(By.desc(POPUP_CLOSE_DESCRIPTION).pkg(PACKAGE_NAME)) != null) {
            pressBack()
            target = wait(Until.findObject(appSelector), UI_TIMEOUT_MS)
        }
        return target
    }

    private fun memorySnapshot(): MemorySnapshot {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("dumpsys meminfo $PACKAGE_NAME")
        val output = descriptor.readText()
        return MemorySnapshot(
            totalPssKb = output.memoryValue(TOTAL_PSS, "TOTAL PSS"),
            javaHeapKb = output.memoryValue(JAVA_HEAP, "Java Heap"),
            nativeHeapKb = output.memoryValue(NATIVE_HEAP, "Native Heap"),
            codeKb = output.memoryValue(CODE, "Code"),
            graphicsKb = output.memoryValue(GRAPHICS, "Graphics"),
            privateOtherKb = output.memoryValue(PRIVATE_OTHER, "Private Other"),
            systemKb = output.memoryValue(SYSTEM, "System"),
        )
    }

    private fun settledMemorySnapshot(): MemorySnapshot {
        device.pressHome()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("am send-trim-memory $PACKAGE_NAME COMPLETE")
            .readText()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cmd activity compact full $PACKAGE_NAME")
            .readText()
        SystemClock.sleep(MEMORY_SETTLE_MS)
        return memorySnapshot()
    }

    private fun String.memoryValue(pattern: Regex, label: String): Long =
        requireNotNull(pattern.find(this)?.groupValues?.get(1)?.toLong()) {
            "$label missing from dumpsys meminfo"
        }

    private fun ParcelFileDescriptor.readText(): String =
        FileInputStream(fileDescriptor).bufferedReader().use { it.readText() }

    private companion object {
        const val PACKAGE_NAME = "dev.androidpods.app"
        const val BENCHMARK_TAG = "AndroidpodsBenchmark"
        const val UI_TIMEOUT_MS = 5_000L
        const val MEMORY_WARM_UP_CYCLES = 20
        const val MEMORY_MEASUREMENT_CYCLES = 20
        const val MEMORY_SETTLE_MS = 1_000L
        const val HOME_SCREEN_TEXT = "Androidpods"
        const val CONTROLS_SCREEN_TEXT = "Custom Gestures & Audio Tuning"
        const val WIDGETS_SCREEN_TEXT = "Home Screen Battery Glance"
        const val SETTINGS_SCREEN_TEXT = "Preferences & Diagnostics"
        const val POPUP_CLOSE_DESCRIPTION = "Close Pop-up"
        val MAC_ADDRESS = Regex("(?:[0-9A-F]{2}:){5}[0-9A-F]{2}", RegexOption.IGNORE_CASE)
        val TOTAL_PSS = Regex("TOTAL PSS:\\s+(\\d+)")
        val JAVA_HEAP = Regex("(?m)^\\s*Java Heap:\\s+(\\d+)")
        val NATIVE_HEAP = Regex("(?m)^\\s*Native Heap:\\s+(\\d+)")
        val CODE = Regex("(?m)^\\s*Code:\\s+(\\d+)")
        val GRAPHICS = Regex("(?m)^\\s*Graphics:\\s+(\\d+)")
        val PRIVATE_OTHER = Regex("(?m)^\\s*Private Other:\\s+(\\d+)")
        val SYSTEM = Regex("(?m)^\\s*System:\\s+(\\d+)")
    }
}

private data class MemorySnapshot(
    val totalPssKb: Long,
    val javaHeapKb: Long,
    val nativeHeapKb: Long,
    val codeKb: Long,
    val graphicsKb: Long,
    val privateOtherKb: Long,
    val systemKb: Long,
)
