rootProject.name = "podman-java-sdk"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("podman-java-sdk")
include("podman-java-sdk-jackson2")
include("podman-java-sdk-jackson3")
include("podman-java-sdk-integration-test")
include("podman-java-sdk-bom")
include("podman-java-sdk-spring-boot4-starter")
