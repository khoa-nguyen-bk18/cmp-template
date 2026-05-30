package com.devindie.vaulty.data.coroutines

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Runs a data-layer coroutine test with a shared virtual-time scheduler.
 *
 * Use [testDispatcherProvider] and pass [TestScope.testScheduler] into classes under test.
 */
fun runDataTest(testBody: suspend TestScope.() -> Unit): TestResult = runTest(testBody = testBody)
