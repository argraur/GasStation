package dev.argraur.gasstation.domain.model

data class GasStationState(
    val pumps: Int = 3,
    val pricePerLiter: Double = 3.529 / 3.785,
    val snacks: Map<String, Double> = mapOf(
        "Snickers" to 1.0,
        "Coca-Cola" to 0.5,
        "Sprite" to 0.5,
        "Red Bull" to 1.5,
        "Lay's" to 0.3,
        "Mountain Dew" to 1.0,
        "French-dog" to 5.0,
        "Hot-dog" to 5.0,
        "Cheeseburger" to 8.0,
        "Twix" to 1.0,
        "Bounty" to 1.0,
        "Цезарь-ролл из Политеха))" to 100.0
    ),
    val income: Double = 0.0
)