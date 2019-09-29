package com.phizzard.es.handlers

import io.vertx.junit5.VertxExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class OrderRetrievalHandlerIntegrationTest {
//    private val embeddedMongoDBTestInstance = EmbeddedMongoDBTestInstance()
//    private val json = mapper.readValue<StoredOrder>(SAMPLE_MONGO_ORDER)
//
//    init {
//        registerJacksonModules()
//    }
//
//    @Test
//    fun `handle returns StoredOrder if _id exists`(context: VertxTestContext, vertx: Vertx) = runBlocking {
//        mockkStatic("com.phizzard.es.extensions.MongoClientExtensionsKt")
//
//        val mongoClient = mockk<MongoClientImpl>() {
//            coEvery { getOrderForId(eq("0")) } returns emptyList()
//            coEvery { getOrderForId(eq("1")) } returns listOf(json)
//        }
//
//        val orderHandler = OrderRetrievalHandler(mongoClient)
//
//        val router = Router.router(vertx)
//        router.get("/order/:orderId")
//            .handler(BodyHandler.create())
//            .handler {
//                launch {
//                    (orderHandler::handleGetOrderById)(it)
//                }
//            }
//
//        val server = vertx.createHttpServer().requestHandler(router).listenAwait(54321)
//
//        val response = WebClient.create(vertx).get(54321, "localhost", "/order/1")
//            .addQueryParam(ORDER_ID_PATH_PARAM_NAME, "1")
//            .putHeader("Content-Type", "application/json")
//            .sendAwait()
//
//        context.verify {
//            response.statusCode() shouldBe 200
//        }
//
//        server.close()
//        context.completeNow()
//    }
//
//    @Test
//    fun `handleAll returns all objects for the curent user`(context: VertxTestContext, vertx: Vertx) =
//        runBlocking {
//            mockkStatic("com.phizzard.es.extensions.MongoClientExtensionsKt")
//            val mongoClient = mockk<MongoClient>() {
//                coEvery { getOrdersForUser(any(), any(), any(), any()) } returns listOf(json, json)
//            }
//
//            val orderHandler = OrderRetrievalHandler(mongoClient)
//
//            val router = Router.router(vertx)
//            router.get("/orders")
//                .handler(BodyHandler.create())
//                .handler {
//                    launch {
//                        (orderHandler::handleAllOrdersForUserId)(it)
//                    }
//                }
//
//            val server = vertx.createHttpServer().requestHandler(router).listenAwait(54321)
//
//            val response = WebClient.create(vertx).get(54321, "localhost", "/orders")
//                .putHeader("Content-Type", "application/json")
//                .putHeader("userId", "PSG")
//                .sendAwait()
//
//            context.verify {
//                response.statusCode() shouldBe 200
//                val orderList = mapper.readValue<List<StoredOrder>>(response.body().bytes)
//                orderList.size shouldBe 2
//            }
//
//            server.close()
//            context.completeNow()
//        }
//
//    @Tag("integration")
//    @Test
//    fun `verify handleAll results uses findOptions`(context: VertxTestContext, vertx: Vertx) =
//        runBlocking {
//            embeddedMongoDBTestInstance.setUp()
//            val mongoClient = MongoClient.createNonShared(
//                vertx,
//                JsonObject().put("host", EMBEDDED_MONGO_HOST).put("port", EMBEDDED_MONGO_PORT).put(
//                    "db_name",
//                    "OrderStorageHandlerTest"
//                )
//            )
//            mongoClient.bulkWriteAwait(
//                ORDERS_COLLECTION_NAME, listOf(
//                    BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", 1)),
//                    BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", 2)),
//                    BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", 3)),
//                    BulkOperation.createInsert(JsonObject(SAMPLE_MONGO_ORDER).put("id", 4)),
//                    BulkOperation.createInsert(
//                        JsonObject(SAMPLE_MONGO_ORDER).put("id", 5).put(
//                            "approvalCode",
//                            "twelve"
//                        )
//                    )
//                )
//            )
//
//            val orderHandler = OrderRetrievalHandler(mongoClient)
//
//            val router = Router.router(vertx)
//            router.get("/orders")
//                .handler(BodyHandler.create())
//                .handler {
//                    launch {
//                        (orderHandler::handleAllOrdersForUserId)(it)
//                    }
//                }
//
//            val server = vertx.createHttpServer().requestHandler(router).listenAwait(54321)
//
//            val response = WebClient.create(vertx).get(54321, "localhost", "/orders")
//                .putHeader("Content-Type", "application/json")
//                .putHeader("userId", "PSG")
//                .addQueryParam("batchSize", "1")
//                .addQueryParam("limit", "1")
//                .addQueryParam("skip", "4")
//                .sendAwait()
//
//            context.verify {
//                response.statusCode() shouldBe 200
//                val orderList = mapper.readValue<List<StoredOrder>>(response.body().bytes)
//                orderList.size shouldBe 1
//                orderList[0].approvalCode shouldBe "twelve"
//            }
//
//            embeddedMongoDBTestInstance.tearDown()
//            server.close()
//            context.completeNow()
//        }
//
//    @Tag("integration")
//    @Test
//    fun `search by date range`(context: VertxTestContext, vertx: Vertx) =
//        runBlocking {
//            embeddedMongoDBTestInstance.setUp()
//            val mongoClient = MongoClient.createNonShared(
//                vertx,
//                JsonObject()
//                    .put("db_name", "OrderStorageHandlerTest")
//                    .put("host", EMBEDDED_MONGO_HOST)
//                    .put("port", EMBEDDED_MONGO_PORT)
//                    .put("useObjectId", false)
//            )
//
//            val startOfAdamsEra = Instant.ofEpochMilli(143398800000L)
//
//            val jsonObjectsList = JsonObject(SAMPLE_MONGO_ORDER).cloneJsonObjectWithIdAndDates(
//                startingUtcInstant = startOfAdamsEra,
//                deltaDaysList = listOf(0L, 3653L, 7305L, 10968L, 14620L)
//            )
//
//            jsonObjectsList.forEach {
//                System.out.println("Save object: ${mongoClient.saveAwait(ORDERS_COLLECTION_NAME, it)}")
//            }
//
//            val orderHandler = OrderRetrievalHandler(mongoClient)
//
//            val router = Router.router(vertx)
//            router.get("/orders")
//                .handler(BodyHandler.create())
//                .handler {
//                    launch {
//                        (orderHandler::handleAllOrdersForUserId)(it)
//                    }
//                }
//
//            val server = vertx.createHttpServer().requestHandler(router).listenAwait(54321)
//
//            val response = WebClient.create(vertx).get(54321, "localhost", "/orders")
//                .putHeader("Content-Type", "application/json")
//                .putHeader("userId", "PSG")
//                .addQueryParam("after", "1973-12-31T23:59:59Z")
//                .addQueryParam("before", "2000-01-01T00:00:01Z")
//                .addQueryParam("limit", "3")
//                .addQueryParam("batchSize", "1000")
//                .addQueryParam("skip", "1")
//                .sendAwait()
//
//            context.verify {
//                response.statusCode() shouldBe 200
//                val orderList = mapper.readValue<List<StoredOrder>>(response.body().bytes)
//                orderList.size shouldBe 2
//                // Would perhaps be more reliable with a TestContainer
//                orderList[0].transactionDate shouldNotBe jsonObjectsList[0].getString("transactionDate")
//                orderList[0].transactionDate shouldBe jsonObjectsList[1].getInstant("transactionDate")
//            }
//
//            embeddedMongoDBTestInstance.tearDown()
//            server.close()
//            context.completeNow()
//        }
//
//    fun JsonObject.cloneJsonObjectWithIdAndDates(
//        startingUtcInstant: Instant,
//        deltaDaysList: List<Long>
//    ): List<JsonObject> =
//        deltaDaysList.map { daysToAdd ->
//            val adjustedInstant = startingUtcInstant.plus(daysToAdd, ChronoUnit.DAYS)
//            val objectIdQuery =
//                JsonObject().buildObjectIdQuery(generateObjectIdAsDatedHexString(Date.from(adjustedInstant)))
//
//            this.copy()
//                .put(MONGO_ID_FIELD_NAME, objectIdQuery)
//                .put("transactionDate", adjustedInstant.toString())
//        }
//
//    fun JsonObject.buildObjectIdQuery(hexString: String) = this.put("\$oid", hexString)
//
//    fun generateObjectIdAsDatedHexString(date: Date): String = ObjectId(date).toHexString()
}
