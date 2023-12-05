package dev.argraur.gasstation.domain.model

enum class CarStatus {
    CONNECTED,
    REQ_RECV,
    IN_QUEUE,
    FUELING,
    STOCKING_UP,
    STOCKED_UP,
    PAYING,
    DRIVER_ON_TOILET_BREAK,
    FINISHED,
    DISCONNECTED
}