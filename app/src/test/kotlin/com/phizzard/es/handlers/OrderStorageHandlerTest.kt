package com.phizzard.es.handlers

import com.amazonaws.services.sqs.AmazonSQS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.phizzard.es.MONGO_ID
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import org.mockito.Mockito.`when`
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

class OrderStorageHandlerTest {
    private val resultsMap = hashMapOf<String, String>()

    private val handler: (String) -> String = { json ->
        val md5HashString = BigInteger(1, MessageDigest.getInstance("MD5").digest(json.toByteArray())).toString(16)

        when (resultsMap.containsKey(md5HashString)) {
            false -> resultsMap.put(md5HashString, UUID.randomUUID().toString())
                ?: throw RuntimeException(
                    "I thought the JVM was smarter...no way this exception should ever be thrown or necessary here"
                )
            else -> resultsMap.get(md5HashString) ?: throw RuntimeException("No UUID for for json blob: $json")
        }
    }
    val resultHandler: Handler<AsyncResult<io.vertx.ext.mongo.MongoClient>> =
        Handler<AsyncResult<io.vertx.ext.mongo.MongoClient>>() {
            fun handle(ar: AsyncResult<MongoClient>) {
                io.vertx.reactivex.ext.mongo.MongoClient(ar.result())
            }
        }

    private val mockMongoClient: MongoClient = mock<MongoClient>() {
        `when` { mock.save(any(), any(), any()) } doReturn null
    }
    private val mockSqs = mock<AmazonSQS>()

    fun `verify a mongoId is propagated in the routingContext`() {
    }
}

class OrderStorageMongoResultHandler(val routingContext: RoutingContext) : Handler<AsyncResult<String>> {
    override fun handle(result: AsyncResult<String>) {
        when (result.succeeded()) {
            true -> routingContext.put(MONGO_ID, result.result())
            else -> reportMongoError(result)
        }
    }

    private fun reportMongoError(result: AsyncResult<String>) {
        val errorMsg = result.cause().message ?: "Unaccounted for MongoClient save error :(!"
        logger.error(errorMsg)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderStorageMongoResultHandler::class.java)
    }
}