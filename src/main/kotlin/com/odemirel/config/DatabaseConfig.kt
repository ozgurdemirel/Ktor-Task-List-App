package com.odemirel.config

import io.ktor.server.config.*
import com.odemirel.dto.DbProperties
import com.odemirel.dto.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database

fun loadDatabaseConfig(config: ApplicationConfig): DatabaseConfig {
    val environment = config.property("database.environment").getString()
    val local = DbProperties(
        jdbcUrl = config.property("database.test.jdbcUrl").getString(),
        driver = config.property("database.test.driver").getString(),
        user = config.property("database.test.user").getString(),
        password = config.property("database.test.password").getString()
    )
    val prod = DbProperties(
        jdbcUrl = config.property("database.prod.jdbcUrl").getString(),
        driver = config.property("database.prod.driver").getString(),
        user = config.property("database.prod.user").getString(),
        password = config.property("database.prod.password").getString()
    )
    return DatabaseConfig(environment, local, prod)
}

fun Application.configureDatabase() {
    val dbConfig = loadDatabaseConfig(environment.config)
    val dataSource = createDataSource(dbConfig)
    // Run Liquibase migrations
    runLiquibaseMigrations(dataSource)
    Database.connect(dataSource)
}

fun createDataSource(dbConfig: DatabaseConfig): HikariDataSource {
    val config = HikariConfig().apply {
        if (dbConfig.environment == "prod") {
            driverClassName = dbConfig.prod.driver
            jdbcUrl = dbConfig.prod.jdbcUrl
            username = dbConfig.prod.user
            password = dbConfig.prod.password
        } else {
            driverClassName = dbConfig.test.driver
            jdbcUrl = dbConfig.test.jdbcUrl
            username = dbConfig.test.user
            password = dbConfig.test.password
        }
        maximumPoolSize = 10
    }
    return HikariDataSource(config).apply {
        validate()
    }
}

fun runLiquibaseMigrations(dataSource: HikariDataSource) {
    dataSource.connection.use { connection ->
        val liquibase = Liquibase(
            "db/changelog/db.changelog-master.xml",
            ClassLoaderResourceAccessor(),
            JdbcConnection(connection)
        )
        liquibase.update("")
    }
}