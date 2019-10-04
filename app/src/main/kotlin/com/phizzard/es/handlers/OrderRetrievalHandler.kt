package com.phizzard.es.handlers

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import com.phizzard.DEFAULT_MAPPER
import com.phizzard.PRETTY_PRINTING_MAPPER
import com.phizzard.es.ORDER_ID_PATH_PARAM_NAME
import com.phizzard.es.PARTNER_ID_PATH_PARAM_NAME
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
        val platform = context.extractPartnerPlatform()
        val (batchSize, limit, skip) = getHandleAllRequestParams(context)
        val findOptions = buildFindOptions(batchSize, limit, skip)

        val (before, after) = context.request().getParam("before") to
            context.request().getParam("after")
        val afterDateTime = if (!after.isNullOrEmpty()) Instant.from(ISO_INSTANT.parse(after)) else null
        val beforeDateTime = if (!before.isNullOrEmpty()) Instant.from(ISO_INSTANT.parse(before)) else null

        val orders = getOrdersForUserWithParams(afterDateTime, beforeDateTime, findOptions, platform)

        logger.info("Obtained {} order objects", orders.size)

        context.response()
            .setStatusCode(SC_OK)
            .end(orders.asJsonString(true))
    }
        .handleGetAllErrors(context)

    private suspend fun getOrdersForUserWithParams(
        afterDateTime: Instant?,
        beforeDateTime: Instant?,
        findOptions: FindOptions,
        platform: PartnerPlatform
    ): List<StoredOrder> = mongoClient.getOrdersForUser(
        userId = platform.name,
        findOptions = findOptions,
        start = afterDateTime,
        end = beforeDateTime
    )

    private fun getHandleAllRequestParams(context: RoutingContext): Triple<String?, String?, String?> =
        listOf("batchSize", "limit", "skip").map { context.request().getParam(it) ?: null }.toTriple()

    private fun buildFindOptions(
        batchSize: String?,
        limit: String?,
        skip: String?
    ): FindOptions = FindOptions()
        .setBatchSize(batchSize?.toInt() ?: DEFAULT_BATCH_SIZE)
        .setLimit(limit?.toInt() ?: DEFAULT_RESULTS_LIMIT)
        .setSkip(skip?.toInt() ?: DEFAULT_RESULTS_TO_SKIP)

    private fun handleFindAwaitResult(
        context: RoutingContext,
        objectList: List<StoredOrder>,
        expectedResults: Int = 1
    ) = when {
        objectList.isNotEmpty() && objectList.size == expectedResults -> context.response()
            .setStatusCode(SC_OK)
            .end(if (expectedResults == 1) objectList[0].asJsonString() else objectList.asJsonString())

        else -> context.response()
            .setStatusCode(SC_NOT_FOUND)
            .end(
                ErrorBody(listOf("No Order found with orderId: ${context.pathParams().get(ORDER_ID_PATH_PARAM_NAME)}"))
                    .asJsonString(prettyPrint = true)
            )
    }

    companion object {
        private val logger = getLogger<OrderRetrievalHandler>()
    }
}

fun <T> List<T>.toTriple(): Triple<T, T, T> = takeUnless { it.isEmpty() }
    ?.let { it[0] to it[1] toTriple it[2] }
    ?: throw IllegalArgumentException("List<T> contains fewer than 3 elements!")

infix fun <T> Pair<T, T>.toTriple(b: T) = Triple(this.first, this.second, b)

fun <T> T.asJsonString(prettyPrint: Boolean = false): String = when (prettyPrint) {
    true -> PRETTY_PRINTING_MAPPER.writeValueAsString(this)
    else -> DEFAULT_MAPPER.writeValueAsString(this)
}

private fun Either<Throwable, Unit>.handleGetAllErrors(context: RoutingContext) = mapLeft {
    getLogger<OrderRetrievalHandler>().error("Retreive all orders failed: {}", it.localizedMessage)
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
    .getOrElse {}

suspend fun RoutingContext.extractPartnerPlatform(): PartnerPlatform = Either.catch {
    val partnerIdPathParam = pathParams().get(PARTNER_ID_PATH_PARAM_NAME)?.toUpperCase() ?: "platformId Not passed"
    PartnerPlatform.valueOf(partnerIdPathParam)
}
    .getOrHandle { throw MissingArgumentException("Missing or invalid partnerId: ${it.localizedMessage}") }

class MissingArgumentException(val msg: String) : RuntimeException(msg)
