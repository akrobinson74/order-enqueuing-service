package com.phizzard.es.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

private const val INVALID_ORDER_OBJECT_MSG = ""

fun handleOpenApiValidationError(routingContext: RoutingContext): Unit =
    routingContext.response()
        .setStatusCode(SC_BAD_REQUEST)
        .end(
            jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                ErrorBody(
                    errors = listOf(routingContext.failure().message ?: ""),
                    msg = INVALID_ORDER_OBJECT_MSG
                )
            )
        )

fun defaultErrorHandler(routingContext: RoutingContext): Unit =
    routingContext.response()
        .setStatusCode(SC_INTERNAL_SERVER_ERROR)
        .end(
            jacksonObjectMapper().writeValueAsString(ErrorBody(listOf(routingContext.failure().message ?: "")))
        )
