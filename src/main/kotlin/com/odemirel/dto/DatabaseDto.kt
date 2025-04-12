package com.odemirel.dto

data class DbProperties(
    val jdbcUrl: String,
    val driver: String,
    val user: String,
    val password: String
)

data class DatabaseConfig(
    val environment: String,
    val test: DbProperties,
    val prod: DbProperties
) 