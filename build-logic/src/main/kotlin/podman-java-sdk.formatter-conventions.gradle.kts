plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        importOrder()

        palantirJavaFormat()

        formatAnnotations()
    }
}
