package com.phizzard.es.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.EMBEDDED_MONGO_HOST
import com.phizzard.es.EMBEDDED_MONGO_PORT
import com.phizzard.es.EmbeddedMongoDBTestInstance
import com.phizzard.es.GET_ORDER_ENDPOINT
import com.phizzard.es.LISTENING_PORT
import com.phizzard.es.LOCATION_HEADER
import com.phizzard.es.MONGO_ID
import com.phizzard.es.ORDERS_COLLECTION_NAME
import com.phizzard.es.SAMPLE_MONGO_ORDER
import com.phizzard.es.SAMPLE_ORDER
import com.phizzard.es.extensions.getFindByIdQuery
import com.phizzard.es.mapper
import com.phizzard.es.models.StoredOrder
import com.phizzard.es.registerJacksonModules
import com.phizzard.models.OrderStatus
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkOperation
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.ext.mongo.bulkWriteAwait
import io.vertx.kotlin.ext.mongo.findAwait
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpStatus.SC_ACCEPTED
import org.apache.http.HttpStatus.SC_CREATED
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class OrderStorageHandlerTest {
    private val embeddedMongoDBTestInstance = EmbeddedMongoDBTestInstance()
    private val json = mapper.readValue<StoredOrder>(SAMPLE_MONGO_ORDER)

    init {
        registerJacksonModules()
    }

    @BeforeEach
    fun startMongo() {
        embeddedMongoDBTestInstance.setUp()
    }

    @AfterEach
    fun stopMongo() {
        embeddedMongoDBTestInstance.tearDown()
    }

    @Test
    fun `store order`(context: VertxTestContext, vertx: Vertx) =
        runBlocking {
            val mongoClient = MongoClient.createNonShared(
                vertx,
                JsonObject().put("host", EMBEDDED_MONGO_HOST).put("port", EMBEDDED_MONGO_PORT).put(
                    "db_name",
                    "OrderStorageHandlerTest"
                )
            )

            var mongoId: String? = null
            val enqueuingHandler = mockk<MessageEnqueuingHandler>() {
                val contextSlot = slot<RoutingContext>()
                coEvery { handle(capture(contextSlot)) } answers {
                    val routingContext = contextSlot.captured
                    mongoId = routingContext.get<String>(MONGO_ID)
                    routingContext
                        .response()
                        .setStatusCode(SC_CREATED)
                        .putHeader(LOCATION_HEADER, "$GET_ORDER_ENDPOINT/$mongoId")
                        .end(OrderEnqueuingResponse(mongoId ?: "").asJsonString())
                }
            }

            val router = Router.router(vertx)
            router.post("/:platformId/order")
                .handler(BodyHandler.create())
                .handler {
                    it.pathParams().put("platformId", "PSG")
                    launch { (OrderStorageHandler(mongoClient)::handleNewOrder)(it) }
                }
                .handler { launch { (enqueuingHandler::handle)(it) } }

            val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)
            val response = WebClient.create(vertx).post(LISTENING_PORT, "localhost", "/{platformId}/order")
                .putHeader("Content-Type", "application/json")
                .sendJsonObjectAwait(JsonObject(SAMPLE_ORDER))

            context.verify {
                response.statusCode() shouldBe SC_CREATED
                response.headers()["location"] shouldBe "/order/$mongoId"
            }

            server.close(); context.completeNow()
        }

    @Test
    fun `update order status`(context: VertxTestContext, vertx: Vertx) =
        runBlocking {
            val mongoClient = MongoClient.createNonShared(
                vertx,
                JsonObject().put("host", EMBEDDED_MONGO_HOST).put("port", EMBEDDED_MONGO_PORT).put(
                    "db_name",
                    "OrderStorageHandlerTest"
                )
            )

            mongoClient.bulkWriteAwait(
                ORDERS_COLLECTION_NAME, listOf(
                    BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("_id", "1"))
                )
            )

            val enqueuingHandler = mockk<MessageEnqueuingHandler>() {
                val contextSlot = slot<RoutingContext>()
                coEvery { handle(capture(contextSlot)) } answers {
                    val routingContext = contextSlot.captured
                    val mongoId = routingContext.get<String>(MONGO_ID)
                    routingContext
                        .response()
                        .setStatusCode(SC_ACCEPTED)
                        .putHeader(LOCATION_HEADER, "$GET_ORDER_ENDPOINT/$mongoId")
                        .end(OrderEnqueuingResponse(mongoId).asJsonString())
                }
            }
            val orderHandler = OrderStorageHandler(mongoClient)

            val router = Router.router(vertx)
            router.put("/order/:orderId")
                .handler(BodyHandler.create())
                .handler {
                    it.pathParams().put("platformId", "PSG")
                    launch {
                        (orderHandler::handleOrderStatusUpdate)(it)
                    }
                }
                .handler { launch { (enqueuingHandler::handle)(it) } }

            val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

            val response = WebClient.create(vertx).put(LISTENING_PORT, "localhost", "/order/{orderId}")
                .putHeader("Content-Type", "application/json")
                .putHeader("userId", "PSG")
                .addQueryParam("orderId", "1")
                .sendJsonObjectAwait(JsonObject().put("status", OrderStatus.APPROVED))

            val mongoOrderList = mongoClient.findAwait(ORDERS_COLLECTION_NAME, "1".getFindByIdQuery())

            context.verify {
                response.statusCode() shouldBe SC_ACCEPTED
                response.headers()["location"] shouldBe "/order/1"
                mongoOrderList[0].getString("status") shouldBe OrderStatus.APPROVED.name
            }

            server.close()
            context.completeNow()
        }
}
