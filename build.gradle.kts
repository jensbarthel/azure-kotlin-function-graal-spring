buildscript {
    repositories { mavenCentral() }
}

plugins {
    kotlin("jvm") version "[1.7.10, 2.0)"
}

allprojects {
    group = "com.example"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")

    val springBootVersion = "[3.0,4.0)"

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    }

    kotlin {
        jvmToolchain(17)
    }
}

