plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradle.spotless)

    implementation(libs.gradle.lombok)

    implementation(libs.gradle.maven.publish)
}
