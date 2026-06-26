package com.devindie.cmptemplate.data.network

data class NetworkConfig(val baseUrl: String = DEFAULT_BASE_URL, val useFakeRemote: Boolean = true) {
    init {
        require(baseUrl.isNotBlank()) { "baseUrl must not be blank" }
    }

    companion object {
        const val DEFAULT_BASE_URL: String = "https://api.example.com"
    }
}
