package com.odemirel

import test.config.testModule
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            testModule()
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
