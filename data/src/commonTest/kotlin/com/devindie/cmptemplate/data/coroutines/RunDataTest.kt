package com.devindie.cmptemplate.data.coroutines

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun runDataTest(testBody: suspend TestScope.(TestDispatcherProvider) -> Unit): TestResult = runTest {
    val provider = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
    testBody(provider)
}
