package com.phizzard.es

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.json.jsonObjectOf

data class CorsConfig(
    val allowedHeaders: Set<String>,
    val allowedMethods: Set<HttpMethod>,
    val allowedOriginPattern: String
)

val JsonObject.corsConfig: CorsConfig
    get() = getJsonObject("cors").mapTo(CorsConfig::class.java)

val JsonObject.mongoConfig: JsonObject
    get() = getJsonObject("mongoConfig")
        .overrideConfigValuesWithEnvVars(mapOf("connection_string" to MONGO_URL))

data class SqsConfig(
    val accessKeyId: String,
    val queueName: String,
    val secretAccessKey: String,
    val serviceEndpoint: String,
    val signingRegion: String
)

val JsonObject.sqsConfig: SqsConfig
    get() = getJsonObject("sqsConfig")
        .overrideConfigValuesWithEnvVars(mapOf("serviceEndpoint" to SQS_URL))
        .mapTo(SqsConfig::class.java)

suspend fun obtainConfiguration(vertx: Vertx, configurationPath: String): JsonObject {
    val envStore = configStoreOptionsOf(type = "env")
    val yamlStore = configStoreOptionsOf(
        type = "file",
        format = "yaml",
        config = jsonObjectOf("path" to configurationPath)
    )
    return ConfigRetriever
        .create(vertx, configRetrieverOptionsOf(stores = listOf(envStore, yamlStore)))
        .getConfigAwait()
}

fun JsonObject.overrideConfigValuesWithEnvVars(configFieldToEnvVarMap: Map<String, String>): JsonObject {
    configFieldToEnvVarMap.filter { getString(it.value, "").isNotEmpty() }
        .map { this.put(it.key, getString(it.value)) }
    return this
}

val DEFAULT_MAPPER: ObjectMapper = jacksonObjectMapper()
    .registerModules(JavaTimeModule())
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
val PRETTY_PRINTING_MAPPER: ObjectWriter = DEFAULT_MAPPER.copy().writerWithDefaultPrettyPrinter()
