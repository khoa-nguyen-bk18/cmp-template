package com.devindie.cmptemplate.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupWithBaselineProfile() {
        benchmarkRule.measureRepeated(
            packageName = "com.devindie.cmptemplate",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun startupWithoutBaselineProfile() {
        benchmarkRule.measureRepeated(
            packageName = "com.devindie.cmptemplate",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None(),
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun feedScrollPerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.devindie.cmptemplate",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        ) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.text(DEFAULT_STORE_NAME)), 5_000)
            device.wait(Until.hasObject(By.text(FIRST_BROWSE_CARD_NAME)), 10_000)
            val browseList =
                device.findObject(
                    By.scrollable(true).hasDescendant(By.text(FIRST_BROWSE_CARD_NAME)),
                ) ?: error("Browse LazyColumn not found")
            browseList.setGestureMargin(device.displayWidth / 5)
            browseList.fling(Direction.DOWN)
            browseList.fling(Direction.DOWN)
        }
    }
}

private const val DEFAULT_STORE_NAME = "Good Games Belconnen"

/** First card from [com.devindie.cmptemplate.data.local.browse.BrowseCatalogSeeder]. */
private const val FIRST_BROWSE_CARD_NAME = "Charizard ex"
