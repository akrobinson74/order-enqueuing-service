package com.phizzard.es.handlers

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.RoutingContext

class AuthHandler : Handler<RoutingContext> {
    private val authProvider: AuthProvider

    init {
        authProvider = JWTAuth.create(
            Vertx.vertx(),
            JWTAuthOptions().addPubSecKey(
                PubSecKeyOptions()
                    .setAlgorithm("ES256")
                    .setPublicKey(
                        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENyvVJ/AN0fPTE7vJCieXo/mMAnIB\n" +
                            "7k9YBqEuC23Y6JFdrM/5z8VrKZ4UVz7WfrnoPGo5PbBUVskezWTVXpws4Q=="
                    )
                    .setSecretKey(
                        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg3hHwF/cw/PxvY83l\n" +
                            "6JWJfu57S1LtCpFbB2k6cwJzgUWhRANCAAQ3K9Un8A3R89MTu8kKJ5ej+YwCcgHu\n" +
                            "T1gGoS4LbdjokV2sz/nPxWspnhRXPtZ+ueg8ajk9sFRWyR7NZNVenCzh"
                    )
            )
        )
    }

    override fun handle(routingContext: RoutingContext) {
//        val authToken = routingContext.
    }
}