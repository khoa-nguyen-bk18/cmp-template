package com.devindie.cmptemplate.data.source.startup

import com.devindie.cmptemplate.data.local.browse.fake.FakeBrowseCardDao
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AppStartupRepositoryImplTest {
    @Test
    fun ensureReady_succeedsWhenDaoProbeSucceeds() = runTest {
        val dao = FakeBrowseCardDao()
        val repository = AppStartupRepositoryImpl(browseCardDao = dao)

        val result = repository.ensureReady()

        assertTrue(result.isSuccess)
    }

    @Test
    fun ensureReady_returnsFailureWhenDaoThrows() = runTest {
        val dao = FakeBrowseCardDao()
        dao.countThrows = IllegalStateException("Database not ready")
        val repository = AppStartupRepositoryImpl(browseCardDao = dao)

        val result = repository.ensureReady()

        assertTrue(result.isFailure)
    }
}
