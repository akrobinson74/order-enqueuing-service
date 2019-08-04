package com.phizzard.es

import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.newrelic.NewRelicConfig
import io.micrometer.newrelic.NewRelicMeterRegistry
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.micrometer.micrometerMetricsOptionsOf
import io.vertx.kotlin.micrometer.vertxPrometheusOptionsOf
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.backends.BackendRegistries
import io.vertx.micrometer.backends.PrometheusBackendRegistry
import org.apache.http.HttpStatus

fun buildMetricsOptions(): MicrometerMetricsOptions {
    val registries = mutableListOf(
        PrometheusBackendRegistry(vertxPrometheusOptionsOf(enabled = true, publishQuantiles = true)).meterRegistry
    )
    if (!System.getenv(NR_INSIGHTS_KEY).isNullOrEmpty()) {
        registries += NewRelicMeterRegistry(
            object : NewRelicConfig {
                override fun accountId(): String {
                    val accountId = System.getenv(NR_ACCOUNT_ID); System.out.println(accountId); return accountId
                }

                override fun apiKey(): String {
                    val apiKey = System.getenv(NR_INSIGHTS_KEY); System.out.println(apiKey); return apiKey
                }

                override fun get(key: String): String? = null

                override fun uri(): String =
                    System.getenv(INSIGHTS_URI) ?: "https://insights-collector.eu01.nr-data.net"
            }, io.micrometer.core.instrument.Clock.SYSTEM
        ).also { it.config().commonTags("appName", System.getenv(NEW_RELIC_APP_NAME)) }
    }
    return micrometerMetricsOptionsOf(registryName = COMPOSITE_REGISTRY_NAME, enabled = true, jvmMetricsEnabled = true)
        .setMicrometerRegistry(CompositeMeterRegistry(io.micrometer.core.instrument.Clock.SYSTEM, registries))
}

/**
 * Based on handler from [io.vertx.micrometer.impl.PrometheusScrapingHandlerImpl]
 */
@Suppress("UnsafeCast")
fun prometheusHandler(routingContext: RoutingContext) {
    (BackendRegistries.getNow(COMPOSITE_REGISTRY_NAME) as CompositeMeterRegistry)
        .registries
        .find { it is PrometheusMeterRegistry }
        ?.let {
            val registry = it as PrometheusMeterRegistry
            routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
                .end(registry.scrape())
        }
        ?: routingContext
            .response()
            .setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .setStatusMessage("Prometheus registry not found")
            .end()
}

private const val COMPOSITE_REGISTRY_NAME = "composite"
private const val INSIGHTS_URI = "INSIGHTS_URI"
private const val NEW_RELIC_APP_NAME = "NEW_RELIC_APP_NAME"
private const val NR_INSIGHTS_KEY = "NR_INSIGHTS_KEY"
private const val NR_ACCOUNT_ID = "NR_ACCOUNT_ID"