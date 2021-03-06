import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    jacoco
    `java-library`
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    id("io.gitlab.arturbosch.detekt") version Versions.io_gitlab_arturbosch_detekt
    id("org.owasp.dependencycheck") version Versions.org_owasp_dependencycheck_gradle_plugin
    id("org.sonarqube") version Versions.org_sonarqube_gradle_plugin
    kotlin("jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
        maven {
            url = uri("s3://phizzard-jars/maven")
            authentication {
                val awsIm by registering(AwsImAuthentication::class)
            }
        }
    }
}
subprojects {
    apply {
        plugin("com.github.ben-manes.versions")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("jacoco")
        plugin("java-library")
        plugin("kotlin")
    }

// -------------------------- Dependencies ------------------------------------

    val ktlint by configurations.creating

    dependencies {
        api(Libs.arrow_core)
        api(Libs.org_jetbrains_kotlin_kotlin_stdlib_jdk8)
        testCompile(Libs.junit_jupiter_api)
        testImplementation(Libs.junit_bom)
        testImplementation(Libs.assertj_core)
        testImplementation(Libs.junit_jupiter_api)
        testImplementation(Libs.junit_jupiter_params)
        testImplementation(Libs.kotlintest_assertions)
        testImplementation(Libs.mockk)
        testImplementation(Libs.mockito_core)
        testImplementation(Libs.mockito_kotlin) {
            exclude(module = "kotlin-reflect")
        }
        testImplementation(Libs.mockwebserver)
        testImplementation(Libs.junit_jupiter)
        testImplementation(Libs.vertx_junit5)
        testRuntimeOnly(Libs.junit_jupiter_engine)
        ktlint(Libs.ktlint)
    }

    tasks {
        getByName<Test>("test") {
            extensions.configure(JacocoTaskExtension::class) {
                excludes = listOf("*AppKt*","*ExtensionsKt*","*MetricsKt*")
            }
            useJUnitPlatform {
                excludeTags("integration")
            }
        }
        create<Test>("integration-test") {
            useJUnitPlatform {
                includeTags("integration")
            }
        }
        getByName<DependencyUpdatesTask>("dependencyUpdates") {
            resolutionStrategy {
                componentSelection {
                    all {
                        val rejected = listOf("alpha", "beta", "rc", "cr", "m", "dmr")
                            .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                            .any { it.matches(candidate.version) }
                        if (rejected) {
                            reject("Release candidate")
                        }
                    }
                }
            }
        }
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        withType<Test> {
            reports.html.destination = file("${reporting.baseDir}/$name")
            testLogging { exceptionFormat = TestExceptionFormat.FULL }
            // https://github.com/gradle/gradle/issues/5431
            addTestListener(object : TestListener {
                override fun beforeTest(testDescriptor: TestDescriptor) {}
                override fun beforeSuite(suite: TestDescriptor) {}
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if (suite.parent == null) {
                        println(buildString {
                            append("Results: ${result.resultType} (${result.testCount} tests, ")
                            append("${result.successfulTestCount} successes, ")
                            append("${result.failedTestCount} failures, ")
                            append("${result.skippedTestCount} skipped)")
                        })
                    }
                }
            })
        }

        val ktlintMain = "com.github.shyiko.ktlint.Main"
        val ktlintArgs = listOf("-F", "$projectDir/src/**/*.kt")
        create<JavaExec>("ktlintCheck") {
            description = "Runs ktlint on all kotlin sources in this project."
//            tasks["check"].group?.let { group = it }
            main = ktlintMain
            classpath = ktlint
            args = ktlintArgs.drop(1)
        }
        create<JavaExec>("ktlintFormat") {
            description = "Runs the ktlint formatter on all kotlin sources in this project."
            group = "Formatting tasks"
            main = ktlintMain
            classpath = ktlint
            args = ktlintArgs
        }
    }

    detekt {
        toolVersion = Versions.io_gitlab_arturbosch_detekt
        input = files("$projectDir/src/main/kotlin", "$projectDir/src/test/kotlin")
        config = files("$rootDir/detekt.yml")
    }

    jacoco {
        toolVersion = "0.8.2"
    }
}

// -------------------------- Global Setup -----------------------------------

tasks {
    create<JacocoReport>("jacocoRootTestReport") {
        getClassDirectories().setFrom(files("${project(":app").buildDir}/classes/kotlin/main"))
        getSourceDirectories().setFrom(files("${project(":app").projectDir}/src/kotlin/main"))

        val validJacocoFileNames = listOf("test.exec", "integration-test.exec")
        val jacocoTestFiles = subprojects.map { project ->
            validJacocoFileNames.map { "${project.buildDir}/jacoco/$it" }.filter { file(it).exists() }
        }.flatten()
            .filter { it.isNotEmpty() }
            .toTypedArray()

        logger.info("Aggregating next JaCoCo Coverage files: {}", jacocoTestFiles)
        getExecutionData().setFrom(files(*jacocoTestFiles))

        reports.csv.isEnabled = true
    }
}

configure<DependencyCheckExtension> {
    autoUpdate = true
    format = ReportGenerator.Format.ALL
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
