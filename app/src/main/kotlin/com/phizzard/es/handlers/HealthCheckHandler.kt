package com.phizzard.es.handlers

import com.phizzard.es.BOOTSTRAP
import com.phizzard.es.BOOTSTRAP_HEALTH
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status

private const val HEALTH_CHECK_TIMEOUT = 3000L

fun buildHealthCheck(vertx: Vertx): HealthCheckHandler = HealthCheckHandler.createWithHealthChecks(
    HealthChecks.create(vertx)
        .register(vertx, BOOTSTRAP, BOOTSTRAP_HEALTH)
)

private fun HealthChecks.register(vertx: Vertx, name: String, address: String): HealthChecks =
    register("health/$name", HEALTH_CHECK_TIMEOUT) { future ->
        vertx.eventBus().send<JsonObject>(address, JsonObject()) {
            if (it.succeeded()) future.complete(Status.OK())
            else future.complete(Status.KO())
        }
    }