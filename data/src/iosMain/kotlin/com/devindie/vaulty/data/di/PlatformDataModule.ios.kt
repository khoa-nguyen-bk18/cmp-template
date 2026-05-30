@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.devindie.vaulty.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import com.devindie.vaulty.data.appearance.AppearanceSettingsDataSource
import com.devindie.vaulty.data.appearance.AppearanceSettingsDataSourceImpl
import com.devindie.vaulty.data.appearance.AppearanceSettingsRepositoryImpl
import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.platform.IosVaultFolderPickerGateway
import com.devindie.vaulty.data.vault.IosVaultFolderScannerDataSource
import com.devindie.vaulty.data.vault.VAULT_FOLDER_DATASTORE_FILE
import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSource
import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSourceImpl
import com.devindie.vaulty.data.vault.VaultFolderRepositoryImpl
import com.devindie.vaulty.data.vault.VaultFolderScannerDataSource
import com.devindie.vaulty.data.vault.index.IosVaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.IosVaultIndexIgnorePersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultDatabase
import com.devindie.vaulty.data.vault.index.VaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.VaultIndexIgnorePersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultIndexIgnoreRepositoryImpl
import com.devindie.vaulty.data.vault.index.VaultIndexRepositoryImpl
import com.devindie.vaulty.data.vault.index.getVaultDatabase
import com.devindie.vaulty.data.vault.index.getVaultDatabaseBuilder
import com.devindie.vaulty.data.vault.sync.IosVaultBackgroundSyncSchedulerDataSource
import com.devindie.vaulty.data.vault.sync.IosVaultFolderSnapshotStore
import com.devindie.vaulty.data.vault.sync.IosVaultFolderWatcherDataSource
import com.devindie.vaulty.data.vault.sync.IosVaultSyncSettingsDataSource
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncExecutor
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncSchedulerDataSource
import com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncSchedulerRepositoryImpl
import com.devindie.vaulty.data.vault.sync.VaultFolderSnapshotStore
import com.devindie.vaulty.data.vault.sync.VaultFolderWatchRepositoryImpl
import com.devindie.vaulty.data.vault.sync.VaultFolderWatcherDataSource
import com.devindie.vaulty.data.vault.sync.VaultSyncSettingsDataSource
import com.devindie.vaulty.data.vault.sync.VaultSyncSettingsRepositoryImpl
import com.devindie.vaulty.domain.gateway.VaultFolderPickerGateway
import com.devindie.vaulty.domain.repository.AppearanceSettingsRepository
import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import com.devindie.vaulty.domain.repository.VaultFolderWatchRepository
import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** iOS bindings: bookmark persistence, picker gateway, security-scoped file access, Room. */
actual fun platformDataModule(): Module = module {
    includes(dispatcherModule)
    single<DataStore<Preferences>> {
        DataStoreFactory.create(
            storage =
            OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = {
                    val documentDirectory =
                        NSFileManager.defaultManager.URLForDirectory(
                            directory = NSDocumentDirectory,
                            inDomain = NSUserDomainMask,
                            appropriateForURL = null,
                            create = false,
                            error = null,
                        )
                    (requireNotNull(documentDirectory).path + "/$VAULT_FOLDER_DATASTORE_FILE")
                        .toPath()
                },
            ),
        )
    }
    single<VaultFolderPersistenceDataSource> {
        VaultFolderPersistenceDataSourceImpl(get())
    }
    single<VaultFolderScannerDataSource> { IosVaultFolderScannerDataSource(get()) }
    single<VaultFolderRepository> { VaultFolderRepositoryImpl(get(), get()) }
    single<VaultFolderPickerGateway> { IosVaultFolderPickerGateway() }
    single<VaultFileContentDataSource> { IosVaultFileContentDataSource(get()) }
    single<VaultDatabase> {
        getVaultDatabase(
            builder = getVaultDatabaseBuilder(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }
    single<VaultIndexIgnorePersistenceDataSource> { IosVaultIndexIgnorePersistenceDataSource() }
    single<VaultIndexIgnoreRepository> { VaultIndexIgnoreRepositoryImpl(get()) }
    single<VaultIndexRepository> {
        VaultIndexRepositoryImpl(get(), get(), get(), get(), get())
    }
    single<VaultSyncSettingsDataSource> { IosVaultSyncSettingsDataSource() }
    single<VaultSyncSettingsRepository> { VaultSyncSettingsRepositoryImpl(get()) }
    single<AppearanceSettingsDataSource> { AppearanceSettingsDataSourceImpl(get()) }
    single<AppearanceSettingsRepository> { AppearanceSettingsRepositoryImpl(get()) }
    single<VaultFolderWatcherDataSource> {
        IosVaultFolderWatcherDataSource(get(), get())
    }
    single<VaultFolderWatchRepository> { VaultFolderWatchRepositoryImpl(get()) }
    single<VaultFolderSnapshotStore> { IosVaultFolderSnapshotStore() }
    single {
        VaultBackgroundSyncExecutor(get(), get(), get(), get(), get())
    }
    single<VaultBackgroundSyncSchedulerDataSource> {
        IosVaultBackgroundSyncSchedulerDataSource()
    }
    single<VaultBackgroundSyncSchedulerRepository> {
        VaultBackgroundSyncSchedulerRepositoryImpl(get())
    }
}
