package dev.argraur.gasstation.domain.services

import androidx.compose.runtime.mutableStateOf
import dev.argraur.gasstation.domain.model.Car
import dev.argraur.gasstation.domain.model.CarStatus
import dev.argraur.gasstation.domain.model.GasStationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

@Single
class GasStationService {
    private val _cars = MutableStateFlow(mutableListOf<MutableStateFlow<Car>>())
    private val _gasStationState = MutableStateFlow(GasStationState())
    private val queue = LinkedList(mutableListOf<MutableStateFlow<Car>>())
    private val pumps = mutableListOf<MutableStateFlow<Car>>()
    val cars = _cars.asStateFlow()
    val gasStationState = _gasStationState.asStateFlow()

    private var id = 0

    suspend fun addCarToQueue(carFlow: MutableStateFlow<Car>) {
        if (pumps.size < _gasStationState.value.pumps) {
            fuel(carFlow)
        } else {
            queueCar(carFlow)
        }
    }

    private fun queueCar(carFlow: MutableStateFlow<Car>) {
        queue.add(carFlow)

        val place = queue.indexOf(carFlow)
        carFlow.update {
            it.copy(id = ++id, status = CarStatus.IN_QUEUE, queuePlace = place)
        }
        carsUpdate {
            it.add(carFlow)
        }
        println("Car ${carFlow.value.id} placed in queue")
    }

    private suspend fun fuel(carFlow: MutableStateFlow<Car>) {
        carFlow.update {
            it.copy(
                id = if (carFlow.value.id == -1) ++id else carFlow.value.id,
                status = CarStatus.FUELING,
		moneySpent = _gasStationState.value.pricePerLiter * it.fuelWanted
            )
        }

        carsUpdate {
            it.add(carFlow)
        }

        pumps.add(carFlow)

        println("Car ${carFlow.value.id} assigned to a pump!")
        val fuel = carFlow.value.fuelWanted
        for (i in 0..fuel.toInt()) {
            delay(500)
            carFlow.update {
                it.copy(
                    id = if (it.id == -1) ++id else it.id,
                    status = CarStatus.FUELING,
                    fueled = it.fueled + 1
                )
            }
        }

        if (carFlow.value.toilet)
            toiletBreak(carFlow)
        if (carFlow.value.wantsSnacks)
            snackUp(carFlow)
        payment(carFlow)

        pumps.remove(carFlow)

        carFlow.update { it.copy(status = CarStatus.FINISHED) }

        if (pumps.size < _gasStationState.value.pumps) {
            repeat(_gasStationState.value.pumps - pumps.size) {
                queueNext()
                delay(50)
            }
        }
    }

    private suspend fun snackUp(carFlow: MutableStateFlow<Car>) {
        carFlow.update { it.copy(status = CarStatus.STOCKING_UP) }
        val random = Random(System.currentTimeMillis())
        val n = random.nextInt(_gasStationState.value.snacks.size)
        val snacks = _gasStationState.value.snacks.asSequence().shuffled(Random(System.currentTimeMillis())).take(n).toList()
        val names = mutableListOf<String>()
        var spent = 0.0
        snacks.forEach {
            names.add(it.key)
            spent += it.value
        }
        delay(3000)
        carFlow.update { it.copy(status = CarStatus.STOCKED_UP, snacks = names, moneySpent = carFlow.value.moneySpent + spent) }
        delay(500)
    }

    private suspend fun toiletBreak(carFlow: MutableStateFlow<Car>) {
        carFlow.update { it.copy(status = CarStatus.DRIVER_ON_TOILET_BREAK) }
        delay(10000)
    }

    private suspend fun payment(carFlow: MutableStateFlow<Car>) {
        carFlow.update { it.copy(status = CarStatus.PAYING) }
        delay(5000)
        _gasStationState.update {
            it.copy(income = it.income + carFlow.value.moneySpent)
        }
    }

    private suspend fun queueNext() {
        if (queue.size == 0)
            return

        queue.forEach { it ->
            it.update {
                it.copy(
                    queuePlace = it.queuePlace - 1
                )
            }
        }

        fuel(queue.pop())
    }

    private fun carsUpdate(fn: (MutableList<MutableStateFlow<Car>>) -> Unit) {
        _cars.update {
            val list = it.toTypedArray().toMutableList()
            fn(list)
            list
        }
    }

    fun changeMaxPumps(maxPumps: Int) {
        _gasStationState.update {
            it.copy(pumps = maxPumps)
        }
        CoroutineScope(Dispatchers.Default).launch {
            repeat(maxPumps - pumps.size) {
                queueNext()
                delay(50)
            }
        }
    }

    fun changePricePerLiter(pricePerLiter: Double) {
        _gasStationState.update {
            it.copy(pricePerLiter = pricePerLiter)
        }
    }

    fun reset() {
        _cars.update {
            mutableListOf()
        }
        _gasStationState.update {
            GasStationState()
        }
        queue.clear()
        pumps.clear()
        id = 0
    }
}
