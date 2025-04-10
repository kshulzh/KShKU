plugins {
    alias(libs.plugins.kotlin.multiplatform)
}
allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven("https://maven.google.com")
    }
}
group = extra["project.group"]!!
version = extra["project.version"]!!

repositories {
    mavenCentral()
}

kotlin {
    jvm()
}