package com.devindie.vaulty.data.vault.index

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Persists per-vault exclude rules in app-private SharedPreferences. */
class AndroidVaultIndexIgnorePersistenceDataSource(context: Context) : VaultIndexIgnorePersistenceDataSource {
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val rulesState = MutableStateFlow(loadAllRules())

    override fun observeRules(storageKey: String): Flow<String> = rulesState
        .map { rules -> rules[storageKey].orEmpty() }
        .distinctUntilChanged()

    override suspend fun getRules(storageKey: String): String = rulesState.value[storageKey].orEmpty()

    override suspend fun saveRules(storageKey: String, rulesText: String) {
        prefs.edit().putString(prefKey(storageKey), rulesText).apply()
        rulesState.update { current -> current + (storageKey to rulesText) }
    }

    override suspend fun clearRules(storageKey: String) {
        prefs.edit().remove(prefKey(storageKey)).apply()
        rulesState.update { current -> current - storageKey }
    }

    private fun loadAllRules(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((key, value) in prefs.all) {
            if (key is String && key.startsWith(KEY_PREFIX) && value is String) {
                val storageKey = key.removePrefix(KEY_PREFIX)
                result[storageKey] = value
            }
        }
        return result
    }

    private fun prefKey(storageKey: String): String = KEY_PREFIX + storageKey

    companion object {
        private const val PREFS_NAME = "vault_index_ignore"
        private const val KEY_PREFIX = "ignore_rules:"
    }
}
