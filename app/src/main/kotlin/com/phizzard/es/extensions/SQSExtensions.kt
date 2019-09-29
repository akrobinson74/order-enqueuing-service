package com.phizzard.es.extensions

import com.amazonaws.services.sqs.model.MessageAttributeValue
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.phizzard.MESSAGE_CLASS
import com.phizzard.es.ORDER_ENQUEUING_SERVICE
import com.phizzard.es.PLATFORM_ID
import com.phizzard.es.SENDER

const val STRING = "String"

fun getMessageRequest(
    url: String,
    messageClass: String,
    platformId: String,
    json: String
) = SendMessageRequest()
    .withQueueUrl(url)
    .withMessageBody(json)
    .addMessageAttributesEntry(
        MESSAGE_CLASS,
        MessageAttributeValue().withDataType(STRING).withStringValue(messageClass)
    )
    .addMessageAttributesEntry(
        PLATFORM_ID,
        MessageAttributeValue().withDataType(STRING).withStringValue(platformId)
    )
    .addMessageAttributesEntry(
        SENDER,
        MessageAttributeValue().withDataType(STRING).withStringValue(ORDER_ENQUEUING_SERVICE)
    )
