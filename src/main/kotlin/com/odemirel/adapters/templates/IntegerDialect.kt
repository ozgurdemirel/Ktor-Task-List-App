package com.odemirel.adapters.templates

import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.dialect.AbstractDialect
import org.thymeleaf.dialect.IExpressionObjectDialect
import org.thymeleaf.expression.IExpressionObjectFactory
import java.util.*

/**
 * Thymeleaf dialect for integer operations
 * This is an adapter between the template engine and the application
 */
class IntegerDialect : AbstractDialect("integer"), IExpressionObjectDialect {
    private val EXPRESSION_OBJECTS: MutableSet<String> = Collections.singleton("int")

    override fun getExpressionObjectFactory(): IExpressionObjectFactory {
        return object : IExpressionObjectFactory {
            override fun getAllExpressionObjectNames(): MutableSet<String> {
                return EXPRESSION_OBJECTS
            }

            override fun buildObject(context: IExpressionContext, expressionObjectName: String): Any? {
                return if (expressionObjectName == "int") IntegerUtil else null
            }

            override fun isCacheable(expressionObjectName: String): Boolean {
                return true
            }
        }
    }
}

object IntegerUtil {
    fun parse(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    // Add methods to handle Integer parameters directly
    fun plus(value: Int): Int = value
    
    fun plus(value: Int, amount: Int): Int = value + amount
    
    fun plus(value: String?, amount: Int): Int? {
        val parsedValue = parse(value)
        return parsedValue?.plus(amount)
    }
    
    fun plus(value: String?, amount: String?): Int? {
        val parsedValue = parse(value)
        val parsedAmount = parse(amount)
        
        return if (parsedValue != null && parsedAmount != null) {
            parsedValue + parsedAmount
        } else {
            null
        }
    }
} 