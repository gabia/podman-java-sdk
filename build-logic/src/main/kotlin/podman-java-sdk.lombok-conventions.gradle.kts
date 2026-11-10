plugins {
    id("io.freefair.lombok")

    java
}

tasks.named<Jar>("sourcesJar") {
    val delombok = tasks.named("delombok")

    dependsOn(delombok)

    val originalSources = files(sourceSets.main.map { it.allSource })

    exclude {
        it.file in originalSources
    }

    from(delombok)
}
