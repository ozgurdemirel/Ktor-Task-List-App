package com.odemirel.config

import com.odemirel.adapters.templates.IntegerDialect
import com.odemirel.adapters.templates.TimeAgoDialect
import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect

/**
 * Configures Thymeleaf templating engine
 * Links template dialects from the adapters layer to the application
 */
fun Application.configureTemplating() {
    // https://ktor.io/docs/server-thymeleaf.html#auto-reload to configure auto-reload check
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
            templateMode = TemplateMode.HTML
        })
        addDialect(LayoutDialect())
        addDialect(TimeAgoDialect())
        addDialect(IntegerDialect())
    }
}
