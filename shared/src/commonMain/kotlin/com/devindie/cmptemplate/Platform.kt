package com.devindie.cmptemplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform