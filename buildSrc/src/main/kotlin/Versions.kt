/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val logback_classic: String = "1.2.3" 

    const val amazon_sqs_java_messaging_lib: String = "1.0.8" // available: "1.0.8"

    const val java_jwt: String = "3.8.3" // available: "3.8.3"

    const val jackson_datatype_jsr310: String = "2.10.0" // available: "2.10.0"

    const val jackson_module_kotlin: String = "2.10.0" // available: "2.10.0"

    const val com_github_johnrengelman_shadow_gradle_plugin: String = "5.1.0"
            // available: "5.1.0"

    const val ktlint: String = "0.31.0" 

    const val newrelic_api: String = "5.7.0" // available: "5.7.0"

    const val mockito_kotlin: String = "2.2.0" // available: "2.2.0"

    const val orderservice_common: String = "0.9.67" // available: "0.9.56"

    const val mockwebserver: String = "4.2.0" // available: "4.2.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.6.1" // available: "0.6.1"

    const val de_flapdoodle_embed_mongo: String = "2.2.0" 

    const val arrow_core: String = "0.10.0" // available: "0.10.0"

    const val io_gitlab_arturbosch_detekt: String = "1.0.1"

    const val kotlintest_assertions: String = "3.4.2" // available: "3.4.2"

    const val io_micrometer: String = "1.2.1" // available: "1.2.1"

    const val mockk: String = "1.9.3" 

    const val io_vertx: String = "3.8.1" // available: "4.0.0-milestone3"

    const val logstash_logback_encoder: String = "6.2" // available: "6.2"

    const val assertj_core: String = "3.13.2" // available: "3.13.2"

    const val bcprov_jdk15on: String = "1.63" // available: "1.63"

    const val elasticmq_rest_sqs_2_12: String = "0.14.14" // available: "0.14.14"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.50" // available: "1.3.50"

    const val kotlin_scripting_compiler_embeddable: String = "1.3.50" // available: "1.3.50"

    const val org_jetbrains_kotlin_kotlin_stdlib_jdk8: String = "1.3.50" // available: "1.3.50"

    const val junit_jupiter_api: String = "5.5.2" // available: "5.5.2"

    const val junit_jupiter_engine: String = "none" 

    const val junit_jupiter_params: String = "5.5.2" // available: "5.5.2"

    const val junit_bom: String = "5.5.2" // available: "5.5.2"

    const val mockito_core: String = "3.0.0" 

    const val org_owasp_dependencycheck_gradle_plugin: String = "5.2.2" // available: "5.2.2"

    const val org_sonarqube_gradle_plugin: String = "2.7.1" 

    const val junit_jupiter: String = "1.12.2" // available: "1.12.2"

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10.2"

        const val currentVersion: String = "5.6.2"

        const val nightlyVersion: String = "6.1-20190927220036+0000"

        const val releaseCandidate: String = ""
    }
}
