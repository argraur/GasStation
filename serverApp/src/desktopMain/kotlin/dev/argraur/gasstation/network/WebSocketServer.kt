package dev.argraur.gasstation.network

import dev.argraur.gasstation.network.handler.CarHandler
import dev.argraur.gasstation.network.routes.gasStationRoute
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

const val PORT = 8080

data class WebSocketServerState(
    var running: Boolean = false,
    var port: Int = 8080
)

@Single
class WebSocketServer(
    private val carHandler: CarHandler,
) {
    private val _state = MutableStateFlow(WebSocketServerState())
    val state = _state.asStateFlow()

    private lateinit var server: ApplicationEngine

    fun startServer() {
        CoroutineScope(Dispatchers.Default).launch {
            server = embeddedServer(Netty, PORT) {
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
                gasStationRoute(carHandler)
            }
            server.start()
            _state.update { it.copy(running = true, port = PORT) }
        }
    }

    fun stopServer() {
        CoroutineScope(Dispatchers.Default).launch {
            server.stop()
            _state.update { it.copy(running = false, port = -1) }
        }
    }
}