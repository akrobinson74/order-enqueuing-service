package com.phizzard.es.handlers

import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_UNAUTHORIZED
import org.slf4j.LoggerFactory

class ErrorHandlers {
    fun handleOpenApiValidationError(routingContext: RoutingContext) =
        routingContext.failure().message
            ?.let { message ->
                logger.error("Invalid input JSON: $message")

                routingContext.response()
                    .setStatusCode(SC_BAD_REQUEST)
                    .end(ErrorBody(errors = listOf(message)).asJsonString(prettyPrint = true))
            }
            ?: throw OpenAPIValidationException("A RoutingContext.failure must have a message attribute!")

    fun defaultErrorHandler(routingContext: RoutingContext) =
        routingContext.run {
            when (this.statusCode()) {
                SC_UNAUTHORIZED -> this.response()
                    .setStatusCode(SC_UNAUTHORIZED)
                    .end(ErrorBody(listOf("Access Denied to '${this.normalisedPath()}'")).asJsonString())
                else -> this.response()
                    .setStatusCode(this.statusCode())
                    .end()
            }
        }

    private val logger = LoggerFactory.getLogger(ErrorHandlers::class.java)
}

data class OpenAPIValidationException(val msg: String) : RuntimeException(msg)