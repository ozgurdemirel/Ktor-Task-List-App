package com.odemirel.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.ZoneOffset

fun currentUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

abstract class BaseTable(name: String) : LongIdTable(name) {

    val createdAt = datetime("created_at")
        .clientDefault { currentUtc() }
    val updatedAt = datetime("updated_at")
        .nullable()
}
