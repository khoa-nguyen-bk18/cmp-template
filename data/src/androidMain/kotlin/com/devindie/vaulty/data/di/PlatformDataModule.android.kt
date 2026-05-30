package com.devindie.vaulty.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import com.devindie.vaulty.data.appearance.AppearanceSettingsDataSource
import com.devindie.vaulty.data.appearance.AppearanceSettingsDataSourceImpl
import com.devindie.vaulty.data.appearance.AppearanceSettingsRepositoryImpl
import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.AndroidVaultFolderScannerDataSource
import com.devindie.vaulty.data.vault.VAULT_FOLDER_DATASTORE_FILE
import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSource
import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSourceImpl
import com.devindie.vaulty.data.vault.VaultFolderRepositoryImpl
import com.devindie.vaulty.data.vault.VaultFolderScannerDataSource
import com.devindie.vaulty.data.vault.index.AndroidVaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.AndroidVaultIndexIgnorePersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultDatabase
import com.devindie.vaulty.data.vault.index.VaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.VaultIndexIgnorePersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultIndexIgnoreRepositoryImpl
import com.devindie.vaulty.data.vault.index.VaultIndexRepositoryImpl
import com.devindie.vaulty.data.vault.index.getVaultDatabase
import com.devindie.vaulty.data.vault.index.getVaultDatabaseBuilder
import com.devindie.vaulty.data.vault.sync.AndroidVaultBackgroundSyncSchedulerDataSource
import com.devindie.vaulty.data.vault.sync.AndroidVaultFolderSnapshotStore
import com.devindie.vaulty.data.vault.sync.AndroidVaultFolderWatcherDataSource
import com.devindie.vaulty.data.vault.sync.AndroidVaultSyncSettingsDataSource
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncExecutor
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncSchedulerDataSource
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncSchedulerRepositoryImpl
import com.devindie.vaulty.data.vault.sync.VaultFolderSnapshotStore
import com.devindie.vaulty.data.vault.sync.VaultFolderWatchRepositoryImpl
import com.devindie.vaulty.data.vault.sync.VaultFolderWatcherDataSource
import com.devindie.vaulty.data.vault.sync.VaultSyncSettingsDataSource
import com.devindie.vaulty.data.vault.sync.VaultSyncSettingsRepositoryImpl
import com.devindie.vaulty.data.vault.sync.VaultSyncWorker
import com.devindie.vaulty.domain.repository.AppearanceSettingsRepository
import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import com.devindie.vaulty.domain.repository.VaultFolderWatchRepository
import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android bindings: `Context`-backed persistence, SAF scanner, Room, repositories. */
actual fun platformDataModule(): Module = module {
    includes(dispatcherModule)
    single<DataStore<Preferences>> {
        DataStoreFactory.create(
            storage =
            FileStorage(
                serializer = PreferencesFileSerializer,
                produceFile = {
                    get<Context>().filesDir.resolve(VAULT_FOLDER_DATASTORE_FILE)
                },
            ),
        )
    }
    single<VaultFolderPersistenceDataSource> {
        VaultFolderPersistenceDataSourceImpl(get())
    }
    single<VaultFolderScannerDataSource> {
        AndroidVaultFolderScannerDataSource(get(), get())
    }
    single<VaultFolderRepository> {
        VaultFolderRepositoryImpl(get(), get())
    }
    single<VaultFileContentDataSource> {
        AndroidVaultFileContentDataSource(get(), get())
    }
    single<VaultDatabase> {
        getVaultDatabase(
            builder = getVaultDatabaseBuilder(get<Context>()),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }
    single<VaultIndexIgnorePersistenceDataSource> {
        AndroidVaultIndexIgnorePersistenceDataSource(get<Context>())
    }
    single<VaultIndexIgnoreRepository> {
        VaultIndexIgnoreRepositoryImpl(get())
    }
    single<VaultIndexRepository> {
        VaultIndexRepositoryImpl(get(), get(), get(), get(), get())
    }
    single<VaultSyncSettingsDataSource> {
        AndroidVaultSyncSettingsDataSource(get<Context>())
    }
    single<VaultSyncSettingsRepository> {
        VaultSyncSettingsRepositoryImpl(get())
    }
    single<AppearanceSettingsDataSource> {
        AppearanceSettingsDataSourceImpl(get())
    }
    single<AppearanceSettingsRepository> {
        AppearanceSettingsRepositoryImpl(get())
    }
    single<VaultFolderWatcherDataSource> {
        AndroidVaultFolderWatcherDataSource(get(), get())
    }
    single<VaultFolderWatchRepository> {
        VaultFolderWatchRepositoryImpl(get())
    }
    single<VaultFolderSnapshotStore> {
        AndroidVaultFolderSnapshotStore(get<Context>())
    }
    single {
        VaultBackgroundSyncExecutor(get(), get(), get(), get(), get())
    }
    workerOf(::VaultSyncWorker)
    single<VaultBackgroundSyncSchedulerDataSource> {
        AndroidVaultBackgroundSyncSchedulerDataSource(get<Context>())
    }
    single<VaultBackgroundSyncSchedulerRepository> {
        VaultBackgroundSyncSchedulerRepositoryImpl(get())
    }
}
