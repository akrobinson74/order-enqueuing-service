package com.phizzard.es

// Environment variables
const val HTTP_PORT = "HTTP_PORT"
const val MONGO_URL = "MONGO_URL"
const val SQS_URL = "SQS_URL"

// Default values
const val DEFAULT_HTTP_PORT = 9080
const val LOCALHOST = "http://127.0.0.1"
const val MONGO_ID = "mongo-id"
const val MONGO_ID_FIELD_NAME = "_id"
const val ORDERS_COLLECTION_NAME = "orders"
const val REQUEST_CONTEXT = "requestContext"

// SQS Message Attributes
const val ORDER_ENQUEUING_SERVICE = "order-enqueuing-service"
const val SENDER = "sender"

const val LOCATION_HEADER = "Location"

const val GET_ORDER_ENDPOINT = "/order"
const val POST_ORDER_ENDPOINT = "/create"

const val AUTH_PASSWORD = "AUTH_PASSWORD"
const val AUTH_USERNAME = "AUTH_USERNAME"
const val CREATE_ORDER_OPERATION_ID = "createOrder"
const val GET_ALL_ORDERS_OPERATION_ID = "getAllOrders"
const val GET_ORDER_OPERATION_ID = "getOrder"
const val HANDLE_AUTH_OPERATION_ID = "handleAuth"
const val HEALTH_CHECK = "healthCheck"
const val METRICS = "metrics"
const val OPEN_API_PATH = "openapi.yaml"
const val PASSWORD = "password"
const val USERNAME = "username"

const val ORDER_ID_PATH_PARAM_NAME = "orderId"
const val PARTNER_ID_PATH_PARAM_NAME = "platformId"
const val PLATFORM_ID = "platformId"

const val BOOTSTRAP = "com.phizzard.es.bootstrap"
const val BOOTSTRAP_HEALTH = "bootstrap-health"