package com.phizzard.es

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.json.JsonObject

data class CorsConfig(
    val allowedHeaders: Set<String>,
    val allowedMethods: Set<HttpMethod>,
    val allowedOriginPattern: String
)

val JsonObject.corsConfig: CorsConfig
    get() = getJsonObject("cors").mapTo(CorsConfig::class.java)

data class ElasticsearchConfig(
    val domainARN: String,
    val endpoint: String,
    val kibana: String,
    val version: String
)

val JsonObject.elasticSearch: ElasticsearchConfig
    get() = getJsonObject("elasticSearch").mapTo(ElasticsearchConfig::class.java)

data class MongoConfig(
    val connection_string: String,
    val db_name: String,
    val host: String,
    val password: String,
    val port: Int,
    val username: String
)

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
        config = JsonObject("path" to configurationPath)
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

val NULL_QUERY_PROJECTION: JsonObject? = null
val DEFAULT_MAPPER: ObjectMapper = jacksonObjectMapper()
val PRETTY_PRINTING_MAPPER: ObjectWriter = DEFAULT_MAPPER.copy().writerWithDefaultPrettyPrinter()
