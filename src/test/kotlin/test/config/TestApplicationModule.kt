package test.config

import com.odemirel.config.configureRouting
import com.odemirel.config.configureSerialization
import com.odemirel.config.configureTemplating
import com.odemirel.dto.DatabaseConfig
import com.odemirel.dto.DbProperties
import io.ktor.server.application.*

/**
 * Initialize the application for testing, ensuring Liquibase migrations
 * run only once across all tests.
 */
fun Application.testModule() {
    // Initialize database with hardcoded test configuration
    val testDbConfig = DatabaseConfig(
        environment = "development",
        test = DbProperties(
            jdbcUrl = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ),
        prod = DbProperties(
            jdbcUrl = "jdbc:postgresql://localhost:5432/ozgurclub",
            driver = "org.postgresql.Driver",
            user = "ozgurclub",
            password = "password"
        )
    )

    TestDatabaseManager.initializeDatabaseForTestsWithConfig(testDbConfig)
    
    // Configure the rest of the application
    configureSerialization()
    configureTemplating()
    configureRouting()
} 