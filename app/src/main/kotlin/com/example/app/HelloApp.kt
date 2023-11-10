package com.example.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan("com.example")
@ConfigurationPropertiesScan("com.example")
class HelloApp

fun main(args: Array<String>) {
    runApplication<HelloApp>(*args) {
        val maybeWorkerPort = System.getenv("FUNCTIONS_HTTPWORKER_PORT")
        val workerPort = maybeWorkerPort ?: 43475
        println("Setting worker port to $workerPort")
        setDefaultProperties(mapOf("server.port" to workerPort))
    }
}
