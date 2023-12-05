package dev.argraur.gasstation.network.handler

import dev.argraur.gasstation.domain.model.Car
import dev.argraur.gasstation.domain.model.CarStatus
import dev.argraur.gasstation.network.model.FuelRequest
import dev.argraur.gasstation.domain.services.GasStationService
import io.ktor.serialization.deserialize
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.lang.Exception

@Single
class CarHandler(
    private val gasStationService: GasStationService
) {
    suspend fun handle(session: DefaultWebSocketServerSession) {
        with(session) {
            val carStateFlow = MutableStateFlow(Car())
            carStateFlow.update { it.copy(status = CarStatus.CONNECTED) }
            CoroutineScope(Dispatchers.Default).launch {
                eventListener(carStateFlow, this@with)
            }
            for (frame in incoming) {
                if (frame is Frame.Close) {
                    carStateFlow.update { it.copy(status = CarStatus.DISCONNECTED) }
                }
                if (frame is Frame.Text) {
                    try {
                        val request = converter?.deserialize<FuelRequest>(frame)
                        if (request == null) {
                            disconnect(carStateFlow, session)
                        }
                        request!!
                        carStateFlow.update {
                            it.copy(
                                status = CarStatus.REQ_RECV,
                                fuelWanted = request.fuel,
                                wantsSnacks = request.snacks,
                                toilet = request.toilet
                            )
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            gasStationService.addCarToQueue(carStateFlow)
                        }
                    } catch (e: Exception) {
                        disconnect(carStateFlow, this)
                    }
                }
            }
        }
    }

    private suspend fun eventListener(car: MutableStateFlow<Car>, session: DefaultWebSocketServerSession) {
        val job = Job()
        CoroutineScope(job).launch {
            try {
                car.collect {
                    with(session) {
                        when (it.status) {
                            CarStatus.CONNECTED -> send("Welcome to Shell!")
                            CarStatus.REQ_RECV -> send("Fuel wanted: ${it.fuelWanted} L, it'll be $${it.fuelWanted * gasStationService.gasStationState.value.pricePerLiter}")
                            CarStatus.IN_QUEUE -> send("Sorry, all pipes are busy right now. You are ${it.queuePlace} in queue.")
                            CarStatus.FUELING -> send("Pumping fuel! Progress: ${it.fueled}/${it.fuelWanted} L")
                            CarStatus.DRIVER_ON_TOILET_BREAK -> send("Driver needs to visit a toilet, that'll take 10 seconds")
                            CarStatus.STOCKING_UP -> send("Driver is spending money on snacks!!")
                            CarStatus.STOCKED_UP -> send("Your items: ${it.snacks.joinToString(", ")}")
                            CarStatus.PAYING -> send("That'll be ${it.moneySpent}$")
                            CarStatus.FINISHED -> {
                                send("Thanks for using Shell services!")
                                disconnect(car, this)
                            }
                            CarStatus.DISCONNECTED -> println("Car has been disconnected.")
                        }
                    }
                }
            } catch (_: CancellationException) {
                println("Collect killed!")
            }
        }
        while (session.isActive) {
            delay(1000)
        }
        job.cancel("Car was disconnected")
    }

    private suspend fun disconnect(car: MutableStateFlow<Car>, session: DefaultWebSocketServerSession) {
        car.update { it.copy(status = CarStatus.DISCONNECTED) }
        session.close(CloseReason(CloseReason.Codes.NORMAL, "Bye-bye!"))
    }
}