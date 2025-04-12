package com.odemirel.config

import com.odemirel.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Root path handler
        get("/") {
            call.respondRedirect("/tasks")
        }
        
        // Register task routes
        taskRoutes()
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
