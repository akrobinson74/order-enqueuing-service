package com.phizzard.es.handlers

import com.phizzard.es.MONGO_ID
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.models.ErrorBody
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.streams.end
import org.slf4j.LoggerFactory

class OrderStorageHandler(private val mongoClient: MongoClient) {
    suspend fun handle(context: RoutingContext) {

        logger.debug("Entered OrderStorageHandler::handle...")

        val order = context.bodyAsJson

        mongoClient.save(ORDERS_COLLECTION_NAME, order) { result ->
            when (result.succeeded()) {
                true -> {
                    val mongoId = result.result()

                    logger.info("Order stored with id: $mongoId")
                    logger.info("Forwarding id ($mongoId) to be enqueued...")

                    context.put(MONGO_ID, mongoId).next()
                }

                else -> context.response()
                    .setStatusCode(500)
                    .end { ErrorBody(listOf(result.cause().message ?: "MongoDB Order storage error!")) }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderStorageHandler::class.java)
    }
}