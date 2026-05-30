package com.devindie.vaulty.data.vault.index

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import platform.Foundation.NSUserDefaults

/** Persists per-vault exclude rules in [NSUserDefaults]. */
class IosVaultIndexIgnorePersistenceDataSource : VaultIndexIgnorePersistenceDataSource {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val rulesState = MutableStateFlow(loadAllRules())

    override fun observeRules(storageKey: String): Flow<String> = rulesState
        .map { rules -> rules[storageKey].orEmpty() }
        .distinctUntilChanged()

    override suspend fun getRules(storageKey: String): String = rulesState.value[storageKey].orEmpty()

    override suspend fun saveRules(storageKey: String, rulesText: String) {
        defaults.setObject(rulesText, prefKey(storageKey))
        rulesState.update { current -> current + (storageKey to rulesText) }
    }

    override suspend fun clearRules(storageKey: String) {
        defaults.removeObjectForKey(prefKey(storageKey))
        rulesState.update { current -> current - storageKey }
    }

    private fun loadAllRules(): Map<String, String> {
        val dictionary = defaults.dictionaryRepresentation()
        val result = mutableMapOf<String, String>()
        for ((key, value) in dictionary) {
            val keyString = key as? String
            val rulesText = value as? String
            if (keyString != null && rulesText != null && keyString.startsWith(KEY_PREFIX)) {
                result[keyString.removePrefix(KEY_PREFIX)] = rulesText
            }
        }
        return result
    }

    private fun prefKey(storageKey: String): String = KEY_PREFIX + storageKey

    companion object {
        private const val KEY_PREFIX = "ignore_rules:"
    }
}
