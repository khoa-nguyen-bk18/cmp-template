package com.devindie.vaulty.data.di

import org.koin.core.module.Module

/**
 * Platform Koin module binding DataSources, Room, and repository implementations.
 *
 * **Wired from:** `androidApp` / iOS `doInitKoin` only — not from `:shared` commonMain.
 */
expect fun platformDataModule(): Module
