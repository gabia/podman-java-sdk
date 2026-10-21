plugins {
    id("podman-java-sdk.common-conventions")

    java
}

val libs = versionCatalogs.named("libs")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.findVersion("java").get().requiredVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
            )
        )
    }
}

testing {
    suites {
        named("test", JvmTestSuite::class) {
            useJUnitJupiter(libs.findVersion("junit").get().requiredVersion)
        }
    }
}
