package com.phizzard.es.clients

import com.nhaarman.mockitokotlin2.mock
import com.phizzard.es.sampleOrderJson
import io.vertx.ext.mongo.MongoClient
import org.junit.jupiter.api.Test

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoClientTest {
    private val client = mock<MongoClient>()
//    private val mongo = MongoContainer.instance
//
//    private val mongoDAO = MongoClient(mongo.host, mongo.port)

    @Test
    fun testStoreOrder() {
        println(sampleOrderJson)
    }
}