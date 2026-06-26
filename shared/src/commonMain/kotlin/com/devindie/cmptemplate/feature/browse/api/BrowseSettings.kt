package com.devindie.cmptemplate.feature.browse.api

import com.devindie.cmptemplate.domain.model.settings.BooleanSettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingDefinition
import com.devindie.cmptemplate.domain.model.settings.SettingKey

object BrowseSettings {
    val ShowPrices = SettingKey("browse.show_prices")

    fun definitions(): List<SettingDefinition> =
        listOf(
            BooleanSettingDefinition(
                key = ShowPrices,
                title = "Show prices",
                description = "Display card prices in browse lists",
                default = true,
            ),
        )
}
