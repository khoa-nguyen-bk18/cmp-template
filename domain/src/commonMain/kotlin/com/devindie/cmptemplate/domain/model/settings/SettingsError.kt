package com.devindie.cmptemplate.domain.model.settings

sealed class SettingsError : Throwable() {
    data object UnknownSettingKey : SettingsError()

    data object TypeMismatch : SettingsError()

    data class OutOfRange(val detail: String) : SettingsError() {
        override val message: String = detail
    }

    data class InvalidChoice(val detail: String) : SettingsError() {
        override val message: String = detail
    }

    data class TextTooLong(val maxLength: Int) : SettingsError() {
        override val message: String = "Text exceeds max length of $maxLength"
    }
}
