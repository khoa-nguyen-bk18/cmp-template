package com.devindie.vaulty.domain.usecase.vault

import com.devindie.vaulty.domain.gateway.VaultFolderPickerGateway
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.VaultFolderSummary
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
/** Unit tests for folder pick, persist, and scan orchestration in [SelectVaultFolderUseCase]. */
class SelectVaultFolderUseCaseTest {
    @Test
    fun invoke_savesAndScansWhenFolderPicked() = runTest {
        val selection =
            VaultFolderSelection(displayName = "Downloads", storageKey = "content://tree/1")
        val summary =
            VaultFolderSummary(
                folderName = "Downloads",
                fileCount = 3,
                totalSizeBytes = 100L,
                scannedAtEpochMs = 1L,
            )
        val repository = FakeVaultFolderRepository(summary = summary)
        val useCase =
            SelectVaultFolderUseCase(
                picker = FakeVaultFolderPickerGateway(selection),
                repository = repository,
            )

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(summary, result.getOrNull())
        assertEquals(selection, repository.savedSelection)
    }

    @Test
    fun invoke_returnsFailureWhenPickerCancelled() = runTest {
        val useCase =
            SelectVaultFolderUseCase(
                picker = FakeVaultFolderPickerGateway(null),
                repository = FakeVaultFolderRepository(),
            )

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FolderPickCancelledException)
    }
}

private class FakeVaultFolderPickerGateway(private val result: VaultFolderSelection?) : VaultFolderPickerGateway {
    override suspend fun pickFolder(): VaultFolderSelection? = result
}

private class FakeVaultFolderRepository(
    private val summary: VaultFolderSummary =
        VaultFolderSummary(
            folderName = "Test",
            fileCount = 0,
            totalSizeBytes = 0L,
            scannedAtEpochMs = 0L,
        ),
) : VaultFolderRepository {
    var savedSelection: VaultFolderSelection? = null

    override fun observeSelection(): Flow<VaultFolderSelection?> = flowOf(savedSelection)

    override suspend fun saveSelection(selection: VaultFolderSelection): Result<Unit> {
        savedSelection = selection
        return Result.success(Unit)
    }

    override suspend fun clearSelection(): Result<Unit> = Result.success(Unit)

    override suspend fun scanSelectedFolder(): Result<VaultFolderSummary> = Result.success(summary)
}
