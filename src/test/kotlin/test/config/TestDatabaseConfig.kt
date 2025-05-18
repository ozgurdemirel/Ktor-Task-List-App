package test.config

import com.odemirel.config.LiquibaseRunner
import com.odemirel.dto.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extension for creating a HikariDataSource from a DatabaseConfig.
 */
object DataSourceFactoryExtension {
    fun create(config: DatabaseConfig): HikariDataSource {
        val dbProps = if (config.environment == "prod") config.prod else config.test
        
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbProps.jdbcUrl
            driverClassName = dbProps.driver
            username = dbProps.user
            password = dbProps.password
            
            // Sensible defaults for tests
            maximumPoolSize = 5
            minimumIdle = 1
            idleTimeout = 30_000
            connectionTimeout = 10_000
            validationTimeout = 5_000
            isAutoCommit = false
        }
        
        return HikariDataSource(hikariConfig)
    }
}

/**
 * Singleton object to manage database initialization for tests.
 * Ensures Liquibase migrations are run only once during test execution.
 */
object TestDatabaseManager {
    private val initialized = AtomicBoolean(false)
    var dataSource: HikariDataSource? = null
    
    // method that accepts a DatabaseConfig directly
    fun initializeDatabaseForTestsWithConfig(dbConfig: DatabaseConfig) {
        // Thread-safe check to ensure database is initialized only once
        // compareAndSet atomically sets the value to true only if current value is false
        // and returns whether the operation succeeded
        if (initialized.compareAndSet(false, true)) {
            // Initialize database only once using DataSourceFactoryExtension
            dataSource = DataSourceFactoryExtension.create(dbConfig).also { ds ->
                // Use the core LiquibaseRunner for migrations
                LiquibaseRunner.run(ds)
                Database.connect(ds)
            }
            
            // Register shutdown hook to close datasource
            Runtime.getRuntime().addShutdownHook(Thread {
                dataSource?.close()
            })
        }
    }
}