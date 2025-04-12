package com.odemirel.routes

import com.odemirel.adapters.web.DomainToWebAdapter.handleCreateTaskResult
import com.odemirel.adapters.web.DomainToWebAdapter.handleDeleteTaskResult
import com.odemirel.adapters.web.DomainToWebAdapter.handleToggleTaskResult
import com.odemirel.adapters.web.DomainToWebAdapter.handleUpdateTaskResult
import com.odemirel.adapters.web.DomainToWebAdapter.respondWithIndexPage
import com.odemirel.adapters.web.WebToDomainAdapter.extractTaskParameters
import com.odemirel.repository.TaskRepository
import com.odemirel.service.TaskService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Route.taskRoutes() {
    // Dependency injection...
    val taskService = TaskService(TaskRepository())
    val pageSize = 10

    get("/tasks") {
        val page = call.parameters["page"]?.toIntOrNull() ?: 1
        call.respondWithIndexPage(taskService, pageSize, page)
    }

    get("/tasks/create") {
        call.respond(ThymeleafContent("create.html", emptyMap()))
    }

    post("/tasks") {
        val task = call.extractTaskParameters()
        val result = taskService.createTask(task)
        result.handleCreateTaskResult(call, taskService, pageSize, task)
    }

    get("/tasks/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@get
        }

        val task = taskService.getTaskById(id)
        if (task == null) {
            call.respondWithIndexPage(taskService, pageSize, errorMessage = "Task with ID $id was not found")
        } else {
            call.respond(ThymeleafContent("show.html", mapOf("task" to task)))
        }
    }

    post("/tasks/{id}/toggle") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@post
        }

        val result = taskService.toggleTaskCompletion(id)
        result.handleToggleTaskResult(call, id, taskService)
    }

    post("/tasks/{id}/delete") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@post
        }

        val result = taskService.deleteTask(id)
        result.handleDeleteTaskResult(call, taskService, pageSize, id)
    }

    get("/tasks/{id}/edit") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@get
        }

        val task = taskService.getTaskById(id)
        if (task == null) {
            call.respondWithIndexPage(taskService, pageSize, errorMessage = "Task with ID $id was not found")
        } else {
            call.respond(ThymeleafContent("edit.html", mapOf("task" to task)))
        }
    }

    post("/tasks/{id}/edit") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@post
        }

        val task = call.extractTaskParameters(id)
        val result = taskService.updateTask(task)
        result.handleUpdateTaskResult(call, taskService, pageSize, id, task)
    }
}