package com.phizzard.es


import arrow.core.Try
import com.phizzard.es.verticles.BootstrapVerticle
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.newrelic.NewRelicConfig
import io.micrometer.newrelic.NewRelicMeterRegistry
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.file.impl.FileResolver
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.DeploymentOptions
import io.vertx.kotlin.core.VertxOptions
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.micrometer.MicrometerMetricsOptions
import io.vertx.kotlin.micrometer.VertxPrometheusOptions
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.backends.PrometheusBackendRegistry
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class App {
    suspend fun main(args: Array<String>) {

        System.setProperty(FileResolver.CACHE_DIR_BASE_PROP_NAME, "/tmp/.vertx")
        if (args.isEmpty()) exitProcess(1)
        val configurationPath = args[0]

        val vertx = Vertx.vertx(VertxOptions(metricsOptions = buildMetricsOptions()))
        val config = obtainConfiguration(vertx, configurationPath)
        Try {
            vertx.deployVerticleAwait(BootstrapVerticle::class.java.canonicalName, DeploymentOptions(config = config))
        }.failed().map {
            LoggerFactory.getLogger("main").error("deployment failed", it)
            exitProcess(1)
        }
    }

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
}
private const val NEW_RELIC_APP_NAME = "NEW_RELIC_APP_NAME"
private const val NR_INSIGHTS_KEY = "NR_INSIGHTS_KEY"
private const val NR_ACCOUNT_ID = "NR_ACCOUNT_ID"