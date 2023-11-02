package com.example.adapter.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("hello/{id}")
    fun handleGetHello(
        @PathVariable id: String
    ): ResponseEntity<String> {
        return ResponseEntity("Hello $id", null, 200)
    }
}