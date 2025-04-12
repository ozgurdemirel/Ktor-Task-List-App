package com.odemirel.dto

data class PaginatedResult<T>(
    val items: List<T>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int
) {
    val totalPages: Int
        get() = ((totalCount + pageSize - 1) / pageSize).toInt()
        
    val hasNextPage: Boolean
        get() = page < totalPages
        
    val hasPreviousPage: Boolean
        get() = page > 1
} 