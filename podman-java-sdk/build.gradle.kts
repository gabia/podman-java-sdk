plugins {
    id("podman-java-sdk.java-library-conventions")

    id("podman-java-sdk.formatter-conventions")

    id("podman-java-sdk.lombok-conventions")

    id("podman-java-sdk.integration-test-fixture-conventions")

    id("podman-java-sdk.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.jspecify)

    api(libs.jackson.annotations)

    compileOnly(libs.jackson2.databind)
    compileOnly(libs.jackson3.databind)

    testCompileOnly(libs.jspecify)

    "integrationTestFixtureImplementation"(platform(libs.testcontainers.bom))
    "integrationTestFixtureImplementation"("org.testcontainers:testcontainers")

    "integrationTestFixtureImplementation"(libs.dotenv.java)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman")
    }
}
