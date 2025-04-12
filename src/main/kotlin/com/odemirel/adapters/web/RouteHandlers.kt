package com.odemirel.adapters.web

import com.odemirel.dto.Task
import com.odemirel.model.currentUtc
import com.odemirel.service.TaskOperationResult
import com.odemirel.service.TaskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.thymeleaf.*

/**
 * Adapter for converting web form data to domain objects
 * This serves as a primary adapter in hexagonal architecture, 
 * translating external input (HTTP) to domain model
 */
object WebToDomainAdapter {
    /**
     * Converts HTTP form parameters to a Task domain object
     */
    suspend fun ApplicationCall.extractTaskParameters(id: Long = 0): Task {
        val params = receiveParameters()
        return Task(
            id = id,
            title = params["title"].orEmpty(),
            description = params["description"].orEmpty(),
            longDescription = params["longDescription"].orEmpty(),
            completed = params["completed"] != null,
            createdAt = currentUtc(),
            updatedAt = if (id > 0) currentUtc() else null
        )
    }
}

/**
 * Adapter for rendering responses to web clients
 * This serves as a secondary adapter in hexagonal architecture,
 * translating domain results to external output (HTTP responses)
 */
object DomainToWebAdapter {
    /**
     * Renders the index page with task list and optional messages
     */
    suspend fun ApplicationCall.respondWithIndexPage(
        taskService: TaskService,
        pageSize: Int,
        page: Int = 1,
        successMessage: String? = null,
        errorMessage: String? = null
    ) {
        val paginatedResult = taskService.getPaginatedTasks(page, pageSize)
        val model = mutableMapOf(
            "tasks" to paginatedResult.items,
            "currentPage" to paginatedResult.page,
            "totalPages" to paginatedResult.totalPages,
            "hasNextPage" to paginatedResult.hasNextPage,
            "hasPreviousPage" to paginatedResult.hasPreviousPage
        )

        successMessage?.let { model["success"] = it }
        errorMessage?.let { model["error"] = it }

        respond(ThymeleafContent("index.html", model))
    }

    /**
     * Handles the result of task creation operation and generates appropriate response
     */
    suspend fun TaskOperationResult.handleCreateTaskResult(
        call: ApplicationCall,
        taskService: TaskService,
        pageSize: Int,
        taskParams: Task
    ) {
        when (this) {
            is TaskOperationResult.Success -> {
                call.respondWithIndexPage(taskService, pageSize, successMessage = "Task created successfully!")
            }
            is TaskOperationResult.Error -> {
                call.respond(
                    ThymeleafContent(
                        "create.html",
                        mapOf(
                            "errors" to errors,
                            "task" to mapOf(
                                "title" to taskParams.title,
                                "description" to taskParams.description,
                                "longDescription" to taskParams.longDescription,
                                "completed" to false
                            )
                        )
                    )
                )
            }
            is TaskOperationResult.NotFound -> {
                call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }
    }

    /**
     * Handles the result of task update operation and generates appropriate response
     */
    suspend fun TaskOperationResult.handleUpdateTaskResult(
        call: ApplicationCall,
        taskService: TaskService,
        pageSize: Int,
        id: Long,
        taskParams: Task
    ) {
        when (this) {
            is TaskOperationResult.Success -> {
                call.respondWithIndexPage(taskService, pageSize, successMessage = "Task updated successfully!")
            }
            is TaskOperationResult.Error -> {
                call.respond(
                    ThymeleafContent(
                        "edit.html",
                        mapOf(
                            "errors" to errors,
                            "task" to mapOf(
                                "id" to id,
                                "title" to taskParams.title,
                                "description" to taskParams.description,
                                "longDescription" to taskParams.longDescription,
                                "completed" to taskParams.completed
                            )
                        )
                    )
                )
            }
            is TaskOperationResult.NotFound -> {
                call.respondWithIndexPage(taskService, pageSize, errorMessage = "Task with ID $id was not found")
            }
        }
    }

    /**
     * Handles the result of task deletion operation and generates appropriate response
     */
    suspend fun TaskOperationResult.handleDeleteTaskResult(
        call: ApplicationCall,
        taskService: TaskService,
        pageSize: Int,
        id: Long
    ) {
        when (this) {
            is TaskOperationResult.Success -> {
                call.respondWithIndexPage(taskService, pageSize, successMessage = "Task deleted successfully!")
            }
            is TaskOperationResult.NotFound -> {
                call.respond(HttpStatusCode.NotFound, "Task with ID $id was not found")
            }
            is TaskOperationResult.Error -> {
                call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }
    }

    /**
     * Handles the result of task toggle operation and generates appropriate response
     */
    suspend fun TaskOperationResult.handleToggleTaskResult(
        call: ApplicationCall,
        id: Long,
        taskService: TaskService
    ) {
        when (this) {
            is TaskOperationResult.Success -> {
                val task = taskService.getTaskById(id)
                if (task == null) {
                    call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                } else {
                    call.respond(
                        ThymeleafContent(
                            "show.html",
                            mapOf("task" to task, "success" to "Task status updated successfully!")
                        )
                    )
                }
            }
            is TaskOperationResult.NotFound -> {
                call.respond(HttpStatusCode.NotFound, "Task with ID $id was not found")
            }
            is TaskOperationResult.Error -> {
                call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }
    }
}
