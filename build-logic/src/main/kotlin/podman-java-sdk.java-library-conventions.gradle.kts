plugins {
    id("podman-java-sdk.java-common-conventions")

    `java-library`
}

java {
    withSourcesJar()

    withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"

    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}
