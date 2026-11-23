import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

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

    compileOnly(platform(libs.jackson2.bom))
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly(platform(libs.jackson3.bom))
    compileOnly("tools.jackson.core:jackson-databind")

    testCompileOnly(libs.jspecify)

    "integrationTestFixtureImplementation"(platform(libs.testcontainers.bom))
    "integrationTestFixtureImplementation"("org.testcontainers:testcontainers")

    "integrationTestFixtureImplementation"(libs.dotenv.java)
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = SourcesJar.None()))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman")
    }
}
