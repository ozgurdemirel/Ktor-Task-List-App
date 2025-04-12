package com.odemirel

import com.odemirel.config.configureDatabase
import com.odemirel.config.configureRouting
import com.odemirel.config.configureSerialization
import com.odemirel.config.configureTemplating
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureTemplating()
    configureRouting()
}
