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

tasks.withType<Test>().configureEach {
    reports {
        html.required.set(true)

        junitXml.required.set(true)
    }
}

tasks.register("integrationTest") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn("jackson2IntegrationTest", "jackson3IntegrationTest")
}
