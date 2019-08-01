/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val logback_classic: String = "1.2.3" 

    const val amazon_sqs_java_messaging_lib: String = "1.0.6" // available: "1.0.6"

    const val java_jwt: String = "3.8.1" // available: "3.8.1"

    const val jackson_datatype_jsr310: String = "2.9.9" // available: "2.10.0.pr1"

    const val jackson_module_kotlin: String = "2.9.9" // available: "2.10.0.pr1"

    const val com_github_johnrengelman_shadow_gradle_plugin: String = "4.0.4" 
            // available: "5.1.0"

    const val ktlint: String = "0.31.0" // available: "0.31.0"

    const val newrelic_api: String = "5.3.0" 

    const val mockito_kotlin: String = "2.1.0" 

    const val mockwebserver: String = "4.0.1" // available: "4.0.1"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val de_flapdoodle_embed_mongo: String = "2.2.0" 

    const val arrow_core: String = "0.8.2" 

    const val io_gitlab_arturbosch_detekt: String = "1.0.0.RC9.2" 

    const val io_micrometer: String = "1.2.0" // available: "1.2.0"

    const val mockk: String = "1.9.3" // available: "1.9.3"

    const val io_vertx: String = "3.6.3" // available: "4.0.0-milestone1"

    const val logstash_logback_encoder: String = "6.1" // available: "6.1"

    const val assertj_core: String = "3.13.1" // available: "3.13.1"

    const val bcprov_jdk15on: String = "1.62" // available: "1.62"

    const val elasticmq_rest_sqs_2_12: String = "0.14.7" // available: "0.14.7"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.41" // available: "1.3.41"

    const val kotlin_scripting_compiler_embeddable: String = "1.3.41" // available: "1.3.41"

    const val org_jetbrains_kotlin_kotlin_stdlib_jdk8: String = "1.3.41" // available: "1.3.41"

    const val junit_jupiter_api: String = "5.5.1" // available: "5.5.1"

    const val junit_jupiter_engine: String = "none" 

    const val junit_jupiter_params: String = "5.5.1" // available: "5.5.1"

    const val junit_bom: String = "5.5.1" // available: "5.5.1"

    const val org_owasp_dependencycheck_gradle_plugin: String = "3.3.4" // available: "5.2.0"

    const val org_sonarqube_gradle_plugin: String = "2.7.1" // available: "2.7.1"

    const val junit_jupiter: String = "1.12.0" // available: "1.12.0"

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10.2"

        const val currentVersion: String = "5.5.1"

        const val nightlyVersion: String = "5.7-20190731220046+0000"

        const val releaseCandidate: String = "5.6-rc-1"
    }
}
