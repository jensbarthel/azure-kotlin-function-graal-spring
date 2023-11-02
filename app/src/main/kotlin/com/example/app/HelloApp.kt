package com.example.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan("com.example")
@ConfigurationPropertiesScan("com.example")
class HelloApp {
}

fun main(args: Array<String>) {
    runApplication<HelloApp>(*args) {
        val maybeWorkerPort = System.getenv("FUNCTIONS_HTTPWORKER_PORT")
        System.err.println("======================== PORT: ${System.getenv("FUNCTIONS_HTTPWORKER_PORT")}")
        maybeWorkerPort?.let { port ->

            setDefaultProperties(mapOf("server.port" to port))
        } ?: setDefaultProperties(mapOf("server.port" to 43475))
    }
}
