pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "[0.5.0,1)"
}

rootProject.name = "azure-function-kotlin-graal-spring"
include("app")
include("stack")
