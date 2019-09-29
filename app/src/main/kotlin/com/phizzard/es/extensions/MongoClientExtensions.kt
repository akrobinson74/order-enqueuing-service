package com.phizzard.es.extensions

import com.phizzard.es.MONGO_ID_FIELD_NAME
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.models.StoredOrder
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.mongo.findAwait
import io.vertx.kotlin.ext.mongo.findWithOptionsAwait
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

suspend fun MongoClient.getOrderForId(mongoId: String): List<StoredOrder> =
    findAwait(ORDERS_COLLECTION_NAME, mongoId.getFindByIdQuery()).map { it.mapTo(StoredOrder::class.java) }

suspend fun MongoClient.getOrdersForUser(
    userId: String,
    findOptions: FindOptions,
    start: Instant? = null,
    end: Instant? = null
): List<StoredOrder> =
    when {
        end != null && start != null -> {
            findWithOptionsAwait(
                ORDERS_COLLECTION_NAME,
                JsonObject().put(
                    "\$and", jsonArrayOf(
                        JsonObject().put("userId", userId),
                        JsonObject().put(
                            MONGO_ID_FIELD_NAME, JsonObject()
                                .put("\$gte", JsonObject().put("\$oid", ObjectId(Date.from(start)).toHexString()))
                                .put("\$lte", JsonObject().put("\$oid", ObjectId(Date.from(end)).toHexString()))
                        )
                    )
                ),
                findOptions
            )
                .map {
                    it.mapTo(StoredOrder::class.java)
                }
        }

        else
        -> findWithOptionsAwait(ORDERS_COLLECTION_NAME, JsonObject().put("userId", userId), findOptions).map {
            it.mapTo(StoredOrder::class.java)
        }
    }

fun String.getFindByIdQuery(): JsonObject = JsonObject().put("_id", this)
