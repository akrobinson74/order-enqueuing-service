package com.phizzard.es.handlers

import com.phizzard.JWTConfig
import com.phizzard.es.AUTH_PASSWORD
import com.phizzard.es.AUTH_USERNAME
import com.phizzard.es.PASSWORD
import com.phizzard.es.USERNAME
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.jwt.JWTOptions
import io.vertx.ext.web.RoutingContext
import org.apache.http.HttpStatus.SC_OK
import org.apache.http.HttpStatus.SC_UNAUTHORIZED
import org.slf4j.LoggerFactory

class AuthHandler constructor(
    private val config: JsonObject,
    private val jwtAuth: JWTAuth
) {
    suspend fun handle(context: RoutingContext) {
        val (password, username) = listOf(PASSWORD, USERNAME).map { context.request().getHeader(it) }

        logger.debug("Config: {}", config)

        if (config.getString(AUTH_USERNAME).equals(username) && config.getString(AUTH_PASSWORD).equals(password)) {
            val token = jwtAuth.generateToken(
                JsonObject(),
                JWTOptions().setAlgorithm(config.getJsonObject("jwtConfig").mapTo(JWTConfig::class.java).algorithm)
            )
            context.response()
                .putHeader("Authorization", "Bearer $token")
                .setStatusCode(SC_OK)
                .end()
        } else
            context.response()
                .setStatusCode(SC_UNAUTHORIZED)
                .end()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
}