package test.config

import com.odemirel.config.createDataSource
import com.odemirel.dto.DatabaseConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton object to manage database initialization for tests.
 * Ensures Liquibase migrations are run only once during test execution.
 */
object TestDatabaseManager {
    private val initialized = AtomicBoolean(false)
    var dataSource: HikariDataSource? = null
    
    // method that accepts a DatabaseConfig directly
    fun initializeDatabaseForTestsWithConfig(dbConfig: DatabaseConfig) {
        if (initialized.compareAndSet(false, true)) {
            // Initialize database only once
            dataSource = createDataSource(dbConfig).also { ds ->
                runLiquibaseMigrations(ds)
                Database.connect(ds)
            }
            
            // Register shutdown hook to close datasource
            Runtime.getRuntime().addShutdownHook(Thread {
                dataSource?.close()
            })
        }
    }

    private fun runLiquibaseMigrations(dataSource: HikariDataSource) {
        dataSource.connection.use { connection ->
            val liquibase = Liquibase(
                "db/changelog/db.changelog-master.xml",
                ClassLoaderResourceAccessor(),
                JdbcConnection(connection)
            )
            liquibase.update("")
        }
    }
} 