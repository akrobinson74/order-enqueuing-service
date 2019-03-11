pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

include("app", "integration-test")

with(rootProject) {
    name = "order_enqueuing_service"
    children.forEach { it.buildFileName = "${it.name}.gradle.kts" }
}

enableFeaturePreview("IMPROVED_POM_SUPPORT")

val isCiServer = false // disabled until the cache is back
buildCache {
    local {
        isEnabled = !isCiServer
    }
}