plugins {
    id("podman-java-sdk.java-common-conventions")

    id("podman-java-sdk.formatter-conventions")

    `jvm-test-suite`
}

testing {
    suites {
        val serializers = mapOf(
            "jackson2IntegrationTest" to ":podman-java-sdk-jackson2",
            "jackson3IntegrationTest" to ":podman-java-sdk-jackson3",
        )

        serializers.forEach { (suiteName, serializer) ->
            register<JvmTestSuite>(suiteName) {
                useJUnitJupiter(libs.versions.junit)

                sources {
                    java.setSrcDirs(listOf("src/integrationTest/java"))
                    resources.setSrcDirs(listOf("src/integrationTest/resources"))
                }

                dependencies {
                    implementation(project(":podman-java-sdk"))
                    implementation(project(":podman-java-sdk")) {
                        capabilities { requireFeature("integration-test-fixture") }
                    }
                    runtimeOnly(project(serializer)) {
                        capabilities { requireFeature("integration-test-fixture") }
                    }
                }
            }
        }
    }
}

val podmanPoolSize = providers.systemProperty("podman.test.pool-size")

val useExternalPodman = providers
    .fileContents(layout.projectDirectory.file(".env"))
    .asText
    .map { text ->
        text.lineSequence()
            .map(String::trim)
            .firstOrNull { it.startsWith("USE_EXTERNAL_PODMAN=") }
            ?.substringAfter("=")
            ?.trim()
            .orEmpty()
    }
    .filter(String::isNotEmpty)
    .orElse(providers.environmentVariable("USE_EXTERNAL_PODMAN"))
    .map(String::toBoolean)
    .orElse(false)

abstract class ExternalPodman : BuildService<BuildServiceParameters.None>

// Suites would share the external daemons, so only one of them may use them at a time
val externalPodman = gradle.sharedServices.registerIfAbsent("externalPodman", ExternalPodman::class) {
    maxParallelUsages.set(1)
}

tasks.withType<Test>().configureEach {
    podmanPoolSize.orNull?.let { systemProperty("podman.test.pool-size", it) }

    if (useExternalPodman.get()) {
        usesService(externalPodman)
    }

    reports {
        html.required.set(true)

        junitXml.required.set(true)
    }
}

tasks.register("integrationTest") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn("jackson2IntegrationTest", "jackson3IntegrationTest")
}
