package dev.argraur.gasstation.ui

import dev.argraur.gasstation.domain.services.GasStationService
import dev.argraur.gasstation.network.WebSocketServer
import org.koin.core.annotation.Single

@Single
class AppViewModel(
    private val webSocketServer: WebSocketServer,
    private val gasStationService: GasStationService
) {
    val serverState = webSocketServer.state
    val cars = gasStationService.cars
    val stationState = gasStationService.gasStationState

    fun startServer() = webSocketServer.startServer()
    fun stopServer() = webSocketServer.stopServer()

    fun changeMaxPumps(maxPumps: Int) {
        gasStationService.changeMaxPumps(maxPumps)
    }

    fun changePricePerLiter(pricePerLiter: Double) {
        gasStationService.changePricePerLiter(pricePerLiter)
    }

    fun resetStation() = gasStationService.reset()
}