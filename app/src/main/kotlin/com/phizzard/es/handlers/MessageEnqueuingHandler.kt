package com.phizzard.es.handlers

import arrow.core.Either
import arrow.core.getOrHandle
import com.amazonaws.services.sqs.AmazonSQS
import com.phizzard.MESSAGE_CLASS
import com.phizzard.es.GET_ORDER_ENDPOINT
import com.phizzard.es.LOCATION_HEADER
import com.phizzard.es.MONGO_ID
import com.phizzard.es.PLATFORM_ID
import com.phizzard.es.extensions.getMessageRequest
import com.phizzard.es.models.ErrorBody
import com.phizzard.models.MessageClass
import com.phizzard.models.PartnerPlatform
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_ACCEPTED
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.slf4j.LoggerFactory

class MessageEnqueuingHandler(
    val queueUrl: String,
    val sqsClient: AmazonSQS
) {
    suspend fun handle(routingContext: RoutingContext) = Either.catch {
        logger.info("Entered MessageEnqueuingHandler::handle...")
        val mongoId = routingContext.get<String>(MONGO_ID)
        logger.debug("Trying to enqueuing message with id ($mongoId)")
        val body = OrderMessage(mongoId).asJsonString()
        logger.debug("Message body: $body")

        val messageClass = MessageClass.getMessageClassForModel(routingContext.get<String>(MESSAGE_CLASS)).name
        val platformId = routingContext.get<PartnerPlatform>(PLATFORM_ID).name
        val msgRequest =
            getMessageRequest(json = body, msgClass = messageClass, platformId = platformId, url = queueUrl)
        val result = sqsClient.sendMessage(msgRequest)
        logger.debug("Succeeded in enqueuing message with id: $mongoId ($result)")

        routingContext.response()
            .setStatusCode(if (routingContext.get<String>(mongoId)?.equals("PUT") ?: false) SC_ACCEPTED else SC_CREATED)
            .putHeader(LOCATION_HEADER, "$GET_ORDER_ENDPOINT/$mongoId")
            .end(OrderEnqueuingResponse(mongoId).asJsonString())
    }
        .getOrHandle { handleEnqueuingError(routingContext, it) }

    private fun handleEnqueuingError(context: RoutingContext, it: Throwable) {
        val message = "${it.message ?: "Unable to write message metadata to queue for indeterminate reason :(!"}"
        logger.error("Failed to enqueue message: $message")

        context.response()
            .setStatusCode(SC_INTERNAL_SERVER_ERROR)
            .end(ErrorBody(listOf(message)).asJsonString(prettyPrint = true))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageEnqueuingHandler::class.java)
    }
}

data class OrderEnqueuingResponse(val orderId: String)
data class OrderMessage(val mongoId: String)