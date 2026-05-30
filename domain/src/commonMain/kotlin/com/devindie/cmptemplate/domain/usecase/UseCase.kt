package com.devindie.cmptemplate.domain.usecase

interface UseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): R
}

interface UseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}
