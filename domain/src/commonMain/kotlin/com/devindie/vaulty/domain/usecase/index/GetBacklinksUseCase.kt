package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Returns files that link to the note identified by [parameters] (file id).
 *
 * **Flow:** Presentation (file detail) → this →
 * [VaultIndexRepository.getBacklinks].
 *
 * @see VaultIndexRepository
 */
class GetBacklinksUseCase(private val repository: VaultIndexRepository) : UseCase<Long, Result<List<VaultFileRef>>> {
    override suspend fun invoke(parameters: Long): Result<List<VaultFileRef>> = repository.getBacklinks(parameters)
}
