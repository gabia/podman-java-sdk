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

    api(project(":podman-java-sdk"))

    api(libs.jackson2.databind)
    runtimeOnly(libs.jackson2.jsr310)

    testCompileOnly(libs.jspecify)

    "integrationTestFixtureApi"(project(":podman-java-sdk")) {
        capabilities { requireFeature("integration-test-fixture") }
    }
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = SourcesJar.None()))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman.jackson2")
    }
}
