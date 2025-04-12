package com.odemirel.routes

import com.odemirel.model.TasksTable
import test.config.testModule
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jsoup.Jsoup
import test.config.TestDatabaseManager
import kotlin.test.*

fun withIntegrationTest(block: suspend ApplicationTestBuilder.() -> Unit) = runBlocking {
    testApplication {
        application {
            testModule()
        }

        block()
    }
}

private fun insertTestTask(title: String = "Test Task", description: String = "Test Desc"): Long {
    val db = Database.connect(TestDatabaseManager.dataSource!!)
    return transaction(db) {
        TasksTable.insertAndGetId {
            it[TasksTable.title] = title
            it[TasksTable.description] = description
            it[TasksTable.longDescription] = "Long for $title"
            it[TasksTable.completed] = false
        }.value
    }
}

class TaskRoutesITTest {

    @Test
    fun `GET - check tasks page HTML structure`(): Unit = withIntegrationTest {
        val response = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        val title = doc.select("title").first()
        assertNotNull(title, "Expected an HTML <title> in the page")
        assertTrue(title.text() == "Tasks - Task List")

        val heading = doc.select("h1").first()
        assertNotNull(heading, "Expected an <h1> element in the page")
        assertEquals("All Tasks", heading.text())
    }

    @Test
    fun `GET - check create page content`(): Unit = withIntegrationTest {
        val response = client.get("/tasks/create")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        val heading = doc.select("h1").first()
        assertNotNull(heading, "Expected an <h1> on the create page")
        assertEquals("Create Task", heading.text())

        val submitButton = doc.select("button[type=submit]").first()
        assertNotNull(submitButton, "Expected a submit button in the create form")
        assertTrue(submitButton.text().contains("Create"), "Button text should indicate creation")
    }

    @Test
    fun `GET - invalid task id should return an error message`(): Unit = withIntegrationTest {
        val response = client.get("/tasks/xxx")  // Not a numeric ID
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val body = response.bodyAsText()
        assertEquals("Invalid task ID", body)
    }

    @Test
    fun `POST - create task successfully should redirect and show success message`(): Unit = withIntegrationTest {
        val response = client.post("/tasks") {
            setBody(FormDataContent(Parameters.build {
                append("title", "New Integration Task")
                append("description", "Description from integration test - min 10 chars")
                append("longDescription", "Optional long description.")
            }))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        // Check for success message
        val successMessage = doc.selectFirst("div.ui.positive.message .content")
        assertNotNull(successMessage, "Success message div not found")
        assertEquals("Task created successfully!", successMessage.text())

        // Check if the new task title appears in the table
        val taskLink = doc.selectFirst("table.ui.celled tbody tr td a.header:contains(New Integration Task)")
        assertNotNull(taskLink, "Link for the new task not found in the table")
    }

    @Test
    fun `POST - create task with validation errors should show errors on create page`(): Unit = withIntegrationTest {
        val response = client.post("/tasks") {
            setBody(FormDataContent(Parameters.build {
                append("title", "No") // Title too short
                append("description", "Short") // Description too short
            }))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        assertNotNull(doc.selectFirst("h1:contains(Create Task)"), "Should still be on create page H1")
        assertEquals("Create New Task", doc.title(), "Should still be on create page Title")

        // Check for specific error messages using more precise selectors
        val titleError = doc.selectFirst("div.field.error:has(input#title) .ui.pointing.red.basic.label")
        assertNotNull(titleError, "Title error label not found")
        assertEquals("Title must be at least 3 characters", titleError.text())

        val descriptionError = doc.selectFirst("div.field.error:has(textarea#description) .ui.pointing.red.basic.label")
        assertNotNull(descriptionError, "Description error label not found")
        assertEquals("Description must be at least 10 characters", descriptionError.text())

        // Check that inputs retain values
        assertEquals("No", doc.selectFirst("input#title")?.attr("value"))
        assertEquals("Short", doc.selectFirst("textarea#description")?.text())
    }

    @Test
    fun `GET - specific task page should load correctly`(): Unit = withIntegrationTest {
        val taskId = insertTestTask("Specific Task", "Details for specific task")
        val response = client.get("/tasks/$taskId")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        assertEquals("Task: Specific Task", doc.title())
        assertEquals("Specific Task", doc.selectFirst("h2.ui.header")?.text())
        // More specific selector for description if possible, assuming it's in a grid cell
        val descriptionElement = doc.select("div.ui.grid > div.twelve.wide.column p").firstOrNull { it.text() == "Details for specific task" }
        assertNotNull(descriptionElement, "Description paragraph not found or doesn't match")
    }

    @Test
    fun `GET - specific task page with non-existent numeric id should redirect with error`(): Unit = withIntegrationTest {
        val nonExistentId = 99999L
        val response = client.get("/tasks/$nonExistentId")

        assertEquals(HttpStatusCode.OK, response.status) // After redirect
        val body = response.bodyAsText()
        val doc = Jsoup.parse(body)

        assertNotNull(doc.selectFirst("h1:contains(All Tasks)"), "Should be redirected to index page H1")
        val errorMessage = doc.selectFirst("div.ui.negative.message .content")
        assertNotNull(errorMessage, "Error message div not found")
        assertEquals("Task with ID $nonExistentId was not found", errorMessage.text())
    }

    @Test
    fun `POST - toggle task completion should update status`(): Unit = withIntegrationTest {
        val taskId = insertTestTask("Toggle Me Task", "Description")

        // Initial state check
        var response = client.get("/tasks/$taskId")
        var body = response.bodyAsText()
        var doc = Jsoup.parse(body)
        assertNotNull(doc.selectFirst("div.ui.label.orange:contains(Pending)"), "Task should initially be Pending")

        // Toggle 1: Pending -> Completed
        response = client.post("/tasks/$taskId/toggle")
        assertEquals(HttpStatusCode.OK, response.status)
        body = response.bodyAsText()
        doc = Jsoup.parse(body)
        assertNotNull(doc.selectFirst("div.ui.label.green:contains(Completed)"), "Task should now be Completed")
        assertNotNull(doc.selectFirst(".ui.positive.message:contains(Task status updated successfully!)"), "Should show toggle success message")

        // Toggle 2: Completed -> Pending
        response = client.post("/tasks/$taskId/toggle")
        assertEquals(HttpStatusCode.OK, response.status)
        body = response.bodyAsText()
        doc = Jsoup.parse(body)
        assertNotNull(doc.selectFirst("div.ui.label.orange:contains(Pending)"), "Task should be Pending again")
        assertNotNull(doc.selectFirst(".ui.positive.message:contains(Task status updated successfully!)"), "Should show toggle success message again")
    }

    @Test
    fun `POST - delete task should remove it and redirect`(): Unit = withIntegrationTest {
        val taskId = insertTestTask("Delete Me Task", "This task will be deleted.")

        // Perform deletion
        val deleteResponse = client.post("/tasks/$taskId/delete")
        assertEquals(HttpStatusCode.OK, deleteResponse.status) // After redirect
        val indexBody = deleteResponse.bodyAsText()
        val indexDoc = Jsoup.parse(indexBody)

        // Check success message
        val successMessage = indexDoc.selectFirst("div.ui.positive.message .content")
        assertNotNull(successMessage, "Delete success message not found")
        assertEquals("Task deleted successfully!", successMessage.text())

        // Check task is not listed
        val taskLink = indexDoc.selectFirst("a.header:contains(Delete Me Task)")
        assertNull(taskLink, "Deleted task title should not be on the index page")

        // Verify it's gone by trying to access its page directly (expect redirect + error)
        val getAfterDeleteResponse = client.get("/tasks/$taskId")
        assertEquals(HttpStatusCode.OK, getAfterDeleteResponse.status) // Redirects
        val bodyAfterDelete = getAfterDeleteResponse.bodyAsText()
        val indexDocAfterDelete = Jsoup.parse(bodyAfterDelete)
        val errorMessage = indexDocAfterDelete.selectFirst("div.ui.negative.message .content")
        assertNotNull(errorMessage, "Error message div not found after trying to access deleted task")
        assertEquals("Task with ID $taskId was not found", errorMessage.text())
    }

    @Test
    fun `POST - edit task successfully should update task and redirect`(): Unit = withIntegrationTest {
        val taskId = insertTestTask("Original Edit", "Original Desc")

        // Perform edit
        val editResponse = client.post("/tasks/$taskId/edit") {
            setBody(FormDataContent(Parameters.build {
                append("title", "Updated Edit Title")
                append("description", "Updated description - min 10 chars")
                append("longDescription", "Updated long desc")
                append("completed", "on") // Check the checkbox
            }))
        }
        assertEquals(HttpStatusCode.OK, editResponse.status) // After redirect
        val indexBody = editResponse.bodyAsText()
        val indexDoc = Jsoup.parse(indexBody)

        // Check success message on index page
        val successMessage = indexDoc.selectFirst("div.ui.positive.message .content")
        assertNotNull(successMessage, "Update success message not found")
        assertEquals("Task updated successfully!", successMessage.text())

        // Verify changes by going to the task's show page
        val showResponse = client.get("/tasks/$taskId")
        assertEquals(HttpStatusCode.OK, showResponse.status)
        val showBody = showResponse.bodyAsText()
        val showDoc = Jsoup.parse(showBody)

        assertEquals("Updated Edit Title", showDoc.selectFirst("h2.ui.header")?.text(), "Show page title check")
        // Check description more specifically
        val descriptionElement = showDoc.select("div.ui.grid > div.twelve.wide.column p").firstOrNull { it.text() == "Updated description - min 10 chars" }
        assertNotNull(descriptionElement, "Updated description paragraph not found or doesn't match")
        assertNotNull(showDoc.selectFirst("div.ui.label.green:contains(Completed)"), "Show page status check (Completed)")
    }

}