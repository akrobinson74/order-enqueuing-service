package com.phizzard.es.handlers

import arrow.core.Try
import arrow.core.getOrElse
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.MessageAttributeValue
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.phizzard.es.MONGO_ID
import com.phizzard.es.ORDER_ENQUEUING_SERVICE
import com.phizzard.es.SENDER
import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.apache.http.HttpStatus.SC_OK
import org.slf4j.LoggerFactory

class MessageEnqueuingHandler(val sqsClient: AmazonSQS) {

    suspend fun handle(routingContext: RoutingContext) {

        logger.info("Entered MessageEnqueuingHandler::handle...")

        val mongoId = routingContext.get<String>(MONGO_ID)

        Try {
            val msgRequest =
                SendMessageRequest()
                    .withMessageBody(OrderMessage(mongoId).asJsonString())
                    .withMessageAttributes(
                        mapOf(
                            SENDER to MessageAttributeValue().withStringValue(ORDER_ENQUEUING_SERVICE)
                        )
                    )

            logger.debug("Trying to enqueuing message with id: $mongoId")
            sqsClient.sendMessage(msgRequest)
            logger.debug("Succeeded in enqueuing message with id: $mongoId")
        }
            .toEither()
            .mapLeft {
                val message =
                    it.cause?.message ?: "Unable to write message metadata to queue for indeterminate reason :(!"

                logger.error("Failed to enqueue message: $message")

                routingContext.response()
                    .setStatusCode(SC_INTERNAL_SERVER_ERROR)
                    .end(
                        ErrorBody(listOf(message)).asJsonString()
                    )
            }
            .getOrElse {
                routingContext.response().statusCode = SC_OK
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderStorageHandler::class.java)
    }
}

data class OrderMessage(val mongoId: String)