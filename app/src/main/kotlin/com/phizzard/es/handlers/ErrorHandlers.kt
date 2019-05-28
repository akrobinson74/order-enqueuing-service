package com.phizzard.es.handlers

import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.slf4j.LoggerFactory
import java.io.InvalidObjectException

private const val INVALID_ORDER_OBJECT_MSG = ""

class ErrorHandlers {
        fun handleOpenApiValidationError(routingContext: RoutingContext) =
        routingContext.failure().message
            ?.let { message ->
                logger.error("Invalid input JSON: $message")

                routingContext.response()
                    .setStatusCode(SC_BAD_REQUEST)
                    .end(ErrorBody(errors = listOf(message)).asJsonString(prettyPrint = true))
            }
            ?: throw InvalidObjectException("A RoutingContext.failure must have a message attribute!")

    fun defaultErrorHandler(routingContext: RoutingContext) =
        routingContext.failure().message
            ?.let { message ->
                logger.error("Encounter error while processing route: $message")

                routingContext.response()
                    .setStatusCode(SC_INTERNAL_SERVER_ERROR)
                    .end(ErrorBody(listOf(message)).asJsonString(prettyPrint = true))
            }
            ?: throw InvalidObjectException("A RoutingContext.failure must have a message attribute!")

    private val logger = LoggerFactory.getLogger(ErrorHandlers::class.java)
}
