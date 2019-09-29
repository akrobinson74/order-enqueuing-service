package com.phizzard.es

import arrow.core.Either
import com.phizzard.es.verticles.BootstrapVerticle
import io.vertx.core.Vertx
import io.vertx.core.file.impl.FileResolver
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {

    System.setProperty(FileResolver.CACHE_DIR_BASE_PROP_NAME, "/tmp/.vertx")
    if (args.isEmpty()) exitProcess(1)
    val configurationPath = args[0]

    val vertx = Vertx.vertx(vertxOptionsOf(metricsOptions = buildMetricsOptions()))
    val config = obtainConfiguration(vertx, configurationPath)
    Either.catch {
        vertx.deployVerticleAwait(BootstrapVerticle::class.java.canonicalName, deploymentOptionsOf(config = config))
    }
        .mapLeft {
            LoggerFactory.getLogger("main").error("deployment failed", it)
            exitProcess(1)
        }
}
