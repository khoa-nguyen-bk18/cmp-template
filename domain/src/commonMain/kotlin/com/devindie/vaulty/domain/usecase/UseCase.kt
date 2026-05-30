package com.devindie.vaulty.domain.usecase

/**
 * Base contract for domain use cases with parameters.
 */
fun interface UseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): R
}

/**
 * Base contract for parameterless domain use cases.
 */
fun interface UseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}
