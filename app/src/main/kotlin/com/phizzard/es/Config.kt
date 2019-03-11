package com.phizzard.es

import io.vertx.core.json.JsonObject

data class ElasticsearchConfig(
    val domainARN: String,
    val endpoint: String,
    val kibana: String,
    val version: String
)

val JsonObject.elasticSearch: ElasticsearchConfig
    get() = getJsonObject("elasticSearch").mapTo(ElasticsearchConfig::class.java)