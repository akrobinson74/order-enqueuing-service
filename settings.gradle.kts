pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

include("acceptance-test", "app", "integration-test")

with(rootProject) {
    name = "order-enqueuing-service"
    children.forEach { it.buildFileName = "${it.name}.gradle.kts" }
}

val isCiServer = false // disabled until the cache is back
buildCache {
    local {
        isEnabled = !isCiServer
    }
}