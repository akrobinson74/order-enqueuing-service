package com.phizzard.es

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.phizzard.es.models.Address
import com.phizzard.es.models.Contact
import com.phizzard.es.models.InboundOrder
import com.phizzard.es.models.OrderItem
import com.phizzard.es.models.StoreDetails
import com.phizzard.models.OrderStatus
import io.vertx.core.http.HttpMethod.CONNECT
import io.vertx.core.http.HttpMethod.DELETE
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.HEAD
import io.vertx.core.http.HttpMethod.OPTIONS
import io.vertx.core.http.HttpMethod.OTHER
import io.vertx.core.http.HttpMethod.PATCH
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.http.HttpMethod.PUT
import io.vertx.core.http.HttpMethod.TRACE
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.math.BigInteger
import java.time.Instant

const val ONE = 1
const val TWO = 2
const val THREE = 3
const val FOUR = 4
const val FIVE = 5
const val LISTENING_PORT = 54321
const val LOCALHOST = "localhost"
const val MONGO_EXTENSION_CLASS = "com.phizzard.es.extensions.MongoClientExtensionsKt"
const val ORDER_PATH = "/order"
const val ORDERS_PATH = "/orders"
const val TRANSACTION_DATE = "transactionDate"

const val ADAMS_DOB_EPOCH_SECONDS = 143398800000L
const val ZERO_DAYS = 0L
const val TEN_YEARS_IN_DAYS = 3653L
const val TWENTY_YEARS_IN_DAYS = 7305L
const val THIRTY_YEARS_IN_DAYS = 10968L
const val FORTY_YEARS_IN_DAYS = 14620L

const val SAMPLE_ADDRESS = """
{
  "city": "Berlin",
  "country": "Germany",
  "street": "Am Studio",
  "streetNumber": "2a",
  "zipCode": "12489"
}
"""
const val SAMPLE_ORDER = """
{
  "id": 123467890,
  "approvalCode": "666",
  "buyerContact": {
    "address": {
      "city": "Berlin",
      "country": "Germany",
      "street": "Am Studio",
      "streetNumber": "2a",
      "zipCode": "12489"
    },
    "email": "michael.mente@phizzard.com",
    "firstName": "Michael",
    "lastName": "Mente",
    "phoneNumber": "49123456789910"
  },
  "orderItems": [
    {
      "articleDescription": "Ladies Kjus T-shirt",
      "currency": "CHF",
      "gtin": "07612997688944",
      "grossPrice": 64.07,
      "netPrice": 69.0,
      "quantity": 1,
      "supplierName": "kjus"
    }
  ],
  "status": "INITIAL",
  "store": {
    "name": "Pais",
    "storeId": "200742"
  }
}
"""

const val SAMPLE_MONGO_ORDER = """
{
  "id": 123467890,
  "approvalCode": "666",
  "buyerContact": {
    "address": {
      "city": "Berlin",
      "country": "Germany",
      "street": "Am Studio",
      "streetNumber": "2a",
      "zipCode": "12489"
    },
    "email": "michael.mente@phizzard.com",
    "firstName": "Michael",
    "lastName": "Mente",
    "phoneNumber": "49123456789910"
  },
  "orderItems": [
    {
      "articleDescription": "Ladies Kjus T-shirt",
      "currency": "CHF",
      "gtin": "07612997688944",
      "grossPrice": 64.07,
      "netPrice": 69.0,
      "quantity": 1,
      "supplierName": "kjus"
    }
  ],
  "status": "INITIAL",
  "store": {
    "name": "Pais",
    "storeId": "200742"
  },
  "userId": "PSG"
}
"""

const val ORDER_WITH_STRINGS_FOR_DOUBLES = """
{
  "id": 123467890,
  "approvalCode": "666",
  "buyerContact": {
    "address": {
      "city": "Berlin",
      "country": "Germany",
      "street": "Am Studio",
      "streetNumber": "2a",
      "zipCode": "12489"
    },
    "email": "michael.mente@phizzard.com",
    "firstName": "Michael",
    "lastName": "Mente",
    "phoneNumber": "49123456789910"
  },
  "orderItems": [
    {
      "articleDescription": "Ladies Kjus T-shirt",
      "currency": "CHF",
      "gtin": "07612997688944",
      "grossPrice": "64.07",
      "netPrice": "69.0",
      "quantity": 1,
      "supplierName": "kjus"
    }
  ],
  "status": "INITIAL",
  "store": {
    "name": "Pais",
    "storeId": "200742"
  }
}
"""

const val DEFAULT_ELASTICSEARCH_PORT = 9324
const val DEFAULT_MONGO_PORT = 27017

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

object MongoContainer {
    val instance by lazy { startMongoContainer() }

    private fun startMongoContainer() = KGenericContainer("mongo:4.0.2").apply {
        withExposedPorts(DEFAULT_MONGO_PORT)
        setWaitStrategy(Wait.forListeningPort())
        start()
    }
}

object SQSContainer {
    val instance by lazy { startSqs() }

    private fun startSqs() = KGenericContainer("softwaremill/elasticmq").apply {
        withClasspathResourceMapping("elasticmq.conf", "/etc/elasticmq.conf", BindMode.READ_ONLY)
        withExposedPorts(DEFAULT_ELASTICSEARCH_PORT)
        setWaitStrategy(Wait.forListeningPort())
        start()
    }
}

val mapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
    .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
    .registerModule(JavaTimeModule())
    .setDateFormat(StdDateFormat())

fun registerJacksonModules() {
    listOf(Json.mapper, Json.prettyMapper).forEach {
        it.registerModules(KotlinModule(), JavaTimeModule())
        it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//        it.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
//        it.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
        it.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }
}

val sampleOrderJson = jacksonObjectMapper().readValue<InboundOrder>(SAMPLE_ORDER)

val serializationTestOrder = InboundOrder(
    approvalCode = "iApprove",
    buyerContact = Contact(
        address = Address(
            city = "Berlin",
            country = "DE",
            street = "Zur Innung",
            streetNumber = "38",
            zipCode = "10247"
        ),
        email = "adam.robinson@phizzard.com",
        firstName = "Adam K",
        lastName = "Robinson",
        phoneNumber = "+49 30 208480 220"
    ),
    id = BigInteger.ZERO,
    orderItems = listOf(
        OrderItem(
            articleDescription = "Jacke Molecule",
            currency = "CHF",
            gtin = "04043874441278",
            grossPrice = 649.95,
            netPrice = 700.0,
            quantity = BigInteger.ONE,
            supplierName = "falke"
        ),
        OrderItem(
            articleDescription = "Cloudventure Waterproof",
            currency = "CHF",
            gtin = "07630040530071",
            grossPrice = 232.13,
            netPrice = 250.0,
            quantity = BigInteger.ONE,
            supplierName = "onrunning"
        ),
        OrderItem(
            articleDescription = "Grandfather New Balance Tee",
            currency = "CHF",
            gtin = "0190737755905",
            grossPrice = 55.62,
            netPrice = 59.90,
            quantity = BigInteger.ONE,
            supplierName = "chrissports"
        ),
        OrderItem(
            articleDescription = "Fundamentals Arena Logo Boxer",
            currency = "CHF",
            gtin = "03468335386195",
            grossPrice = 32.62,
            netPrice = 34.90,
            quantity = BigInteger.ONE,
            supplierName = "chrissports"
        ),
        OrderItem(
            articleDescription = "Haglöfs Mistral GT Men",
            currency = "CHF",
            gtin = "07318841021911",
            grossPrice = 185.70,
            netPrice = 200.0,
            quantity = BigInteger.ONE,
            supplierName = "montana"
        ),
        OrderItem(
            articleDescription = "Mammut Ultimate V Tour SO Hooded Jacket Men",
            currency = "CHF",
            gtin = "07613357384490",
            grossPrice = 277.62,
            netPrice = 299.0,
            quantity = BigInteger.ONE,
            supplierName = "mammut"
        ),
        OrderItem(
            articleDescription = "Ladies Kjus T-shirt",
            currency = "CHF",
            gtin = "07612997688944",
            grossPrice = 64.07,
            netPrice = 69.0,
            quantity = BigInteger.ONE,
            supplierName = "kjus"
        ),
        OrderItem(
            articleDescription = "LOWA WORKER GTX® S3, schwarz",
            currency = "CHF",
            gtin = "04052471391787",
            grossPrice = 343.45,
            netPrice = 369.90,
            quantity = BigInteger.ONE,
            supplierName = "lowa"
        )
    ),
    status = OrderStatus.OPEN,
    store = StoreDetails(
        contactInfo = null,
        gln = "4399902217590",
        name = "AReallyAwesomeStore"
    ),
    transactionDate = Instant.parse("2019-09-01T12:00:00Z")
)

const val MONGO_DB_PATH = "/test_orders?streamType=netty"
val testConfig = jsonObjectOf(
    HTTP_PORT to DEFAULT_HTTP_PORT,
    "cors" to JsonObject.mapFrom(
        CorsConfig(
            allowedHeaders = setOf(
                "Authorization", "Cache-Control", "Content-Length", "Content-Range", "Content-Type", "DNT",
                "If-Modified-Since", "User-Agent", "X-Requested-With"
            ),
            allowedMethods = setOf(CONNECT, DELETE, GET, HEAD, OPTIONS, OTHER, PATCH, POST, PUT, TRACE),
            allowedOriginPattern = "*"
        )
    ),
    "mongoConfig" to mapOf("connection_string" to "mongodb://127.0.0.1/test_orders?streamType=netty"),
    "sqsConfig" to JsonObject.mapFrom(
        SqsConfig(
            accessKeyId = "x",
            queueName = "test-orders",
            secretAccessKey = "x",
            serviceEndpoint = "http://localhost:9324",
            signingRegion = "elasticmq"
        )
    )
)

val deltaDaysList = listOf(
    ZERO_DAYS,
    TEN_YEARS_IN_DAYS,
    TWENTY_YEARS_IN_DAYS,
    THIRTY_YEARS_IN_DAYS,
    FORTY_YEARS_IN_DAYS
)