package com.phizzard.es.extensions

import com.phizzard.es.REQUEST_CONTEXT
import com.phizzard.es.markerOf
import io.vertx.core.MultiMap
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

val RoutingContext.requestContext: JsonObject
    get() = get<JsonObject>(REQUEST_CONTEXT) ?: JsonObject()

val RoutingContext.requestMarker: Marker
    get() = markerOf(requestContext)

val MultiMap.marker: Marker
    get() = markerOf(this.names().map {
        it to (this.getAll(it).firstOrNull() ?: "")
    }.toMap().filterValues { it.isNotEmpty() })