import com.vanniktech.maven.publish.JavaPlatform

plugins {
    id("podman-java-sdk.common-conventions")

    id("podman-java-sdk.maven-publish-conventions")

    `java-platform`
}

dependencies {
    constraints {
        api(project(":podman-java-sdk"))
        api(project(":podman-java-sdk-jackson2"))
        api(project(":podman-java-sdk-jackson3"))
    }
}

mavenPublishing {
    configure(JavaPlatform())
}
