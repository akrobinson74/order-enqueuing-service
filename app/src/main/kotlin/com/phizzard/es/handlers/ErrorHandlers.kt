package com.phizzard.es.handlers

import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_BAD_REQUEST
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR

private const val INVALID_ORDER_OBJECT_MSG = ""

fun handleOpenApiValidationError(routingContext: RoutingContext) =
    routingContext.response()
        .setStatusCode(SC_BAD_REQUEST)
        .end(
            ErrorBody(
                errors = listOf(routingContext.failure().message ?: ""),
                msg = INVALID_ORDER_OBJECT_MSG
            )
                .asJsonString(prettyPrint = true)
        )

fun defaultErrorHandler(routingContext: RoutingContext) =
    routingContext.response()
        .setStatusCode(SC_INTERNAL_SERVER_ERROR)
        .end(
            ErrorBody(listOf(routingContext.failure().message ?: "")).asJsonString(prettyPrint = true)
        )
