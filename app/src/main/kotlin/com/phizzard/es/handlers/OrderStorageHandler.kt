package com.phizzard.es.handlers

import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_CREATED
import org.slf4j.LoggerFactory

class OrderStorageHandler {
    suspend fun handle(context: RoutingContext) {

        logger.info("Entered OrderStorageHandler::handle...")

        context.response()
            .setStatusCode(SC_CREATED)
            .end("You are awesome!")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderStorageHandler::class.java)
    }
}