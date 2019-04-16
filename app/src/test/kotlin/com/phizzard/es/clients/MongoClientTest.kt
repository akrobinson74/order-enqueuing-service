package com.phizzard.es.clients

import com.nhaarman.mockitokotlin2.mock
import com.phizzard.es.sampleOrderJson
import com.phizzard.es.verticles.registerJacksonModules
import io.vertx.ext.mongo.MongoClient
import org.junit.jupiter.api.Test

class MongoClientTest {
    init {
        registerJacksonModules()
    }

    private val client = mock<MongoClient>()

    @Test
    fun testStoreOrder() {
        println(sampleOrderJson)
    }
}