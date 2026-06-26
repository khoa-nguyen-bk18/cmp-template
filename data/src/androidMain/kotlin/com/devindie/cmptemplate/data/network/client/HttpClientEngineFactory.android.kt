package com.devindie.cmptemplate.data.network.client

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createPlatformHttpClientEngine(): HttpClientEngine = OkHttp.create()
