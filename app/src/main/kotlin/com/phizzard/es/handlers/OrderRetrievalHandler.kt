package com.phizzard.es.handlers

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import com.phizzard.es.DEFAULT_MAPPER
import com.phizzard.es.ORDER_ID_PATH_PARAM_NAME
import com.phizzard.es.PARTNER_ID_PATH_PARAM_NAME
import com.phizzard.es.PRETTY_PRINTING_MAPPER
import com.phizzard.es.extensions.getLogger
import com.phizzard.es.extensions.getOrderForId
import com.phizzard.es.extensions.getOrdersForUser
import com.phizzard.es.models.ErrorBody
import com.phizzard.es.models.StoredOrder
import com.phizzard.models.PartnerPlatform
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_OK
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

const val DEFAULT_BATCH_SIZE = 100
const val DEFAULT_RESULTS_LIMIT = 100
const val DEFAULT_RESULTS_TO_SKIP = 0

class OrderRetrievalHandler(private val mongoClient: MongoClient) {

    suspend fun handleGetOrderById(context: RoutingContext) = Either.catch {
        context.pathParams()
            .get(ORDER_ID_PATH_PARAM_NAME)
            ?.let {
                val orderList = mongoClient.getOrderForId(it)
                handleFindAwaitResult(context = context, objectList = orderList)
            }
            ?: throw MissingArgumentException("No required path parameter 'orderId' was passed!")
    }
        .mapLeft {
            logger.error(
                "Retrieve order for _id ({}) failed: {}",
                context.pathParams().get(ORDER_ID_PATH_PARAM_NAME),
                it.localizedMessage
            )
        }
        .getOrElse { }

    suspend fun handleAllOrdersForUserId(context: RoutingContext) = Either.catch {
        val platform = Either.catch {
            PartnerPlatform.valueOf(
                context.pathParams().get(PARTNER_ID_PATH_PARAM_NAME)?.toUpperCase() ?: "platformId Not passed"
            )
        }
            .getOrHandle {
                throw MissingArgumentException("Missing or invalid partnerId: ${it.localizedMessage}")
            }

        val batchSize = context.request().getParam("batchSize")
        val limit = context.request().getParam("limit")
        val skip = context.request().getParam("skip")
        val findOptions = FindOptions()
            .setBatchSize(batchSize?.toInt() ?: DEFAULT_BATCH_SIZE)
            .setLimit(limit?.toInt() ?: DEFAULT_RESULTS_LIMIT)
            .setSkip(skip?.toInt() ?: DEFAULT_RESULTS_TO_SKIP)

        val (before, after) =
            context.request().getParam("before") to context.request().getParam("after")
        val afterDateTime = if (!after.isNullOrEmpty()) Instant.from(ISO_INSTANT.parse(after)) else null
        val beforeDateTime = if (!before.isNullOrEmpty()) Instant.from(ISO_INSTANT.parse(before)) else null

        val orders = mongoClient.getOrdersForUser(
            userId = platform.name,
            findOptions = findOptions,
            start = afterDateTime,
            end = beforeDateTime
        )

        logger.info("Obtained {} order objects", orders.size)

        context.response()
            .setStatusCode(SC_OK)
            .end(orders.asJsonString(true))
    }
        .mapLeft {
            logger.error("Retreive all orders failed: {}", it.localizedMessage)

            when {
                it is MissingArgumentException -> HttpStatus.SC_BAD_REQUEST to it.msg
                else -> HttpStatus.SC_INTERNAL_SERVER_ERROR to "Please contact backend@phizzard.com about this error."
            }
                .let {
                    context.response()
                        .setStatusCode(it.first)
                        .end(it.second)
                }
        }
        .getOrElse { }

    private fun handleFindAwaitResult(
        context: RoutingContext,
        objectList: List<StoredOrder>,
        expectedResults: Int = 1
    ) =
        when {
            objectList.isNotEmpty() && objectList.size == expectedResults ->
                context.response()
                    .setStatusCode(SC_OK)
                    .end(if (expectedResults == 1) objectList[0].asJsonString() else objectList.asJsonString())
            else ->
                context.response()
                    .setStatusCode(SC_NOT_FOUND)
                    .end(
                        ErrorBody(
                            listOf("No Order found with orderId: ${context.pathParams().get(ORDER_ID_PATH_PARAM_NAME)}")
                        )
                            .asJsonString(prettyPrint = true)
                    )
        }

    companion object {
        private val logger = getLogger<OrderRetrievalHandler>()
    }
}

fun <T> T.asJsonString(prettyPrint: Boolean = false): String = when (prettyPrint) {
    true -> PRETTY_PRINTING_MAPPER.writeValueAsString(this)
    else -> DEFAULT_MAPPER.writeValueAsString(this)
}

class MissingArgumentException(val msg: String) : RuntimeException(msg)
