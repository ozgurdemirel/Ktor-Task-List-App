package com.odemirel.service

import com.odemirel.dto.PaginatedResult
import com.odemirel.dto.Task
import com.odemirel.repository.TaskRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking



import java.time.LocalDateTime
import kotlin.test.*

class TaskServiceTest {
    private lateinit var taskRepo: TaskRepository
    private lateinit var taskSrv: TaskService

    @BeforeTest
    fun setUp() {
        taskRepo = mockk()
        taskSrv = TaskService(taskRepo)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createTask with valid input returns success`() = runBlocking {
        val time = LocalDateTime.now()
        val task = Task(
            0, "Test Task", "Test description", "Long description",
            false, time, time
        )
        coEvery { taskRepo.createTask(any()) } returns 1L

        val result = taskSrv.createTask(task)
        assertIs<TaskOperationResult.Success>(result)
        assertEquals(1L, result.taskId)
        coVerify { taskRepo.createTask(any()) }
    }

    @Test
    fun `createTask with invalid input returns error`() = runBlocking {
        val time = LocalDateTime.now()
        val task = Task(
            0, "", "Test description", "Long description",
            false, time, time
        )
        val result = taskSrv.createTask(task)
        assertIs<TaskOperationResult.Error>(result)
        assertTrue(result.errors.containsKey("title"))
        coVerify(exactly = 0) { taskRepo.createTask(any()) }
    }

    @Test
    fun `getTaskById returns task when found`() = runBlocking {
        val id = 1L
        val time = LocalDateTime.now()
        val expected = Task(
            id, "Test Task", "Test description", "Long description",
            false, time, time
        )
        coEvery { taskRepo.getTaskById(id) } returns expected

        val result = taskSrv.getTaskById(id)
        assertNotNull(result)
        assertEquals(expected, result)
        coVerify { taskRepo.getTaskById(id) }
    }

    @Test
    fun `getTaskById returns null when task not found`() = runBlocking {
        coEvery { taskRepo.getTaskById(999L) } returns null
        val result = taskSrv.getTaskById(999L)
        assertEquals(null, result)
        coVerify { taskRepo.getTaskById(999L) }
    }

    @Test
    fun `getPaginatedTasks returns paginated result`() = runBlocking {
        val time = LocalDateTime.now()
        val tasks = listOf(Task(1L, "Task 1", "Description 1", "", false, time, time))
        val paginated = PaginatedResult(tasks, 1, 1, 10)
        coEvery { taskRepo.getPaginatedTasks(1, 10) } returns paginated

        val result = taskSrv.getPaginatedTasks(1, 10)
        assertEquals(paginated, result)
        coVerify { taskRepo.getPaginatedTasks(1, 10) }
    }

    @Test
    fun `updateTask with valid input returns success when task exists`() = runBlocking {
        val time = LocalDateTime.now()
        val existing = Task(
            1L, "Original Task", "Original description", "",
            false, time, time
        )
        val updated = Task(
            1L, "Updated Task", "Updated description", "",
            false, time, time
        )
        coEvery { taskRepo.getTaskById(1L) } returns existing
        coEvery { taskRepo.updateTask(1L, any()) } returns true

        val result = taskSrv.updateTask(updated)
        assertIs<TaskOperationResult.Success>(result)
        assertEquals(1L, result.taskId)
        coVerify {
            taskRepo.getTaskById(1L)
            taskRepo.updateTask(1L, any())
        }
    }

    @Test
    fun `toggleTaskCompletion returns success when task exists`() = runBlocking {
        coEvery { taskRepo.toggleTaskCompletion(1L) } returns true
        val result = taskSrv.toggleTaskCompletion(1L)
        assertIs<TaskOperationResult.Success>(result)
        assertEquals(1L, result.taskId)
        coVerify { taskRepo.toggleTaskCompletion(1L) }
    }

    @Test
    fun `deleteTask returns success when task exists`() = runBlocking {
        coEvery { taskRepo.deleteTask(1L) } returns true
        val result = taskSrv.deleteTask(1L)
        assertIs<TaskOperationResult.Success>(result)
        assertEquals(1L, result.taskId)
        coVerify { taskRepo.deleteTask(1L) }
    }
}