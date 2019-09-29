package com.phizzard.es.handlers

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.SendMessageResult
import com.phizzard.MESSAGE_CLASS
import com.phizzard.es.GET_ORDER_ENDPOINT
import com.phizzard.es.LOCATION_HEADER
import com.phizzard.es.MONGO_ID
import com.phizzard.es.PLATFORM_ID
import com.phizzard.es.extensions.getMessageRequest
import com.phizzard.es.models.ErrorBody
import com.phizzard.es.registerJacksonModules
import com.phizzard.models.PartnerPlatform
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpStatus.SC_ACCEPTED
import org.apache.http.HttpStatus.SC_CREATED
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.ws.rs.core.HttpHeaders.CONTENT_TYPE
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@ExtendWith(VertxExtension::class)
class MessageEnqueuingHandlerTest {
    init {
        registerJacksonModules()
    }

    @Test
    fun `verify sendMessage failure returns ErrorBody and 500`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        val sqs = mockk<AmazonSQS>() {
            every { sendMessage(any()) } throws Exception("Some unforeseen SQS Error :(")
        }
        val messageEnqueuingHandler = MessageEnqueuingHandler(queueUrl = "", sqsClient = sqs)

        val router = Router.router(vertx)
        router.post(ORDER_PATH)
            .handler(BodyHandler.create())
            .handler {
                launch {
                    it.put(MONGO_ID, "1")
                    it.put(PLATFORM_ID, PartnerPlatform.PSG)
                    (messageEnqueuingHandler::handle)(it)
                }
            }

        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

        val response = WebClient.create(vertx).post(LISTENING_PORT, LOCALHOST, ORDER_PATH)
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .sendAwait()

        context.verify {
            response.statusCode() shouldBe 500
            val responseBody = response.bodyAsJsonObject().mapTo(ErrorBody::class.java)
            responseBody.errors.size shouldBe 1
            responseBody.msg shouldBe ""
        }

        server.close()
        context.completeNow()
    }

    @Test
    fun `verify addMessageAttributesEntry failure returns ErrorBody and 500`(context: VertxTestContext, vertx: Vertx) =
        runBlocking {
            mockkStatic("com.phizzard.es.extensions.SQSExtensionsKt")
            every { getMessageRequest(any(), any(), any(), any()) } throws IllegalArgumentException("Please work!!!!!")

            val mockSendResult = mockk<SendMessageResult>()
            val sqs = mockk<AmazonSQS>() {
                every { sendMessage(any()) } returns mockSendResult
            }
            val messageEnqueuingHandler = MessageEnqueuingHandler(queueUrl = "", sqsClient = sqs)

            val router = Router.router(vertx)
            router.post(ORDER_PATH)
                .handler(BodyHandler.create())
                .handler {
                    launch {
                        it.put(MONGO_ID, "1")
                        it.put(PLATFORM_ID, PartnerPlatform.PSG)
                        (messageEnqueuingHandler::handle)(it)
                    }
                }

            val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

            val response = WebClient.create(vertx).post(LISTENING_PORT, LOCALHOST, ORDER_PATH)
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .sendAwait()

            context.verify {
                response.statusCode() shouldBe 500
                val responseBody = response.bodyAsJsonObject().mapTo(ErrorBody::class.java)
                responseBody.errors.size shouldBe 1
                responseBody.msg shouldBe ""
            }

            server.close()
            context.completeNow()
        }

    @Test
    fun `verify successful POST results in 201`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        val mockSendResult = mockk<SendMessageResult>()
        val sqs = mockk<AmazonSQS>() {
            every { sendMessage(any()) } returns mockSendResult
        }
        val messageEnqueuingHandler = MessageEnqueuingHandler(queueUrl = "", sqsClient = sqs)

        val router = Router.router(vertx)
        router.post(ORDER_PATH)
            .handler(BodyHandler.create())
            .handler {
                launch {
                    it.put(MESSAGE_CLASS, "PhizzardOrder")
                    it.put(MONGO_ID, "1")
                    it.put(PLATFORM_ID, PartnerPlatform.PSG)
                    (messageEnqueuingHandler::handle)(it)
                }
            }

        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

        val response = WebClient.create(vertx).post(LISTENING_PORT, LOCALHOST, ORDER_PATH)
            .putHeader("Content-Type", "application/json")
            .sendAwait()

        context.verify {
            response.statusCode() shouldBe SC_CREATED
            response.getHeader(LOCATION_HEADER) shouldBe "$GET_ORDER_ENDPOINT/1"
            val responseBody = response.bodyAsJsonObject().mapTo(OrderEnqueuingResponse::class.java)
            responseBody.orderId shouldBe "1"
        }

        server.close()
        context.completeNow()
    }

    @Test
    fun `verify successful PUT results in 202`(context: VertxTestContext, vertx: Vertx) = runBlocking {
        val mockSendResult = mockk<SendMessageResult>()
        val sqs = mockk<AmazonSQS>() {
            every { sendMessage(any()) } returns mockSendResult
        }
        val messageEnqueuingHandler = MessageEnqueuingHandler(queueUrl = "", sqsClient = sqs)

        val router = Router.router(vertx)
        router.put(ORDER_PATH)
            .handler(BodyHandler.create())
            .handler {
                launch {
                    it.put(MESSAGE_CLASS, "PhizzardOrder")
                    it.put(MONGO_ID, "1")
                    it.put(PLATFORM_ID, PartnerPlatform.PSG)
                    it.put("1", "PUT")
                    (messageEnqueuingHandler::handle)(it)
                }
            }

        val server = vertx.createHttpServer().requestHandler(router).listenAwait(LISTENING_PORT)

        val response = WebClient.create(vertx).put(LISTENING_PORT, LOCALHOST, ORDER_PATH)
            .putHeader("Content-Type", "application/json")
            .sendAwait()

        context.verify {
            response.statusCode() shouldBe SC_ACCEPTED
            response.getHeader(LOCATION_HEADER) shouldBe "$GET_ORDER_ENDPOINT/1"
            val responseBody = response.bodyAsJsonObject().mapTo(OrderEnqueuingResponse::class.java)
            responseBody.orderId shouldBe "1"
        }

        server.close()
        context.completeNow()
    }
}