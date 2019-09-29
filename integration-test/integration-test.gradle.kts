// -------------------------- Dependencies ------------------------------------
dependencies {
    api(Libs.amazon_sqs_java_messaging_lib)
    api(Libs.bcprov_jdk15on)
    api(Libs.jackson_datatype_jsr310)
    api(Libs.jackson_module_kotlin)
    api(Libs.java_jwt)
    api(Libs.logstash_logback_encoder)
    api(Libs.micrometer_registry_new_relic)
    api(Libs.micrometer_registry_prometheus)
    api(Libs.newrelic_api)
    api(Libs.vertx_config)
    api(Libs.vertx_config_yaml)
    api(Libs.vertx_core)
    api(Libs.vertx_lang_kotlin_coroutines)
    api(Libs.vertx_health_check)
    api(Libs.vertx_lang_kotlin)
    api(Libs.vertx_micrometer_metrics)
    api(Libs.vertx_mongo_client)
    api(Libs.vertx_web)
    api(Libs.vertx_web_api_contract)
    api(Libs.vertx_web_client)

    runtimeOnly(Libs.logback_classic)

    testImplementation(Libs.elasticmq_rest_sqs_2_12)
    testImplementation(Libs.de_flapdoodle_embed_mongo)
    testImplementation(Libs.mockk)
    testImplementation(Libs.junit_jupiter)
}

// -------------------------- Building Application ----------------------------