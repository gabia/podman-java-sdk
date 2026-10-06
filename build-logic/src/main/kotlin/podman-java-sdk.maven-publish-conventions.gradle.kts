import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("com.vanniktech.maven.publish.base")

    signing
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = SourcesJar.None()))

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()

    pom {
        name.set(project.name)
        description.set("Java client for the Podman REST API")
        url.set("https://github.com/gabia/podman-java-sdk")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                name.set("가비아 클라우드개발팀")
                email.set("cloud-system@gabia.com")
                organization.set("Gabia Cloud")
                organizationUrl.set("https://www.gabiacloud.com/")
            }
        }

        scm {
            url.set("https://github.com/gabia/podman-java-sdk")
            connection.set("scm:git:https://github.com/gabia/podman-java-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/gabia/podman-java-sdk.git")
        }
    }
}

signing {
    setRequired(provider {
        !version.toString().endsWith("-SNAPSHOT") &&
            gradle.taskGraph.allTasks.any { it is PublishToMavenRepository && it.repository.name == "mavenCentral" }
    })
}
