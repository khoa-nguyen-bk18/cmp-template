package com.devindie.cmptemplate.data.network.client

import io.ktor.client.engine.HttpClientEngine

internal expect fun createPlatformHttpClientEngine(): HttpClientEngine
