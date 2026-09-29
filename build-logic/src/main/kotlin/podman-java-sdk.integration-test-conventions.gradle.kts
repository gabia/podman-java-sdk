plugins {
    `jvm-test-suite`
}

val libs = versionCatalogs.named("libs")

testing {
    suites {
        register("integrationTest", JvmTestSuite::class) {
            useJUnitJupiter(libs.findVersion("junit").get().requiredVersion)

            dependencies {
                implementation(project())
            }
        }
    }
}

configurations {
    named("integrationTestImplementation").configure {
        extendsFrom(named("implementation").get())
    }

    named("integrationTestCompileOnly").configure {
        extendsFrom(named("compileOnly").get())
    }
}

tasks.named<Test>("integrationTest") {
    reports {
        html.required.set(true)

        junitXml.required.set(true)
    }
}
