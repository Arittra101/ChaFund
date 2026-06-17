package com.example.chafund.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatcherProvider(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher    = testDispatcher
    override val io: CoroutineDispatcher      = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}
