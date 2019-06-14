/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val aws_sqs_msg_lib: String = "1.0.5"

    const val okhttp3_mockwebserver = "3.14.0"

    const val elastimq_sqs = "0.14.6"

    const val logback_classic: String = "1.2.3" 

    const val java_jwt: String = "3.6.0" // available: "3.7.0"

    const val jackson_datatype_jsr310: String = "2.9.8" 

    const val jackson_module_kotlin: String = "2.9.8" 

    const val com_github_johnrengelman_shadow_gradle_plugin: String = "4.0.4" 
            // available: "5.0.0"

    const val ktlint: String = "0.29.0" // available: "0.30.0"

    const val com_newrelic_agent_java: String = "5.1.1" // available: "4.11.0"

    const val mockito_kotlin: String = "2.1.0" 

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2"

    const val embedded_mongo: String = "2.2.0"

    const val arrow_core: String = "0.8.2" 

    const val io_gitlab_arturbosch_detekt: String = "1.0.0.RC9.2"

    const val io_micrometer: String = "1.1.2"

    const val io_mockk: String = "1.9.1.kotlin12"

    const val io_vertx: String = "3.6.3" // available: "3.6.3"

    const val logstash_logback_encoder: String = "5.3" 

    const val assertj_core: String = "3.11.1" // available: "3.12.1"

    const val bcprov_jdk15on: String = "1.60" // available: "1.61"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.20" // available: "1.3.21"

    const val kotlin_scripting_compiler_embeddable: String = "1.3.20" // available: "1.3.21"

    const val org_jetbrains_kotlin_kotlin_stdlib_jdk8: String = "1.3.0" // available: "1.3.21"

    const val junit_jupiter_api: String = "5.3.2" // available: "5.4.0"

    const val junit_jupiter_engine: String = "none" 

    const val junit_jupiter_params: String = "5.3.2" // available: "5.4.0"

    const val junit_bom: String = "5.3.2" // available: "5.4.0"

    const val org_owasp_dependencycheck_gradle_plugin: String = "3.3.4" // available: "4.0.2"

    const val org_sonarqube_gradle_plugin: String = "2.6.2" // available: "2.7"

    const val vertx_micrometer_metrics: String = ""

    const val test_containers: String = "1.10.6"
    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10.2"

        const val currentVersion: String = "5.2.1"

        const val nightlyVersion: String = "5.4-20190307000626+0000"

        const val releaseCandidate: String = "5.3-rc-1"
    }
}
