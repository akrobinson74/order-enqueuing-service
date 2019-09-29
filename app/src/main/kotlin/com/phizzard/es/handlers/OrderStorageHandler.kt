package com.phizzard.es.handlers

import arrow.core.Either
import com.mongodb.MongoException
import com.phizzard.MESSAGE_CLASS
import com.phizzard.es.MONGO_ID
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.PARTNER_ID_PATH_PARAM_NAME
import com.phizzard.es.PLATFORM_ID
import com.phizzard.es.extensions.getFindByIdQuery
import com.phizzard.es.models.ErrorBody
import com.phizzard.models.PartnerPlatform
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.ext.mongo.saveAwait
import io.vertx.kotlin.ext.mongo.updateCollectionAwait
import org.apache.http.HttpStatus
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter

class OrderStorageHandler(private val mongoClient: MongoClient) {
    suspend fun handleNewOrder(context: RoutingContext) {
        logger.debug("Entered OrderStorageHandler::handle...")

        val order = context.bodyAsJson

        Either.catch {
            val platformId =
                PartnerPlatform.valueOf(
                    context.pathParams().get(PARTNER_ID_PATH_PARAM_NAME)?.toUpperCase() ?: "platformId Not passed"
                )

            mongoClient.saveAwait(ORDERS_COLLECTION_NAME, order.put("userId", platformId.name))
                ?.let { mongoId ->
                    logger.info("Order stored with id: $mongoId")
                    logger.info("Forwarding id ($mongoId) to be enqueued...")

                    context.put(MONGO_ID, mongoId)
                        .put(PLATFORM_ID, platformId)
                        .put(MESSAGE_CLASS, "PhizzardOrder")
                        .next()
                }
                ?: throw MongoException("Unable to store Order: ${context.bodyAsString}")
        }
            .mapLeft { throwable ->
                context.response()
                    .setStatusCode(SC_INTERNAL_SERVER_ERROR)
                    .end { ErrorBody(listOf(throwable.localizedMessage ?: "MongoDB Order storage error!")) }
            }
    }

    suspend fun handleOrderStatusUpdate(context: RoutingContext) {
        val orderId = context.request().getParam("orderId")
        val putQuery = context.bodyAsJson

        Either.catch {
            val platformId =
                PartnerPlatform.valueOf(
                    context.pathParams().get(PARTNER_ID_PATH_PARAM_NAME)?.toUpperCase() ?: "platformId Not passed"
                )

            val updateResult =
                mongoClient.updateCollectionAwait(
                    ORDERS_COLLECTION_NAME,
                    orderId.getFindByIdQuery(),
                    JsonObject().put("\$set", putQuery)
                )
            when {
                updateResult?.docModified == 1L -> context.put(MONGO_ID, orderId)
                    .put(PLATFORM_ID, platformId)
                    .put(MESSAGE_CLASS, "PhizzardOrder")
                    .next()
                else -> context.response().setStatusCode(SC_NOT_FOUND)
            }
        }
            .mapLeft {
                val msg = MessageFormatter.arrayFormat(
                    "Attempt to apply query ({}) to object id ({}) failed: {}",
                    arrayOf<String>(
                        putQuery.encodePrettily(),
                        orderId,
                        it.localizedMessage
                    )
                ).message

                logger.error(msg)

                context.response()
                    .setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .end(ErrorBody(errors = listOf(msg)).asJsonString())
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderStorageHandler::class.java)
    }
}