plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.odemirel"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
    implementation(libs.exposed.dao)
    implementation(libs.h2)
    implementation(libs.hikaricp)
    implementation(libs.liquibase)
    implementation(libs.postgresql)
    implementation(libs.thymeleaf.layout.dialect)
    implementation(libs.ktor.server.thymeleaf)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.cohort.ktor)
    implementation(libs.cohort.core)
    implementation(libs.cohort.hikari)
    implementation(libs.cohort.liquibase)
    implementation(libs.cohort.logback)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.jsoup)
    testImplementation(libs.kotlin.test.junit)

}
