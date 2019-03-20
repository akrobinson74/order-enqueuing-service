package com.phizzard.es

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
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
    val host: String,
    val port: String
)

val JsonObject.mongoConfig: JsonObject
    get() = getJsonObject("mongoConfig")

suspend fun obtainConfiguration(vertx: Vertx, configurationPath: String): JsonObject {
    val envStore = ConfigStoreOptions(type = "env")
    val yamlStore = ConfigStoreOptions(
        type = "file",
        format = "yaml",
        config = JsonObject("path" to configurationPath)
    )
    return ConfigRetriever
        .create(vertx, ConfigRetrieverOptions(stores = listOf(envStore, yamlStore)))
        .getConfigAwait()
}
