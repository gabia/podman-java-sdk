import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("podman-java-sdk.java-library-conventions")

    id("podman-java-sdk.formatter-conventions")

    id("podman-java-sdk.lombok-conventions")

    id("podman-java-sdk.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.jspecify)

    api(project(":podman-java-sdk"))
    api(project(":podman-java-sdk-jackson3"))

    compileOnly(platform(libs.spring.boot.dependencies))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testCompileOnly(libs.jspecify)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-jackson")
    testImplementation("org.assertj:assertj-core")
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = SourcesJar.None()))
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.gabiacloud.podman.spring.boot.autoconfigure")
    }
}
