package com.odemirel.repository

import com.odemirel.dto.DatabaseConfig
import com.odemirel.dto.DbProperties
import com.odemirel.dto.Task
import com.odemirel.model.TasksTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import test.config.TestDatabaseManager
import java.time.LocalDateTime
import kotlin.test.*

class TaskRepositoryTest {

    private lateinit var repository: TaskRepository
    private lateinit var db: Database

    private val testDbConfig = DatabaseConfig(
        environment = "development",
        test = DbProperties(
            jdbcUrl = "jdbc:h2:mem:test_repo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ),
        prod = DbProperties("", "", "", "")
    )

    @BeforeTest
    fun setup() {
        // Ensure the database is initialized *before* accessing the dataSource.
        // TestDatabaseManager's internal AtomicBoolean prevents multiple initializations.
        TestDatabaseManager.initializeDatabaseForTestsWithConfig(testDbConfig)
        db = Database.connect(TestDatabaseManager.dataSource!!)
        repository = TaskRepository()

        transaction(db) {
            TasksTable.deleteAll()
        }
    }

    @AfterTest
    fun tearDown() {
        // Optional cleanup
    }

    private suspend fun insertTestTask(
        title: String = "Test Task",
        description: String = "Test Desc",
        completed: Boolean = false
    ): Long {
        return newSuspendedTransaction(db = db) {
            TasksTable.insertAndGetId {
                it[TasksTable.title] = title
                it[TasksTable.description] = description
                it[TasksTable.longDescription] = "Long description for $title"
                it[TasksTable.completed] = completed
            }.value
        }
    }

    private fun createSampleTaskDto(
        id: Long = 0,
        title: String = "Sample Task",
        description: String = "Sample Desc"
    ): Task {
        val now = LocalDateTime.now()
        return Task(
            id = id,
            title = title,
            description = description,
            longDescription = "Long desc for $title",
            completed = false,
            createdAt = now,
            updatedAt = null
        )
    }

    @Test
    fun `createTask should insert a new task and return its ID`() = runBlocking {
        val taskDto = createSampleTaskDto(title = "New Task", description = "Desc for new task")
        val createdId = repository.createTask(taskDto)
        assertTrue(createdId > 0)

        val retrievedTask = repository.getTaskById(createdId)
        assertNotNull(retrievedTask)
        assertEquals(taskDto.title, retrievedTask.title)
        assertEquals(taskDto.description, retrievedTask.description)
        assertFalse(retrievedTask.completed)
    }

    @Test
    fun `getTaskById should return task when found`() = runBlocking {
        val insertedId = insertTestTask(title = "Find Me")
        val foundTask = repository.getTaskById(insertedId)
        assertNotNull(foundTask)
        assertEquals(insertedId, foundTask.id)
        assertEquals("Find Me", foundTask.title)
    }

    @Test
    fun `getTaskById should return null when not found`() = runBlocking {
        val foundTask = repository.getTaskById(9999L)
        assertNull(foundTask)
    }

    @Test
    fun `getPaginatedTasks should return correct page and items`() = runBlocking {
        for (i in 1..15) {
            insertTestTask(title = "Task $i")
        }

        var result = repository.getPaginatedTasks(page = 1, pageSize = 10)
        assertEquals(10, result.items.size)
        assertEquals(1, result.page)
        assertEquals(15, result.totalCount)
        assertEquals(2, result.totalPages)
        assertTrue(result.hasNextPage)
        assertFalse(result.hasPreviousPage)
        assertEquals("Task 15", result.items.first().title)

        result = repository.getPaginatedTasks(page = 2, pageSize = 10)
        assertEquals(5, result.items.size)
        assertEquals(2, result.page)
        assertEquals(15, result.totalCount)
        assertEquals(2, result.totalPages)
        assertFalse(result.hasNextPage)
        assertTrue(result.hasPreviousPage)
        assertEquals("Task 5", result.items.first().title)

        result = repository.getPaginatedTasks(page = 3, pageSize = 10)
        assertEquals(0, result.items.size)
        assertEquals(3, result.page)
        assertEquals(15, result.totalCount)
        assertEquals(2, result.totalPages)
        assertFalse(result.hasNextPage)
        assertTrue(result.hasPreviousPage)
    }

    @Test
    fun `updateTask should modify existing task and return true`() = runBlocking {
        val insertedId = insertTestTask(title = "Original Title")
        val originalTask = repository.getTaskById(insertedId)!!
        val taskToUpdate = originalTask.copy(
            title = "Updated Title",
            description = "Updated Description",
            completed = true
        )

        val success = repository.updateTask(insertedId, taskToUpdate)
        assertTrue(success)

        val updatedTask = repository.getTaskById(insertedId)
        assertNotNull(updatedTask)
        assertEquals("Updated Title", updatedTask.title)
        assertEquals("Updated Description", updatedTask.description)
        assertTrue(updatedTask.completed)
        assertNotNull(updatedTask.updatedAt)
        assertTrue(updatedTask.updatedAt!! >= updatedTask.createdAt)
    }

    @Test
    fun `updateTask should return false for non-existent task`() = runBlocking {
        val success = repository.updateTask(8888L, createSampleTaskDto())
        assertFalse(success)
    }

    @Test
    fun `toggleTaskCompletion should flip completed status and return true`() = runBlocking {
        val insertedId = insertTestTask(title = "Toggle Me", completed = false)

        var success = repository.toggleTaskCompletion(insertedId)
        assertTrue(success)
        var task = repository.getTaskById(insertedId)
        assertNotNull(task)
        assertTrue(task.completed)
        assertNotNull(task.updatedAt)
        val firstUpdateTime = task.updatedAt

        success = repository.toggleTaskCompletion(insertedId)
        assertTrue(success)
        task = repository.getTaskById(insertedId)
        assertNotNull(task)
        assertFalse(task.completed)
        assertNotNull(task.updatedAt)
        assertTrue(task.updatedAt!! >= firstUpdateTime!!)
    }

    @Test
    fun `toggleTaskCompletion should return false for non-existent task`() = runBlocking {
        val success = repository.toggleTaskCompletion(7777L)
        assertFalse(success)
    }

    @Test
    fun `deleteTask should remove task and return true`() = runBlocking {
        val insertedId = insertTestTask(title = "Delete Me")
        assertNotNull(repository.getTaskById(insertedId))

        val success = repository.deleteTask(insertedId)
        assertTrue(success)
        assertNull(repository.getTaskById(insertedId))
    }

    @Test
    fun `deleteTask should return false for non-existent task`() = runBlocking {
        val success = repository.deleteTask(6666L)
        assertFalse(success)
    }
}