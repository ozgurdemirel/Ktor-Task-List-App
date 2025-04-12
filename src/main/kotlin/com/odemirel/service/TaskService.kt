package com.odemirel.service

import com.odemirel.dto.PaginatedResult
import com.odemirel.dto.Task
import com.odemirel.repository.TaskRepository
import java.time.LocalDateTime

class TaskService(private val taskRepository: TaskRepository) {

   private fun validateTask(task: Task): ValidationResult {
    val errors = mutableMapOf<String, String>()
    when {
        task.title.isBlank() -> errors["title"] = "Title cannot be empty"
        task.title.length < 3 -> errors["title"] = "Title must be at least 3 characters"
        task.title.length > 100 -> errors["title"] = "Title cannot exceed 100 characters"
    }

    when {
        task.description.isBlank() -> errors["description"] = "Description cannot be empty"
        task.description.length < 10 -> errors["description"] = "Description must be at least 10 characters"
        task.description.length > 500 -> errors["description"] = "Description cannot exceed 500 characters"
    }

    if (task.longDescription.isNotBlank() && task.longDescription.length > 10000) {
        errors["longDescription"] = "Long description cannot exceed 10000 characters"
    }

    return if (errors.isEmpty()) ValidationResult.Valid(task) else ValidationResult.Invalid(errors)
}

    suspend fun createTask(task: Task): TaskOperationResult {
        val taskToValidate = task.copy(
            completed = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        return when (val validationResult = validateTask(taskToValidate)) {
            is ValidationResult.Valid -> {
                val taskId = taskRepository.createTask(validationResult.task)
                TaskOperationResult.Success(taskId)
            }
            is ValidationResult.Invalid -> {
                TaskOperationResult.Error(validationResult.errors)
            }
        }
    }


    suspend fun getTaskById(id: Long): Task? {
        return taskRepository.getTaskById(id)
    }

    suspend fun getPaginatedTasks(page: Int = 1, pageSize: Int = 10): PaginatedResult<Task> {
        return taskRepository.getPaginatedTasks(page, pageSize)
    }

    suspend fun updateTask(task: Task): TaskOperationResult {
        val existingTask = taskRepository.getTaskById(task.id) ?: return TaskOperationResult.NotFound

        val taskToValidate = task.copy(
            createdAt = existingTask.createdAt,
            updatedAt = LocalDateTime.now()
        )

        return when (val validationResult = validateTask(taskToValidate)) {
            is ValidationResult.Valid -> {
                val updated = taskRepository.updateTask(task.id, validationResult.task)
                if (updated) TaskOperationResult.Success(task.id) else TaskOperationResult.NotFound
            }
            is ValidationResult.Invalid -> {
                TaskOperationResult.Error(validationResult.errors)
            }
        }
    }

    suspend fun toggleTaskCompletion(id: Long): TaskOperationResult {
        val toggled = taskRepository.toggleTaskCompletion(id)
        return if (toggled) TaskOperationResult.Success(id) else TaskOperationResult.NotFound
    }

    suspend fun deleteTask(id: Long): TaskOperationResult {
        val deleted = taskRepository.deleteTask(id)
        return if (deleted) TaskOperationResult.Success(id) else TaskOperationResult.NotFound
    }
}

sealed class ValidationResult {
    data class Valid(val task: Task) : ValidationResult()
    data class Invalid(val errors: Map<String, String>) : ValidationResult()
}

sealed class TaskOperationResult {
    data class Success(val taskId: Long) : TaskOperationResult()
    data class Error(val errors: Map<String, String>) : TaskOperationResult()
    data object NotFound : TaskOperationResult()
} 