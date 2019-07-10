package com.phizzard.es.handlers

import arrow.core.Try
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.MessageAttributeValue
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.phizzard.es.GET_ORDER_ENDPOINT
import com.phizzard.es.LOCATION_HEADER
import com.phizzard.es.MONGO_ID
import com.phizzard.es.ORDER_ENQUEUING_SERVICE
import com.phizzard.es.SENDER
import com.phizzard.es.models.ErrorBody
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.slf4j.LoggerFactory

class MessageEnqueuingHandler(
    val queueUrl: String,
    val sqsClient: AmazonSQS
) {
    suspend fun handle(routingContext: RoutingContext) {

        logger.info("Entered MessageEnqueuingHandler::handle...")

        val mongoId = routingContext.get<String>(MONGO_ID)

        Try {
            val body = OrderMessage(mongoId).asJsonString()
            val msgRequest = SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(body)
                .addMessageAttributesEntry(
                    SENDER,
                    MessageAttributeValue().withDataType("String").withStringValue(ORDER_ENQUEUING_SERVICE)
                )

            logger.debug("Trying to enqueuing message with id ($mongoId) and body: $body")

            val result = sqsClient.sendMessage(msgRequest)

            logger.debug("Succeeded in enqueuing message with id: $mongoId ($result)")
        }
            .toEither()
            .mapLeft {
                val message = "${it.cause ?: "Unable to write message metadata to queue for indeterminate reason :(!"}"

                logger.error("Failed to enqueue message: $message")

                routingContext.response()
                    .setStatusCode(SC_INTERNAL_SERVER_ERROR)
                    .end(
                        ErrorBody(listOf(message)).asJsonString(prettyPrint = true)
                    )
            }
            .map {
                routingContext.response()
                    .setStatusCode(SC_CREATED)
                    .putHeader(LOCATION_HEADER, "$GET_ORDER_ENDPOINT/$mongoId")
                    .end(OrderCreationResponse(mongoId).asJsonString())
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageEnqueuingHandler::class.java)
    }
}

data class OrderCreationResponse(val orderId: String)
data class OrderMessage(val mongoId: String)