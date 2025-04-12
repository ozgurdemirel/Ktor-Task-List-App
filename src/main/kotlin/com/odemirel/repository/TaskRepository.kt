package com.odemirel.repository

import com.odemirel.dto.PaginatedResult
import com.odemirel.dto.Task
import com.odemirel.model.TasksTable
import com.odemirel.model.currentUtc
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class TaskRepository {

    /**
     * Executes the provided database operation on the IO dispatcher, ensuring that the main thread is not blocked.
     * This utilizes a suspended transaction, which properly supports coroutines and centralizes transaction management,
     * thus removing boilerplate and improving error handling.
     *
     * @param block The suspend function containing the database operation.
     * @return The result of the database operation.
     */
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun toTask(row: ResultRow): Task = Task(
        id = row[TasksTable.id].value,
        title = row[TasksTable.title],
        description = row[TasksTable.description],
        longDescription = row[TasksTable.longDescription],
        completed = row[TasksTable.completed],
        createdAt = row[TasksTable.createdAt],
        updatedAt = row[TasksTable.updatedAt]
    )

    suspend fun createTask(task: Task): Long = dbQuery {
        val insertedId = TasksTable.insertAndGetId {
            it[title] = task.title
            it[description] = task.description
            it[longDescription] = task.longDescription
            it[completed] = task.completed
        }
        insertedId.value
    }

    suspend fun getTaskById(id: Long): Task? = dbQuery {
        TasksTable.select(TasksTable.columns)
            .where { TasksTable.id eq id }
            .limit(1)
            .map { toTask(it) }
            .firstOrNull()
    }

    suspend fun getPaginatedTasks(page: Int = 1, pageSize: Int = 10): PaginatedResult<Task> = dbQuery {
        val offset = (page - 1) * pageSize
        val totalCount = TasksTable.selectAll().count()

        val items = TasksTable.selectAll()
            .orderBy(TasksTable.id, SortOrder.DESC)
            .limit(pageSize).offset(offset.toLong())
            .map { toTask(it) }

        PaginatedResult(
            items = items,
            totalCount = totalCount,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun updateTask(id: Long, task: Task): Boolean = dbQuery {
        val updatedRows = TasksTable.update({ TasksTable.id eq id }) {
            it[title] = task.title
            it[description] = task.description
            it[longDescription] = task.longDescription
            it[completed] = task.completed
            it[updatedAt] = currentUtc()
        }
        updatedRows > 0
    }

    suspend fun toggleTaskCompletion(id: Long): Boolean = dbQuery {
        val existingTask = TasksTable
            .select(TasksTable.columns)
            .where { TasksTable.id eq id }
            .singleOrNull()
            ?.let { toTask(it) }
            ?: return@dbQuery false

        val newCompletedValue = !existingTask.completed

        val updatedRows = TasksTable.update({ TasksTable.id eq id }) {
            it[completed] = newCompletedValue
            it[updatedAt] = LocalDateTime.now()
        }
        updatedRows > 0
    }

    suspend fun deleteTask(id: Long): Boolean = dbQuery {
        val deletedRows = TasksTable.deleteWhere { TasksTable.id eq id }
        deletedRows > 0
    }
}
