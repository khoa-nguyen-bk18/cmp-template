package com.devindie.cmptemplate.domain.model.auth

object SupportedCountries {
    private val singapore =
        CountryDialCode(
            isoCode = "SG",
            dialCode = "+65",
            displayName = "Singapore",
            flagEmoji = "🇸🇬",
        )

    private val malaysia =
        CountryDialCode(
            isoCode = "MY",
            dialCode = "+60",
            displayName = "Malaysia",
            flagEmoji = "🇲🇾",
        )

    private val thailand =
        CountryDialCode(
            isoCode = "TH",
            dialCode = "+66",
            displayName = "Thailand",
            flagEmoji = "🇹🇭",
        )

    private val indonesia =
        CountryDialCode(
            isoCode = "ID",
            dialCode = "+62",
            displayName = "Indonesia",
            flagEmoji = "🇮🇩",
        )

    private val philippines =
        CountryDialCode(
            isoCode = "PH",
            dialCode = "+63",
            displayName = "Philippines",
            flagEmoji = "🇵🇭",
        )

    private val vietnam =
        CountryDialCode(
            isoCode = "VN",
            dialCode = "+84",
            displayName = "Vietnam",
            flagEmoji = "🇻🇳",
        )

    val default: CountryDialCode = singapore

    val all: List<CountryDialCode> =
        listOf(
            singapore,
            malaysia,
            thailand,
            indonesia,
            philippines,
            vietnam,
        )
}
