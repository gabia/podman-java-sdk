plugins {
    id("podman-java-sdk.java-library-conventions")

    id("podman-java-sdk.formatter-conventions")

    id("podman-java-sdk.lombok-conventions")

    id("podman-java-sdk.integration-test-fixture-conventions")

    id("podman-java-sdk.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.jspecify)

    api(project(":podman-java-sdk"))

    api(libs.jackson3.databind)

    testCompileOnly(libs.jspecify)

    "integrationTestFixtureApi"(project(":podman-java-sdk")) {
        capabilities { requireFeature("integration-test-fixture") }
    }
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman.jackson3")
    }
}
