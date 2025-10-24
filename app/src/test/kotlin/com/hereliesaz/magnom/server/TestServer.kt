package com.hereliesaz.magnom.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TestServer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, port = 8080) {
                install(ContentNegotiation) {
                    json()
                }
                routing {
                    post("/data") {
                        val data = call.receiveText()
                        println("Received data: $data")
                        call.respondText("Data received")
                    }
                }
            }.start(wait = true)
        }
    }
}
