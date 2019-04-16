package com.phizzard.es.handlers

import com.phizzard.es.DEFAULT_MAPPER
import com.phizzard.es.NULL_QUERY_PROJECTION
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.ORDER_ID_PATH_PARAM_NAME
import com.phizzard.es.PRETTY_PRINTING_MAPPER
import com.phizzard.es.models.ErrorBody
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_NOT_FOUND
import org.apache.http.HttpStatus.SC_OK

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
                                ErrorBody(listOf("No Order found with orderId: $it")).asJsonString(prettyPrint = true)
                            )
                }
            }
            ?: throw MissingArgumentException("No required path parameter 'orderId' was passed!")
    }
}

fun String.getFindByIdQuery(): JsonObject =
    JsonObject().put("_id", this)

fun <T> T.asJsonString(prettyPrint: Boolean = false): String = when (prettyPrint) {
    true -> PRETTY_PRINTING_MAPPER.writeValueAsString(this)
    else -> DEFAULT_MAPPER.writeValueAsString(this)
}

class MissingArgumentException(val msg: String) : RuntimeException(msg)
