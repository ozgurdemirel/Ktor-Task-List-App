package com.odemirel.config

import io.ktor.server.config.*
import com.odemirel.dto.DbProperties
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

object LiquibaseRunner {

    private const val CHANGELOG = "db/changelog/db.changelog-master.xml"

    fun run(ds: HikariDataSource) {
        ds.connection.use { conn ->
            val database = DatabaseFactory
                .getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(conn))
            val liquibase = Liquibase(
                CHANGELOG,
                ClassLoaderResourceAccessor(),
                database
            )
            liquibase.update(Contexts(), LabelExpression())
        }
    }
}

object DataSourceFactory {

    fun create(environment: ApplicationConfig): HikariDataSource {
        val db = loadDatabaseConfig(environment)
        val hikariCfg = HikariConfig().apply {
            jdbcUrl = db.jdbcUrl
            driverClassName = db.driver
            username = db.user
            password = db.password

            maximumPoolSize = environment.int("database.hikari.maxPool", 10)
            minimumIdle = environment.int("database.hikari.minIdle", 2)
            idleTimeout = environment.long("database.hikari.idleTimeoutMs", 60_000)
            connectionTimeout = environment.long("database.hikari.connectionTimeoutMs", 30_000)
            validationTimeout = 5_000
            leakDetectionThreshold = environment.long("database.hikari.leakDetectionMs", 0)
            isAutoCommit = false
        }
        return HikariDataSource(hikariCfg).also { it.validate() }
    }

    private fun loadDatabaseConfig(appConf: ApplicationConfig): DbProperties {
        val env = appConf.property("database.environment").getString() // "prod" | "test"
        val prefix = if (env == "prod") "database.prod" else "database.test"

        fun prop(key: String) = appConf.property("$prefix.$key").getString()

        return DbProperties(
            jdbcUrl = prop("jdbcUrl"),
            driver = prop("driver"),
            user = prop("user"),
            password = prop("password")
        )
    }

    private fun ApplicationConfig.int(path: String, default: Int) =
        propertyOrNull(path)?.getString()?.toInt() ?: default

    private fun ApplicationConfig.long(path: String, default: Long) =
        propertyOrNull(path)?.getString()?.toLong() ?: default
}




