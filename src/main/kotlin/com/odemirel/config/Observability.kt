package com.odemirel.config

import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.hikari.HikariConnectionsHealthCheck
import com.sksamuel.cohort.hikari.HikariDataSourceManager
import com.sksamuel.cohort.liquibase.LiquibaseMigrations
import com.sksamuel.cohort.logback.LogbackManager
import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import liquibase.Liquibase
import kotlin.time.Duration.Companion.seconds


fun Application.configureObservability(
    dataSource: HikariDataSource,
    liquibase: Liquibase,
) {

    // readiness and liveness check could be differentiated on read world

    val readinessChecks = HealthCheckRegistry(Dispatchers.IO) {
        register(HikariConnectionsHealthCheck(dataSource, 1), 10.seconds, 10.seconds)
    }

    val livenessChecks = HealthCheckRegistry(Dispatchers.IO) {
        register(ThreadDeadlockHealthCheck(), 6.seconds, 30.seconds)
        register(HikariConnectionsHealthCheck(dataSource, 1), 10.seconds, 10.seconds)
    }

    install(Cohort) {

        endpointPrefix = "admin"
        logManager = LogbackManager
        dataSources = listOf(HikariDataSourceManager(dataSource))

        //migrations = LiquibaseMigrations(dataSource) // GET /admin/dbmigration fix on newer version
        migrations = LiquibaseMigrations(dataSource, liquibase.changeLogFile) // GET /admin/dbmigration

        // optional
        threadDump = true      // GET /admin/threaddump
        jvmInfo = true         // GET /admin/jvm
        verboseHealthCheckResponse = !EnvironmentManager.isProduction()


        healthcheck("/readiness", readinessChecks)
        healthcheck("/liveness", livenessChecks)

    }


}
