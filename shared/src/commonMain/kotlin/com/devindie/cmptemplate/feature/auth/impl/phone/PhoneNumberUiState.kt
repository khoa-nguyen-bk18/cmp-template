package com.devindie.cmptemplate.feature.auth.impl.phone

import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import kotlinx.collections.immutable.ImmutableList

data class PhoneNumberUiState(
    val countries: ImmutableList<CountryDialCode>,
    val selectedCountry: CountryDialCode,
    val localDigits: String = "",
    val isCountryDropdownExpanded: Boolean = false,
    val phoneError: String? = null,
    val isNextEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val sendError: String? = null,
)
