package dev.argraur.gasstation.domain.model

data class Car(
    var id: Int = -1,
    var status: CarStatus = CarStatus.DISCONNECTED,
    var fuelWanted: Double = 0.0,
    var fueled: Double = 0.0,
    var moneySpent: Double = 0.0,
    var toilet: Boolean = false,
    var wantsSnacks: Boolean = false,
    var snacks: List<String> = listOf(),
    var queuePlace: Int = -1
)