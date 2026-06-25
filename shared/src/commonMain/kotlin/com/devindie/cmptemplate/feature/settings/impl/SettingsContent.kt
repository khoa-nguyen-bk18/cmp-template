package com.devindie.cmptemplate.feature.settings.impl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.domain.model.settings.SettingKey
import com.devindie.cmptemplate.domain.model.settings.SettingValue
import com.devindie.cmptemplate.domain.model.settings.SettingsItemModel
import com.devindie.cmptemplate.domain.model.settings.SettingsSectionModel
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    state: SettingsScreenUiState,
    messages: Flow<String>,
    onBack: () -> Unit,
    onSettingChanged: (SettingKey, SettingValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(messages) {
        messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            state.sections.forEach { section ->
                item(key = "header-${section.id}") {
                    SettingsSectionHeader(section = section)
                }
                items(section.items, key = { item -> item.key.value }) { item ->
                    SettingsItemRow(
                        item = item,
                        onValueChange = onSettingChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(section: SettingsSectionModel) {
    Text(
        text = section.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
