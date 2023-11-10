package com.example.adapter.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("hello")
    fun handleGetHello(
    ): ResponseEntity<String> {
        return ResponseEntity("Hello friend", null, 200)
    }
}