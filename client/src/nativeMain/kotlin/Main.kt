import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import model.FuelRequest
import platform.posix.time
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
fun main(engine: HttpClientEngineFactory<HttpClientEngineConfig>) {
    val client = HttpClient(engine) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    runBlocking {
        client.webSocket(method = HttpMethod.Get, host = "172.25.160.1", port = 8080, path = "/") {
            sendSerialized(FuelRequest(
                fuel = 5,
                toilet = false,
                snacks = false
            ))
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                println(frame.readText())
            }
        }
    }
}
