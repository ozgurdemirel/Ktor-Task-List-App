package com.odemirel.adapters.templates

import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.dialect.AbstractDialect
import org.thymeleaf.dialect.IExpressionObjectDialect
import org.thymeleaf.expression.IExpressionObjectFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.*

/**
 * Thymeleaf dialect for time ago formatting
 * This is an adapter between the template engine and the application
 */
class TimeAgoDialect : AbstractDialect("timeAgo"), IExpressionObjectDialect {
    private val EXPRESSION_OBJECTS: MutableSet<String> = Collections.singleton("timeAgo")

    override fun getExpressionObjectFactory(): IExpressionObjectFactory {
        return object : IExpressionObjectFactory {
            override fun getAllExpressionObjectNames(): MutableSet<String> {
                return EXPRESSION_OBJECTS
            }

            override fun buildObject(context: IExpressionContext, expressionObjectName: String): Any? {
                return if (expressionObjectName == "timeAgo") TimeAgoUtil else null
            }

            override fun isCacheable(expressionObjectName: String): Boolean {
                return true
            }
        }
    }
}

object TimeAgoUtil {
    private val iso8601Formatter = DateTimeFormatter.ISO_DATE_TIME

    fun format(temporal: Temporal?): String = temporal
        ?.let(::toLocalDateTime)
        ?.let(::calculateTimeAgo)
        ?: ""

    fun toISOString(temporal: Temporal?): String = temporal
        ?.let(::toLocalDateTime)
        ?.let(::toISOStringUTC)
        ?: ""

    fun formatDate(temporal: Temporal?, pattern: String = "yyyy-MM-dd HH:mm"): String = temporal
        ?.let(::toLocalDateTime)
        ?.let { formatWithPattern(it, pattern) }
        ?: ""

    private fun toLocalDateTime(temporal: Temporal): LocalDateTime = LocalDateTime.from(temporal)

    private fun calculateTimeAgo(dateTime: LocalDateTime): String =
        Duration.between(dateTime, LocalDateTime.now(ZoneOffset.UTC)).let { duration ->
            when {
                duration.isNegative -> "- - -"
                duration.toMinutes() < 1 -> "just now"
                duration.toMinutes() < 60 -> "${duration.toMinutes()} minute${pluralize(duration.toMinutes())} ago"
                duration.toHours() < 24 -> "${duration.toHours()} hour${pluralize(duration.toHours())} ago"
                duration.toDays() < 30 -> "${duration.toDays()} day${pluralize(duration.toDays())} ago"
                duration.toDays() < 365 -> "${duration.toDays() / 30} month${pluralize(duration.toDays() / 30)} ago"
                else -> "${duration.toDays() / 365} year${pluralize(duration.toDays() / 365)} ago"
            }
        }

    private fun pluralize(value: Long) = if (value == 1L) "" else "s"

    private fun toISOStringUTC(dateTime: LocalDateTime): String =
        dateTime.atZone(ZoneOffset.UTC).format(iso8601Formatter)

    private fun formatWithPattern(dateTime: LocalDateTime, pattern: String): String =
        dateTime.format(DateTimeFormatter.ofPattern(pattern))
} 