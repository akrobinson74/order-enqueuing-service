package com.phizzard.es

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.json.JsonObject

data class ElasticsearchConfig(
    val domainARN: String,
    val endpoint: String,
    val kibana: String,
    val version: String
)

val JsonObject.elasticSearch: ElasticsearchConfig
    get() = getJsonObject("elasticSearch").mapTo(ElasticsearchConfig::class.java)

data class MongoConfig(
    val connection_string: String = "mongodb://127.0.0.1",
    val db_name: String = "orders",
    val host: String = "127.0.0.1",
    val password: String = "",
    val port: Int = 27017,
    val username: String = ""
)

val JsonObject.mongoConfig: JsonObject
    get() = getJsonObject("mongoConfig")
        .overrideConfigValuesWithEnvVars(mapOf("connection_string" to MONGO_URL))

data class SqsConfig(
    val accessKeyId: String = "x",
    val queueName: String = "incoming-orders",
    val secretAccessKey: String = "x",
    val serviceEndpoint: String = "http://localhost:9324",
    val signingRegion: String = "elasticmq"
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
