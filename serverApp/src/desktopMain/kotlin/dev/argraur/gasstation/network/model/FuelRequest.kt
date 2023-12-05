package dev.argraur.gasstation.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FuelRequest(
    val fuel: Double,
    val snacks: Boolean,
    val toilet: Boolean
)