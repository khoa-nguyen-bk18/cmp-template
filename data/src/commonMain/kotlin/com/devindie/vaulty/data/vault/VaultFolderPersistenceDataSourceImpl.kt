package com.devindie.vaulty.data.vault

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.devindie.vaulty.domain.model.VaultFolderSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal const val VAULT_FOLDER_DATASTORE_FILE = "vault_folder.preferences_pb"

/** Persists vault selection in Preferences [DataStore] with an in-memory [Flow] mirror. */
class VaultFolderPersistenceDataSourceImpl(private val dataStore: DataStore<Preferences>) :
    VaultFolderPersistenceDataSource {
    private val selectionState = MutableStateFlow(runBlocking { loadSelection() })

    override fun observeSelection(): Flow<VaultFolderSelection?> = selectionState.asStateFlow()

    override suspend fun save(selection: VaultFolderSelection) {
        dataStore.edit { prefs ->
            prefs[KEY_STORAGE] = selection.storageKey
            prefs[KEY_DISPLAY_NAME] = selection.displayName
        }
        selectionState.value = selection
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
        selectionState.value = null
    }

    override fun currentSelection(): VaultFolderSelection? = selectionState.value

    private suspend fun loadSelection(): VaultFolderSelection? {
        val prefs = dataStore.data.first()
        val storageKey = prefs[KEY_STORAGE]
        val displayName = prefs[KEY_DISPLAY_NAME]
        return if (storageKey != null && displayName != null) {
            VaultFolderSelection(displayName = displayName, storageKey = storageKey)
        } else {
            null
        }
    }

    private companion object {
        val KEY_STORAGE = stringPreferencesKey("storage_key")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    }
}
