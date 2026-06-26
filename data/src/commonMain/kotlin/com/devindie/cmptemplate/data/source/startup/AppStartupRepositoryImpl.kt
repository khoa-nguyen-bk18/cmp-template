package com.devindie.cmptemplate.data.source.startup

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardDao
import com.devindie.cmptemplate.domain.repository.AppStartupRepository

class AppStartupRepositoryImpl(private val browseCardDao: BrowseCardDao) : AppStartupRepository {
    override suspend fun ensureReady(): Result<Unit> = runCatching { browseCardDao.count() }
}
