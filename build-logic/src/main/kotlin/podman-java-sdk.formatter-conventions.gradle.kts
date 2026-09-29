plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        importOrder()

        palantirJavaFormat()

        formatAnnotations()

        licenseHeaderFile(rootProject.file("gradle/spotless/license-header.java"))
    }
}
