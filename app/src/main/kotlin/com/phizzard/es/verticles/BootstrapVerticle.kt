package com.phizzard.es.verticles

import arrow.core.Try
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.newrelic.api.agent.NewRelic
import com.phizzard.es.*
import com.phizzard.es.handlers.*
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.web.api.contract.openapi3.OpenAPI3RouterFactory.createAwait
import io.vertx.kotlin.ext.web.api.contract.routerFactoryOptionsOf
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class BootstrapVerticle : CoroutineVerticle() {

    private val mongoConfig: JsonObject by lazy { config.mongoConfig }

    override suspend fun start() {
        logger.info("Starting BootstrapVerticle")

        registerJacksonModules()

        val routerOptions = routerFactoryOptionsOf(
            mountValidationFailureHandler = true,
            mountNotImplementedHandler = true
        )

        val mongoClient = MongoClient.createShared(vertx, JsonObject())
        val sqsClient: AmazonSQS = AmazonSQSClientBuilder.standard()
            .build()

        val router = createAwait(vertx, OPEN_API_PATH)
            .addFailureHandlerByOperationId(CREATE_ORDER_OPERATION_ID, ::defaultErrorHandler)
            .addHandlerByOperationId(METRICS, ::prometheusHandler)
            .addSuspendingHandlerByOperationId(
                handler = OrderStorageHandler(mongoClient)::handle,
                operationId = CREATE_ORDER_OPERATION_ID
            )
            .addSuspendingHandlerByOperationId(
                handler = MessageEnqueuingHandler(sqsClient)::handle,
                operationId = CREATE_ORDER_OPERATION_ID
            )
            .addSuspendingHandlerByOperationId(
                handler = OrderRetrievalHandler(mongoClient)::handle,
                operationId = GET_ORDER_OPERATION_ID
            )
            .setValidationFailureHandler(::handleOpenApiValidationError)
            .setBodyHandler(BodyHandler.create(false))
            .setOptions(routerOptions)
            .router

        vertx.createHttpServer()
            .requestHandler(router)
            .listenAwait(config.getInteger(HTTP_PORT, DEFAULT_HTTP_PORT))

        logger.info("Started BootstrapVerticle")
    }

    private fun OpenAPI3RouterFactory.addSuspendingHandlerByOperationId(
        operationId: String,
        handler: suspend (RoutingContext) -> Unit
    ): OpenAPI3RouterFactory = addHandlerByOperationId(operationId) { routingContext ->
        val token = NewRelic.getAgent().transaction.token

        launch {
            token.link()
            Try { handler(routingContext) }
                .failed()
                .map {
                    logger.error(routingContext.requestMarker, "request failed", it.cause ?: it)
                    routingContext.fail(it)
                }
            token.expire()
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(BootstrapVerticle::class.java)
        private const val CREATE_ORDER_OPERATION_ID = "createOrder"
        private const val GET_ORDER_OPERATION_ID = "getOrder"
        private const val HEALTHCHECK = "healthCheck"
        private const val METRICS = "metrics"
        private const val OPEN_API_PATH = "/openapi.yaml"
    }
}

fun registerJacksonModules() {
    listOf(Json.mapper, Json.prettyMapper).forEach {
        it.registerModules(KotlinModule())
    }
}
