/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Update this file with
 *   `$ ./gradlew buildSrcVersions` */
object Libs {
    /**
     * http://logback.qos.ch */
    const val logback_classic: String = "ch.qos.logback:logback-classic:" + Versions.logback_classic

    /**
     * https://github.com/awslabs/amazon-sqs-java-messaging-lib/ */
    const val amazon_sqs_java_messaging_lib: String =
            "com.amazonaws:amazon-sqs-java-messaging-lib:" + Versions.amazon_sqs_java_messaging_lib

    /**
     * https://github.com/auth0/java-jwt */
    const val java_jwt: String = "com.auth0:java-jwt:" + Versions.java_jwt

    /**
     * https://github.com/FasterXML/jackson-modules-java8 */
    const val jackson_datatype_jsr310: String =
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:" +
            Versions.jackson_datatype_jsr310

    /**
     * https://github.com/FasterXML/jackson-module-kotlin */
    const val jackson_module_kotlin: String =
            "com.fasterxml.jackson.module:jackson-module-kotlin:" + Versions.jackson_module_kotlin

    const val com_github_johnrengelman_shadow_gradle_plugin: String =
            "com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:" +
            Versions.com_github_johnrengelman_shadow_gradle_plugin

    /**
     * https://github.com/shyiko/ktlint */
    const val ktlint: String = "com.github.shyiko:ktlint:" + Versions.ktlint

    /**
     * https://newrelic.com/ */
    const val newrelic_api: String = "com.newrelic.agent.java:newrelic-api:" + Versions.newrelic_api

    /**
     * https://github.com/nhaarman/mockito-kotlin */
    const val mockito_kotlin: String = "com.nhaarman.mockitokotlin2:mockito-kotlin:" +
            Versions.mockito_kotlin

    /**
     * https://github.com/square/okhttp */
    const val mockwebserver: String = "com.squareup.okhttp3:mockwebserver:" + Versions.mockwebserver

    const val de_fayard_buildsrcversions_gradle_plugin: String =
            "de.fayard.buildSrcVersions:de.fayard.buildSrcVersions.gradle.plugin:" +
            Versions.de_fayard_buildsrcversions_gradle_plugin

    /**
     * http://github.com/flapdoodle-oss/embedmongo.flapdoodle.de */
    const val de_flapdoodle_embed_mongo: String = "de.flapdoodle.embed:de.flapdoodle.embed.mongo:" +
            Versions.de_flapdoodle_embed_mongo

    /**
     * https://github.com/arrow-kt/arrow/ */
    const val arrow_core: String = "io.arrow-kt:arrow-core:" + Versions.arrow_core

    const val detekt_cli: String = "io.gitlab.arturbosch.detekt:detekt-cli:" +
            Versions.io_gitlab_arturbosch_detekt

    const val io_gitlab_arturbosch_detekt_gradle_plugin: String =
            "io.gitlab.arturbosch.detekt:io.gitlab.arturbosch.detekt.gradle.plugin:" +
            Versions.io_gitlab_arturbosch_detekt

    /**
     * https://github.com/micrometer-metrics/micrometer */
    const val micrometer_registry_new_relic: String =
            "io.micrometer:micrometer-registry-new-relic:" + Versions.io_micrometer

    /**
     * https://github.com/micrometer-metrics/micrometer */
    const val micrometer_registry_prometheus: String =
            "io.micrometer:micrometer-registry-prometheus:" + Versions.io_micrometer

    /**
     * http://mockk.io */
    const val mockk: String = "io.mockk:mockk:" + Versions.mockk

    const val vertx_config_yaml: String = "io.vertx:vertx-config-yaml:" + Versions.io_vertx

    const val vertx_config: String = "io.vertx:vertx-config:" + Versions.io_vertx

    const val vertx_core: String = "io.vertx:vertx-core:" + Versions.io_vertx

    const val vertx_health_check: String = "io.vertx:vertx-health-check:" + Versions.io_vertx

    const val vertx_junit5: String = "io.vertx:vertx-junit5:" + Versions.io_vertx

    const val vertx_lang_kotlin_coroutines: String = "io.vertx:vertx-lang-kotlin-coroutines:" +
            Versions.io_vertx

    const val vertx_lang_kotlin: String = "io.vertx:vertx-lang-kotlin:" + Versions.io_vertx

    const val vertx_micrometer_metrics: String = "io.vertx:vertx-micrometer-metrics:" +
            Versions.io_vertx

    const val vertx_mongo_client: String = "io.vertx:vertx-mongo-client:" + Versions.io_vertx

    const val vertx_web_api_contract: String = "io.vertx:vertx-web-api-contract:" +
            Versions.io_vertx

    const val vertx_web_client: String = "io.vertx:vertx-web-client:" + Versions.io_vertx

    const val vertx_web: String = "io.vertx:vertx-web:" + Versions.io_vertx

    /**
     * https://github.com/logstash/logstash-logback-encoder */
    const val logstash_logback_encoder: String = "net.logstash.logback:logstash-logback-encoder:" +
            Versions.logstash_logback_encoder

    /**
     * http://assertj.org */
    const val assertj_core: String = "org.assertj:assertj-core:" + Versions.assertj_core

    /**
     * http://www.bouncycastle.org/java.html */
    const val bcprov_jdk15on: String = "org.bouncycastle:bcprov-jdk15on:" + Versions.bcprov_jdk15on

    /**
     * http://softwaremill.com/open-source */
    const val elasticmq_rest_sqs_2_12: String = "org.elasticmq:elasticmq-rest-sqs_2.12:" +
            Versions.elasticmq_rest_sqs_2_12

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String =
            "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:" +
            Versions.org_jetbrains_kotlin_jvm_gradle_plugin

    /**
     * https://kotlinlang.org/ */
    const val kotlin_scripting_compiler_embeddable: String =
            "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:" +
            Versions.kotlin_scripting_compiler_embeddable

    /**
     * https://kotlinlang.org/ */
    const val org_jetbrains_kotlin_kotlin_stdlib_jdk8: String =
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:" +
            Versions.org_jetbrains_kotlin_kotlin_stdlib_jdk8

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_api: String = "org.junit.jupiter:junit-jupiter-api:" +
            Versions.junit_jupiter_api

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_engine: String = "org.junit.jupiter:junit-jupiter-engine:" +
            Versions.junit_jupiter_engine

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_params: String = "org.junit.jupiter:junit-jupiter-params:" +
            Versions.junit_jupiter_params

    /**
     * https://junit.org/junit5/ */
    const val junit_bom: String = "org.junit:junit-bom:" + Versions.junit_bom

    const val org_owasp_dependencycheck_gradle_plugin: String =
            "org.owasp.dependencycheck:org.owasp.dependencycheck.gradle.plugin:" +
            Versions.org_owasp_dependencycheck_gradle_plugin

    const val org_sonarqube_gradle_plugin: String = "org.sonarqube:org.sonarqube.gradle.plugin:" +
            Versions.org_sonarqube_gradle_plugin

    /**
     * https://testcontainers.org */
    const val junit_jupiter: String = "org.testcontainers:junit-jupiter:" + Versions.junit_jupiter
}
