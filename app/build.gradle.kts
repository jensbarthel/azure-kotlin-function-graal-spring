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

plugins {
    kotlin("plugin.spring") version "[1.4.0,2.0)"
    id("org.springframework.boot") version "[3.1.5,4)"
    id("org.graalvm.buildtools.native") version "[0.9.28, 1)"
}

val springBootVersion = "[3.0,4.0)"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.create<Zip>("packageDistribution") {
    dependsOn("nativeCompile")
    archiveFileName = "${getParent()!!.name}-${project.name}.zip"
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
    from("src/main/function")
    from("build/native/nativeCompile/app")
}