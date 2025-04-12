package com.odemirel.model

object TasksTable : BaseTable("tasks") {
    val title = varchar("title", 255)
    val description = text("description")
    val longDescription = text("long_description")
    val completed = bool("completed").default(false)
}