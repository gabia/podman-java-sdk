plugins {
    id("podman-java-sdk.java-library-conventions")

    id("podman-java-sdk.formatter-conventions")

    id("podman-java-sdk.lombok-conventions")

    id("podman-java-sdk.integration-test-conventions")

    id("podman-java-sdk.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.jspecify)

    api(libs.jackson.databind)

    integrationTestCompileOnly(libs.jspecify)

    integrationTestImplementation(platform(libs.testcontainers.bom))
    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:testcontainers-junit-jupiter")

    integrationTestImplementation(libs.dotenv.java)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman")
    }
}
