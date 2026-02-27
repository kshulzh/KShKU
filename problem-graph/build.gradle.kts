plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.vanniktech.maven.publish") version "0.36.0"
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
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                kotlin("stdlib")
                kotlin("reflect")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()
    coordinates(group.toString(), "problem-graph", version.toString())

    pom {
        name = "Problem Graph library"
        description = "resolving complex depends problems"
        inceptionYear = "2026"
        url = "https://github.com/kshulzh/KShKU"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kshulzh"
                name = "Kirill Shulzhenko"
                url = "https://github.com/kshulzh/"
                email = "kirill.shulzhenko2000@gmail.com"
                organization = "kshulzh"
                organizationUrl = "https://github.com/kshulzh"

            }
        }
        scm {
            url = "https://github.com/kshulzh/KShKU"
            connection = "scm:git:git://github.com/kshulzh/KShKU.git"
            developerConnection = "scm:git:ssh://git@github.com/kshulzh/KShKU.git"
        }
    }
}