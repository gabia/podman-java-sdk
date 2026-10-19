plugins {
    `java-library`
}

val libs = versionCatalogs.named("libs")

val integrationTestFixture = sourceSets.create("integrationTestFixture")

java {
    registerFeature(integrationTestFixture.name) {
        usingSourceSet(integrationTestFixture)
    }
}

val javaComponent = components["java"] as AdhocComponentWithVariants

listOf("integrationTestFixtureApiElements", "integrationTestFixtureRuntimeElements").forEach {
    javaComponent.withVariantsFromConfiguration(configurations[it]) { skip() }
}

dependencies {
    "integrationTestFixtureImplementation"(project())

    "integrationTestFixtureCompileOnly"(libs.findLibrary("jspecify").get())

    "integrationTestFixtureCompileOnly"("org.junit.jupiter:junit-jupiter-api:${libs.findVersion("junit").get()}")
}
