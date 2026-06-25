package com.devindie.cmptemplate.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

fun createSettingsDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        produceFile = { context.filesDir.resolve(SETTINGS_DATASTORE_FILE) },
    )
