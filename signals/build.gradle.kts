import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

group = extra["project.group"]!!
version = extra["project.version"]!!

kotlin {
    //jvm
    jvm()
    //native
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                kotlin("stdlib")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation(project(":thread-local"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI.create("https://maven.pkg.github.com/kshulzh/KShKU")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}