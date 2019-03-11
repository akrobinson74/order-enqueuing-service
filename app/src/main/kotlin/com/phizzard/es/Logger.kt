package com.phizzard.es

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import net.logstash.logback.marker.MapEntriesAppendingMarker
import org.slf4j.Marker

val RoutingContext.requestContext: JsonObject
    get() = get<JsonObject>(REQUEST_CONTEXT) ?: JsonObject()

val RoutingContext.requestMarker: Marker
    get() = markerOf(requestContext)


fun markerOf(jsonObject: JsonObject) = markerOf(jsonObject.map)

fun markerOf(map: Map<String, Any> = emptyMap()): Marker = MapEntriesAppendingMarker(map)