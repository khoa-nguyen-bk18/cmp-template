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
            compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        ) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Click me!")), 5_000)
            val feed = device.findObject(By.scrollable(true))
                ?: error("Scrollable feed not found")
            feed.setGestureMargin(device.displayWidth / 5)
            feed.fling(Direction.DOWN)
            feed.fling(Direction.DOWN)
        }
    }
}
