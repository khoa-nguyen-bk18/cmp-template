package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.repository.AuthRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class GetSupportedCountriesUseCase(private val repository: AuthRepository) : UseCaseNoParams<List<CountryDialCode>> {
    override suspend fun invoke(): List<CountryDialCode> = repository.getSupportedCountries()
}
