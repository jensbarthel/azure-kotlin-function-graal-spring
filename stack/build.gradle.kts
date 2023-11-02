buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:[3.0,4.0)")
        classpath("org.jetbrains.kotlin:kotlin-noarg:[1.4.0,2.0)")
        classpath("org.jetbrains.kotlin:kotlin-allopen:[1.4.0,2.0)")
    }
}

val springBootVersion = "[2.7.3,3.0)"

plugins {
    id("application")
    id("org.jetbrains.kotlin.plugin.spring") version "[1.7.10,2.0)"
}

application {
    mainClass.set("com.example.hello.infra.MainKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    implementation("com.pulumi:pulumi:[0.6.0,1.0]")
    implementation("com.pulumi:azure-native:(1.0,2.0]")
    implementation("org.springframework.boot:spring-boot-starter")
}