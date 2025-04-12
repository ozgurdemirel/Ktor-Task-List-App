package com.odemirel.dto

import java.time.LocalDateTime

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val longDescription: String,
    val completed: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) 