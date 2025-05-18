package com.odemirel

import com.odemirel.config.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dataSource = DataSourceFactory.create(environment.config)

    val liquibase = LiquibaseRunner.run(dataSource)

    Database.connect(dataSource)

    configureSerialization()
    configureTemplating()
    configureRouting()
}

