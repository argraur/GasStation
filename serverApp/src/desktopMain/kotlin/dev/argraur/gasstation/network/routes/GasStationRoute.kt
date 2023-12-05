package dev.argraur.gasstation.network.routes

import dev.argraur.gasstation.network.handler.CarHandler
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket

fun Application.gasStationRoute(carHandler: CarHandler) {
    routing {
        webSocket("/") {
            carHandler.handle(this)
        }
    }
}