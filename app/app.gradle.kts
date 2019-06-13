
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.github.johnrengelman.shadow") version Versions.com_github_johnrengelman_shadow_gradle_plugin
}

// -------------------------- Dependencies ------------------------------------

val newrelic by configurations.creating

dependencies {
    api(Libs.aws_sqs_msg_lib)
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

    testImplementation(Libs.elastimq_sqs)
    testImplementation(Libs.embedded_mongo)
    testImplementation(Libs.io_mockk)
    testImplementation(Libs.test_containers)

    newrelic(Libs.newrelic_agent)
}

// -------------------------- Building Application ----------------------------

application {
    mainClassName = "com.phizzard.es.AppKt"
}

val copyNewRelicAgent by tasks.creating(Copy::class) {
    from(newrelic)
    from("$projectDir/src/main/resources") {
        include("newrelic.yml")
    }
    into("$rootDir/docker/app/build/")
    rename {
        if (it == "newrelic-agent-${Versions.com_newrelic_agent_java}.jar") "newrelic.jar"
        else it
    }
}

val copyNecessaryFiles by tasks.creating(Copy::class) {
    from("$projectDir/src/main/resources") {
        include("openapi.yaml", "config*.yml", "logback.xml", "wait-for-it.sh")
    }
    into("$rootDir/docker/app/build")
}

val renameProdConfig by tasks.creating(Copy::class) {
    from("$projectDir/src/main/resources") {
        include("config-prod.yml")
    }
    rename { fileName ->
        fileName.replace("-prod","")
    }
    into("$rootDir/docker/app/build")
}

tasks {
    getByName<ShadowJar>("shadowJar") {
        baseName = rootProject.name
        classifier = ""
        destinationDir = file("$rootDir/docker/app/build/")
        mergeServiceFiles()
        exclude("META-INF/*.DSA", "META-INF/*.RSA", "*.yml", "*.pem")

        dependsOn.add(copyNewRelicAgent)
        dependsOn.add(copyNecessaryFiles)
        if (System.getenv("ENV")?.equals("prod") ?: false) dependsOn.add(renameProdConfig)
    }
}