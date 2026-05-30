package com.devindie.cmptemplate.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
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
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupAndScroll() {
        baselineProfileRule.collect(
            packageName = "com.devindie.cmptemplate",
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()

            device.wait(Until.hasObject(By.text("Click me!")), 5_000)
            val feed = device.findObject(By.scrollable(true))
                ?: error("Scrollable feed not found")
            feed.setGestureMargin(device.displayWidth / 5)
            feed.fling(Direction.DOWN)
            feed.fling(Direction.DOWN)

            device.findObject(By.text("Click me!"))?.click()
            device.wait(Until.hasObject(By.textContains("Compose:")), 3_000)
        }
    }
}
