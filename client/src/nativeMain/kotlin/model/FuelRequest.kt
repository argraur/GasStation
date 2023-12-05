package model

import kotlinx.serialization.Serializable

@Serializable
data class FuelRequest(
    val fuel: Int,
    val snacks: Boolean,
    val toilet: Boolean
)