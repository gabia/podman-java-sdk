plugins {
    id("io.freefair.lombok")

    java
}

tasks.named<Jar>("sourcesJar") {
    val delombok = tasks.named("delombok")

    dependsOn(delombok)

    exclude {
        it.file in sourceSets.main.get().allSource.files
    }

    from(delombok)
}
