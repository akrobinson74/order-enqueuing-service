package com.phizzard.es.verticles

import arrow.core.Try
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.newrelic.api.agent.NewRelic
import com.phizzard.es.CREATE_ORDER_OPERATION_ID
import com.phizzard.es.DEFAULT_HTTP_PORT
import com.phizzard.es.GET_ORDER_OPERATION_ID
import com.phizzard.es.HTTP_PORT
import com.phizzard.es.METRICS
import com.phizzard.es.OPEN_API_PATH
import com.phizzard.es.SqsConfig
import com.phizzard.es.handlers.MessageEnqueuingHandler
import com.phizzard.es.handlers.OrderRetrievalHandler
import com.phizzard.es.handlers.OrderStorageHandler
import com.phizzard.es.handlers.defaultErrorHandler
import com.phizzard.es.handlers.handleOpenApiValidationError
import com.phizzard.es.mongoConfig
import com.phizzard.es.prometheusHandler
import com.phizzard.es.requestMarker
import com.phizzard.es.sqsConfig
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
    private val sqsConfig: SqsConfig by lazy { config.sqsConfig }

    override suspend fun start() {
        logger.info("Starting BootstrapVerticle")

        registerJacksonModules()

        val routerOptions = routerFactoryOptionsOf(
            mountValidationFailureHandler = true,
            mountNotImplementedHandler = true
        )

        val mongoClient = MongoClient.createShared(vertx, JsonObject())
        val sqsClient: AmazonSQS = AmazonSQSClientBuilder.standard()
            .withCredentials(
                AWSStaticCredentialsProvider(BasicAWSCredentials(sqsConfig.accessKeyId, sqsConfig.secretAccessKey))
            )
            .withEndpointConfiguration(
                EndpointConfiguration(sqsConfig.serviceEndpoint, sqsConfig.signingRegion)
            )
            .build()

        val router = createAwait(vertx, OPEN_API_PATH)
            .addFailureHandlerByOperationId(CREATE_ORDER_OPERATION_ID, ::defaultErrorHandler)
            .addHandlerByOperationId(METRICS, ::prometheusHandler)
            .addSuspendingHandlerByOperationId(
                handler = OrderStorageHandler(mongoClient)::handle,
                operationId = CREATE_ORDER_OPERATION_ID
            )
            .addSuspendingHandlerByOperationId(
                handler = MessageEnqueuingHandler(
                    queueUrl = sqsClient.getQueueUrl(sqsConfig.queueName).queueUrl,
                    sqsClient = sqsClient
                )::handle,
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
    }
}

fun registerJacksonModules() {
    listOf(Json.mapper, Json.prettyMapper).forEach {
        it.registerModules(KotlinModule())
    }
}
