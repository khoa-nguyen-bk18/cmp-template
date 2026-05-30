package com.devindie.cmptemplate

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
