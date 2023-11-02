package com.example.hello.infra

import com.pulumi.Context
import com.pulumi.Pulumi

fun main() {
    Pulumi.run { ctx: Context -> Stack().provision(ctx) }
}