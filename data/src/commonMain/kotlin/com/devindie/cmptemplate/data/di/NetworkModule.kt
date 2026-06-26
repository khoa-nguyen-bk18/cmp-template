package com.devindie.cmptemplate.data.di

import com.devindie.cmptemplate.data.auth.KSafeTokenStore
import com.devindie.cmptemplate.data.auth.TokenRefreshDataSource
import com.devindie.cmptemplate.data.auth.TokenStore
import com.devindie.cmptemplate.data.network.NetworkConfig
import com.devindie.cmptemplate.data.network.client.HttpClientFactory
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import com.devindie.cmptemplate.data.source.remote.browse.FakeBrowseCardRemoteDataSource
import com.devindie.cmptemplate.data.source.remote.browse.KtorBrowseCardRemoteDataSource
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun networkModule(networkConfig: NetworkConfig = NetworkConfig()): Module = module {
    single { networkConfig }
    single { HttpClientFactory(networkConfig = get(), tokenStore = get()) }
    single<HttpClient>(named(NetworkQualifiers.REFRESH_HTTP_CLIENT)) {
        get<HttpClientFactory>().createRefreshClient()
    }
    single {
        TokenRefreshDataSource(
            refreshClient = get(named(NetworkQualifiers.REFRESH_HTTP_CLIENT)),
            networkConfig = get(),
        )
    }
    single<HttpClient>(named(NetworkQualifiers.AUTHENTICATED_HTTP_CLIENT)) {
        get<HttpClientFactory>().createAuthenticatedClient(tokenRefreshDataSource = get())
    }
    single<TokenStore> { KSafeTokenStore(ksafe = get()) }
    single<BrowseCardRemoteDataSource> {
        val config = get<NetworkConfig>()
        if (config.useFakeRemote) {
            FakeBrowseCardRemoteDataSource()
        } else {
            KtorBrowseCardRemoteDataSource(
                httpClient = get(named(NetworkQualifiers.AUTHENTICATED_HTTP_CLIENT)),
                networkConfig = config,
            )
        }
    }
}
