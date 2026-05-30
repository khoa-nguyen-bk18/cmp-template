package com.devindie.cmptemplate.data.di

import android.content.Context
import com.devindie.cmptemplate.data.browse.BrowseCardLocalDataSource
import com.devindie.cmptemplate.data.browse.BrowseCardLocalDataSourceImpl
import com.devindie.cmptemplate.data.browse.BrowseCardRepositoryImpl
import com.devindie.cmptemplate.data.browse.BrowseDatabase
import com.devindie.cmptemplate.data.browse.getBrowseDatabase
import com.devindie.cmptemplate.data.browse.getBrowseDatabaseBuilder
import com.devindie.cmptemplate.data.coroutines.AndroidDispatcherProvider
import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDataModule(): Module =
    module {
        single<DispatcherProvider> { AndroidDispatcherProvider() }
        single {
            getBrowseDatabase(
                builder = getBrowseDatabaseBuilder(get<Context>()),
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }
        single { get<BrowseDatabase>().browseCardDao() }
        single<BrowseCardLocalDataSource> {
            BrowseCardLocalDataSourceImpl(dao = get())
        }
        single<BrowseCardRepository> {
            BrowseCardRepositoryImpl(
                localDataSource = get(),
                dispatchers = get(),
            )
        }
    }
