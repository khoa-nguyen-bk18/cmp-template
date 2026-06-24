package com.devindie.cmptemplate.data.di

import android.content.Context
import com.devindie.cmptemplate.data.auth.UserRepositoryImpl
import com.devindie.cmptemplate.data.coroutines.AndroidDispatcherProvider
import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.data.local.browse.getBrowseDatabaseBuilder
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSource
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardPagerFactoryImpl
import com.devindie.cmptemplate.data.source.local.browse.BrowseDatabase
import com.devindie.cmptemplate.data.source.local.browse.CardDetailRepositoryImpl
import com.devindie.cmptemplate.data.source.local.browse.getBrowseDatabase
import com.devindie.cmptemplate.data.source.startup.AppStartupRepositoryImpl
import com.devindie.cmptemplate.domain.repository.AppStartupRepository
import com.devindie.cmptemplate.domain.repository.CardDetailRepository
import com.devindie.cmptemplate.domain.repository.UserRepository
import eu.anifantakis.lib.ksafe.KSafe
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDataModule(): Module = module {
    single<DispatcherProvider> { AndroidDispatcherProvider() }
    single { KSafe(get<Context>()) }
    includes(networkModule())
    single {
        getBrowseDatabase(
            builder = getBrowseDatabaseBuilder(get<Context>()),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }
    single { get<BrowseDatabase>().browseCardDao() }
    single { get<BrowseDatabase>().browseRemoteKeyDao() }
    single<BrowseCardLocalDataSource> {
        BrowseCardLocalDataSourceImpl(dao = get())
    }
    single {
        BrowseCardPagerFactoryImpl(
            database = get(),
            remoteDataSource = get(),
        )
    }
    single<CardDetailRepository> {
        CardDetailRepositoryImpl(
            localDataSource = get(),
            dispatchers = get(),
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(tokenStore = get())
    }
    single<AppStartupRepository> {
        AppStartupRepositoryImpl(browseCardDao = get())
    }
}
