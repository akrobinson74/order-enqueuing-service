package com.phizzard.es.extensions

import arrow.core.Try
import com.newrelic.api.agent.NewRelic
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.RequestParameters
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger

suspend inline fun <reified T : Verticle> Vertx.deployVerticle(options: DeploymentOptions = DeploymentOptions()) =
    deployVerticleAwait(T::class.java.canonicalName, options)

val RoutingContext.parsedParameters: RequestParameters
    get() = get("parsedParameters")

fun RoutingContext.containsParameter(parameterName: String) =
    get<RequestParameters>("parsedParameters")?.pathParametersNames()?.contains(parameterName) ?: false

inline fun <reified T : Any> RequestParameters.mapTo(): T = body().jsonObject.mapTo()

inline fun <reified T : Any> JsonObject.mapTo(): T = mapTo(T::class.java)

// MR on Vert.x open https://github.com/vert-x3/vertx-lang-kotlin/pull/97
inline fun <reified T> HttpRequest<Buffer>.mapTo(): HttpRequest<T> = `as`(BodyCodec.json(T::class.java))

fun <T> MessageConsumer<T>.suspendingHandler(
    scope: CoroutineScope,
    logger: Logger,
    handler: suspend (Message<T>) -> Unit
): MessageConsumer<T> = handler { message ->
    val token = NewRelic.getAgent().transaction.token
    scope.launch {
        token.link()
        Try { handler(message) }.failed().map {
            logger.error(message.headers().marker, it.message, it)
            message.fail(1, it.message)
        }
        token.expire()
    }
}