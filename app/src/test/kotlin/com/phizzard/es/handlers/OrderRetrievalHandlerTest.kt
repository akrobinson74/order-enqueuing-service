package com.phizzard.es.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.ADAMS_DOB_EPOCH_SECONDS
import com.phizzard.es.EMBEDDED_MONGO_HOST
import com.phizzard.es.EMBEDDED_MONGO_PORT
import com.phizzard.es.EmbeddedMongoDBTestInstance
import com.phizzard.es.FIVE
import com.phizzard.es.FOUR
import com.phizzard.es.LISTENING_PORT
import com.phizzard.es.LOCALHOST
import com.phizzard.es.MONGO_EXTENSION_CLASS
import com.phizzard.es.MONGO_ID_FIELD_NAME
import com.phizzard.es.ONE
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.ORDERS_PATH
import com.phizzard.es.ORDER_ID_PATH_PARAM_NAME
import com.phizzard.es.ORDER_PATH
import com.phizzard.es.PLATFORM_ID
import com.phizzard.es.SAMPLE_MONGO_ORDER
import com.phizzard.es.THREE
import com.phizzard.es.TRANSACTION_DATE
import com.phizzard.es.TWO
import com.phizzard.es.deltaDaysList
import com.phizzard.es.extensions.getOrderForId
import com.phizzard.es.extensions.getOrdersForUser
import com.phizzard.es.mapper
import com.phizzard.es.models.StoredOrder
import com.phizzard.es.registerJacksonModules
import com.phizzard.models.PartnerPlatform.PSG
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkOperation
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.impl.MongoClientImpl
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.ext.mongo.bulkWriteAwait
import io.vertx.kotlin.ext.mongo.saveAwait
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.apache.http.HttpStatus.SC_OK
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.ws.rs.core.HttpHeaders.CONTENT_TYPE
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@ExtendWith(VertxExtension::class)
class OrderRetrievalHandlerTest {
    private val embeddedMongoDBTestInstance = EmbeddedMongoDBTestInstance()
    private val json = mapper.readValue<StoredOrder>(SAMPLE_MONGO_ORDER)

    init {
        registerJacksonModules()
    }

    @AfterEach
    fun cleanUp() {
        embeddedMongoDBTestInstance.tearDown()
    }

    @BeforeEach
    fun prepare() {
        embeddedMongoDBTestInstance.setUp()
    }

    @Test
    fun `handle returns StoredOrder if _id exists`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        mockkStatic(MONGO_EXTENSION_CLASS)
        val mongoClient = mockk<MongoClientImpl>() {
            coEvery { getOrderForId(eq("0")) } returns emptyList()
            coEvery { getOrderForId(eq("1")) } returns listOf(json)
        }

        val router =
            router(allOrders = false, mongoClient = mongoClient, path = "$ORDER_PATH/:orderId", vertx = vertx)
        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
        val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, "$ORDER_PATH/1")
            .addQueryParam(ORDER_ID_PATH_PARAM_NAME, "1")
            .putHeadersAndSend()

        context.verify { response.statusCode() shouldBe SC_OK }

        server.close()
        context.completeNow()
    }

    @Test
    fun `handleAll requires a userId header - 400`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        val router =
            router(mongoClient = mockk<MongoClient>(), path = ORDERS_PATH, vertx = vertx, withoutPlatform = true)
        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
        val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, ORDERS_PATH)
            .putHeadersAndSend()

        context.verify {
            response.statusCode() shouldBe SC_BAD_REQUEST
            response.bodyAsString() shouldMatch "^.*Missing or invalid partnerId.+$".toRegex()
        }
        server.close(); context.completeNow()
    }

    @Test
    fun `exception in handleAll results in - 500`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        mockkStatic(MONGO_EXTENSION_CLASS)
        val mongoClient = mockk<MongoClient>() {
            coEvery { getOrdersForUser(any(), any(), any(), any()) } throws NullPointerException("mocked exception...")
        }
        val router = router(mongoClient = mongoClient, path = ORDERS_PATH, vertx = vertx)
        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
        val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, ORDERS_PATH)
            .putHeadersAndSend()

        context.verify { response.statusCode() shouldBe SC_INTERNAL_SERVER_ERROR }
        server.close(); context.completeNow()
    }

    @Test
    fun `handleAll returns all objects for the curent user`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        mockkStatic(MONGO_EXTENSION_CLASS)
        val mongoClient = mockk<MongoClient>() {
            coEvery { getOrdersForUser(any(), any(), any(), any()) } returns listOf(json, json)
        }
        val router = router(mongoClient = mongoClient, path = ORDERS_PATH, vertx = vertx)
        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
        val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, ORDERS_PATH)
            .putHeadersAndSend()

        context.verify {
            response.statusCode() shouldBe SC_OK
            mapper.readValue<List<StoredOrder>>(response.body().bytes).size shouldBe 2
        }
        server.close(); context.completeNow()
    }

    //    @Tag("integration")
    @Test
    fun `verify handleAll results uses findOptions`(context: VertxTestContext, vertx: Vertx) =
        runBlocking {
            val mongoClient = MongoClient.createNonShared(
                vertx,
                JsonObject().put("host", EMBEDDED_MONGO_HOST).put("port", EMBEDDED_MONGO_PORT)
                    .put("db_name", "OrderStorageHandlerTest")
            )
            insertTestOrdersWithId(mongoClient)
            val router = router(mongoClient = mongoClient, path = ORDERS_PATH, vertx = vertx)
            val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
            val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, ORDERS_PATH)
                .addQueryParam("batchSize", "1")
                .addQueryParam("limit", "1")
                .addQueryParam("skip", "4")
                .putHeadersAndSend()

            context.verify {
                response.statusCode() shouldBe SC_OK
                val orderList = mapper.readValue<List<StoredOrder>>(response.body().bytes)
                orderList.size shouldBe 1
                orderList[0].approvalCode shouldBe "twelve"
            }
            server.close(); context.completeNow()
        }

    //    @Tag("integration")
    @Test
    fun `search by date range`(context: VertxTestContext, vertx: Vertx) =
        runBlocking {
            val mongoClient = MongoClient.createNonShared(
                vertx,
                JsonObject()
                    .put("db_name", "OrderStorageHandlerTest")
                    .put("host", EMBEDDED_MONGO_HOST)
                    .put("port", EMBEDDED_MONGO_PORT)
                    .put("useObjectId", false)
            )

            val jsonObjectsList = JsonObject(SAMPLE_MONGO_ORDER).cloneJsonObjectWithIdAndDates(
                startingUtcInstant = Instant.ofEpochMilli(ADAMS_DOB_EPOCH_SECONDS),
                deltaDaysList = deltaDaysList
            )
            jsonObjectsList.forEach { mongoClient.saveAwait(ORDERS_COLLECTION_NAME, it) }

            val router = router(mongoClient = mongoClient, path = ORDERS_PATH, vertx = vertx)
            val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

            val response = WebClient.create(vertx).get(LISTENING_PORT, LOCALHOST, ORDERS_PATH)
                .addQueryParam("after", "1973-12-31T23:59:59Z")
                .addQueryParam("before", "2000-01-01T00:00:01Z")
                .addQueryParam("limit", "3")
                .addQueryParam("batchSize", "1000")
                .addQueryParam("skip", "1")
                .putHeadersAndSend()

            context.verify {
                response.statusCode() shouldBe SC_OK
                val orderList = mapper.readValue<List<StoredOrder>>(response.body().bytes)
                orderList.size shouldBe 2
                // Would perhaps be more reliable with a TestContainer
                orderList[0].transactionDate shouldNotBe jsonObjectsList[0].getString(TRANSACTION_DATE)
                orderList[0].transactionDate shouldBe jsonObjectsList[1].getInstant(TRANSACTION_DATE)
            }
            server.close(); context.completeNow()
        }

    private suspend fun insertTestOrdersWithId(mongoClient: MongoClient) {
        mongoClient.bulkWriteAwait(
            ORDERS_COLLECTION_NAME, listOf(
                BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", ONE)),
                BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", TWO)),
                BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", THREE)),
                BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", FOUR)),
                BulkOperation.createInsert(
                    JsonObject(SAMPLE_MONGO_ORDER).put("id", FIVE).put(
                        "approvalCode",
                        "twelve"
                    )
                )
            )
        )
    }
}

fun generateObjectIdAsDatedHexString(date: Date): String = ObjectId(date).toHexString()

suspend fun <T> HttpRequest<T>.putHeadersAndSend() = this.putHeader(CONTENT_TYPE, APPLICATION_JSON).sendAwait()

fun JsonObject.buildObjectIdQuery(hexString: String) = this.put("\$oid", hexString)

fun JsonObject.cloneJsonObjectWithIdAndDates(
    startingUtcInstant: Instant,
    deltaDaysList: List<Long>
): List<JsonObject> =
    deltaDaysList.map { daysToAdd ->
        val adjustedInstant = startingUtcInstant.plus(daysToAdd, ChronoUnit.DAYS)
        val objectIdQuery =
            JsonObject().buildObjectIdQuery(generateObjectIdAsDatedHexString(Date.from(adjustedInstant)))

        this.copy()
            .put(MONGO_ID_FIELD_NAME, objectIdQuery)
            .put(TRANSACTION_DATE, adjustedInstant.toString())
    }

fun CoroutineScope.router(
    allOrders: Boolean = true,
    mongoClient: MongoClient,
    path: String,
    vertx: Vertx,
    withoutPlatform: Boolean = false
) = Router.router(vertx).let {
    it.get(path)
        .handler(BodyHandler.create())
        .handler {
            if (!withoutPlatform) it.pathParams().put(PLATFORM_ID, PSG.name)
            launch {
                val orderHandler = OrderRetrievalHandler(mongoClient)
                val handler =
                    if (allOrders) orderHandler::handleAllOrdersForUserId
                    else orderHandler::handleGetOrderById

                handler(it)
            }
        }
    it
}