package com.phizzard.es

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.newrelic.NewRelicConfig
import io.micrometer.newrelic.NewRelicMeterRegistry
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.micrometer.MicrometerMetricsOptions
import io.vertx.kotlin.micrometer.VertxPrometheusOptions
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.backends.PrometheusBackendRegistry

data class ElasticsearchConfig(
    val domainARN: String,
    val endpoint: String,
    val kibana: String,
    val version: String
)

val JsonObject.elasticSearch: ElasticsearchConfig
    get() = getJsonObject("elasticSearch").mapTo(ElasticsearchConfig::class.java)



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

fun buildMetricsOptions(): MicrometerMetricsOptions {
    val registries = mutableListOf(
        PrometheusBackendRegistry(VertxPrometheusOptions(enabled = true, publishQuantiles = true)).meterRegistry
    )
    if (!System.getenv(NR_INSIGHTS_KEY).isNullOrEmpty()) {
        registries += NewRelicMeterRegistry(
            object : NewRelicConfig {
                override fun accountId(): String = System.getenv(NR_ACCOUNT_ID)
                override fun apiKey(): String = System.getenv(NR_INSIGHTS_KEY)
                override fun get(key: String): String? = null
            }, Clock.SYSTEM
        ).also { it.config().commonTags("appName", System.getenv(NEW_RELIC_APP_NAME)) }
    }
    return MicrometerMetricsOptions(
        registryName = COMPOSITE_REGISTRY_NAME,
        enabled = true,
        jvmMetricsEnabled = true
    )
        .setMicrometerRegistry(CompositeMeterRegistry(Clock.SYSTEM, registries))
}

private const val NEW_RELIC_APP_NAME = "NEW_RELIC_APP_NAME"
private const val NR_INSIGHTS_KEY = "NR_INSIGHTS_KEY"
private const val NR_ACCOUNT_ID = "NR_ACCOUNT_ID"