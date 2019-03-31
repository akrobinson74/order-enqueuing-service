package com.phizzard.es

// Environment variables
const val HTTP_PORT = "HTTP_PORT"

// Default values
const val DEFAULT_HTTP_PORT = 8080

const val MONGO_ID = "mongo-id"
const val ORDERS_COLLECTION_NAME = "orders"
const val REQUEST_CONTEXT = "requestContext"

// SQS Message Attributes
const val ORDER_ENQUEUING_SERVICE = "order-enqueuing-service"
const val SENDER = "sender"

const val LOCATION_HEADER = "Location"

const val GET_ORDER_ENDPOINT = "/order"
const val POST_ORDER_ENDPOINT = "/create"

const val CREATE_ORDER_OPERATION_ID = "createOrder"
const val GET_ORDER_OPERATION_ID = "getOrder"
const val HEALTHCHECK = "healthCheck"
const val METRICS = "metrics"
const val OPEN_API_PATH = "/openapi.yaml"