package com.phizzard.es.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.models.ErrorBody
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_OK

private const val ORDER_ID_PATH_PARAM_NAME = "orderId"
private val NULL_QUERY_PROJECTION: JsonObject? = null

class OrderRetrievalHandler(val mongoClient: MongoClient) {

    suspend fun handle(context: RoutingContext) {
        context.pathParams()
            .get(ORDER_ID_PATH_PARAM_NAME)
            ?.let {
                mongoClient.findOne(ORDERS_COLLECTION_NAME, it.getFindByIdQuery(), NULL_QUERY_PROJECTION) { result ->
                    result.succeeded()
                        .takeIf { it }
                        ?.let {
                            context.response()
                                .setStatusCode(SC_OK)
                                .end(result.result().encode())
                        }
                        ?: context.response()
                            .setStatusCode(SC_NOT_FOUND)
                            .end(
                                ErrorBody(listOf("No Order found with orderId: $it")).asJsonString()
                            )
                }
            }
            ?: throw RuntimeException("No required path parameter 'orderId' was passed!")
    }
}

fun String.getFindByIdQuery(): JsonObject =
    JsonObject().put("_id", this)

fun <T> T.asJsonString(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)